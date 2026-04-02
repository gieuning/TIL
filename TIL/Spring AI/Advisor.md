# Advisor란?

Spring AI에서 `ChatClient`의 요청과 응답 사이를 가로채는 미들웨어, 인터셉터 패턴 구현체

복잡한 부가 기능(대화 저장, 문서 검색 등)을 `ChatClient` 외부로 분리해서 핵심 서비스 로직을 간결하게 유지하는 게 목적

---

## 동작 방식

요청이 나갈 때는 밖에서 안으로(Before), 응답이 돌아올 때는 안에서 밖으로(After) 순서로 실행

```
사용자 질문
    ↓
[Advisor 1] ← before() : 프롬프트 수정, 컨텍스트 추가
    ↓
[Advisor 2] ← before() : Advisor Chain
    ↓
LLM 호출
    ↓
[Advisor 2] ← after() : 로깅/모니터링
    ↓
[Advisor 1] ← after() : Advisor Chain
    ↓
최종 응답
```

---

## Advisor를 왜 쓰나?

서비스 코드에 모든 기능을 때려박으면 이렇게 됨

```java
public String chat(String message) {
  // 대화 기록 조회 (관심사 1)
  List<Message> history = chatMemory.get(conversationId);

  // 문서 검색 (관심사 2)
  List<Document> docs = vectorStore.similaritySearch(message);

  // 프롬프트 구성 (지저분한 문자열 결합)
  String fullPrompt = "Context: " + docs + "\nHistory: " + history + "\nQuestion: " + message;

  // LLM 호출
  String response = chatClient.call(fullPrompt);

  // 대화 기록 저장 (관심사 1 마무리)
  chatMemory.add(conversationId, message, response);

  return response;
}
```

Advisor로 분리하면 서비스 코드는 "질문하고 답 받기"에만 집중할 수 있음

```java
// 설정 단계: 필요한 기능 조립
ChatClient chatClient = chatClientBuilder
        .defaultAdvisors(
            new MessageChatMemoryAdvisor(chatMemory), // 대화 기록 자동 관리
            new QuestionAnswerAdvisor(vectorStore)    // RAG 자동 처리
        )
        .build();

// 실행 단계
public String chat(String message) {
  return chatClient.prompt().user(message).call().content();
}
```

-> AOP랑 같은 개념
-> 공통 관심사를 서비스 밖으로 빼서 재사용 가능한 단위로 만든 것

**ChatMemoryAdvisor 예시**
<details>

```java

@Slf4j
public class ChatMemoryAdvisor implements BaseAdvisor {

  private final Map<String, List<Message>> conversationStore = new ConcurrentHashMap<>();
  // 대화 내용을 저장하는 공간 (자료구조에 저장)
  private static final String CONTEXT_USER_TEXT = "chat_memory_user_text";
  private final String conversationId; // 룸 지정
  private final int maxMessages; // 룸 최대 몇개까지 지정

  public ChatMemoryAdvisor(String conversationId, int maxMessages) {
    this.conversationId = conversationId;
    this.maxMessages = maxMessages;
  }

  @Override
  public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
    // [핵심] 1. 저장소에서 이전 대화 기록을 가져옴
    List<Message> history = conversationStore.getOrDefault(conversationId, new ArrayList<>());

    // [핵심] 2. 이전 기록 + 현재 질문을 합쳐서 새로운 메시지 리스트 생성
    List<Message> fullMessage = new ArrayList<>();
    fullMessage.addAll(request.prompt().getInstructions());

    // 3. 현재 질문 텍스트 추출 (나중에 after에서 저장하기 위함)
    String userText = request.prompt().getInstructions().stream()
        .filter(m -> m.getMessageType() == MessageType.USER)
        .map(Message::getText) // 타입전환 -> 객체::메소드명 -> 자바.메소드
        .findFirst()
        .orElse("");

    // 4. 합쳐진 메시지들로 프롬프트를 재구성하여 반환
    return request.mutate() // 이 친구가 실행되고 나면 User, Prompt Text
        .prompt(new Prompt(fullMessage))  // AI에게 과거 기록을 함께 보냄
        .context(CONTEXT_USER_TEXT, userText) // userText에 태그를 붙여준다.
        .build();
  }

  @Override
  public ChatClientResponse after(ChatClientResponse response,
      AdvisorChain advisorChain) {
    List<Message> history = conversationStore.computeIfAbsent(
        conversationId, k -> new CopyOnWriteArrayList<>()
    );

    // 1. 사용자 질문 저장 (context에서 꺼냄)
    Optional.ofNullable(
            response.context().get(CONTEXT_USER_TEXT))
        .map(Object::toString)
        .filter(text -> !text.isBlank())
        .ifPresent(text -> history.add(new UserMessage(text)));

    // 2. AI 응답 저장
    if (response.chatResponse() != null && response.chatResponse().getResult() != null) {
      var output = response.chatResponse().getResult().getOutput();
      history.add(new AssistantMessage(output.getText(), output.getMetadata()));
    }

    // 3. FIFO 용량 관리
    while (history.size() > maxMessages && !history.isEmpty()) {
      history.remove(0);
    }

    return response;
  }


  @Override
  public int getOrder() {
    return 0; // 순서임 order는 기본적으로 0으로
  }

}
```

</details>

---

## BaseAdvisor 구조

구현해야 할 메서드는 두 개

```java
// 요청 전처리: 시스템 프롬프트 추가, 컨텍스트 삽입 등
ChatClientRequest before(ChatClientRequest request, AdvisorChain chain);

// 응답 후처리: 대화 기록 저장, 토큰 로깅 등
ChatClientResponse after(ChatClientResponse response, AdvisorChain chain);
```

`adviseCall()`은 자동 구현 -> `before() → chain.nextCall() → after()` 샌드위치 구조로 동작

동기(Call), 비동기(Stream) 둘 다 지원

---

## 데이터 객체

**ChatClientRequest**

- `adviseContext` : Advisor끼리 데이터 공유하는 Map -> 가장 중요
- `chatOptions` : temperature, Top-P 등 모델 설정
- `messages`, `userText` : 실제 대화 내용

**ChatClientResponse**

- `chatResponse` : 모델이 생성한 답변 + 토큰 사용량
- `adviseContext` : 요청 때 컨텍스트 그대로 유지 -> 후처리에서도 참조 가능

---

## SafeGuardAdvisor

실제 유저에게 서비스 노출할 때 안전성 담당하는 Advisor

**CASE 1 - 입력 차단**

- 사용자가 유해한 질문 입력
- `before()`에서 감지 -> LLM 호출 없이 바로 차단
- -> 불필요한 API 비용 절감, 브랜드 보호

**CASE 2 - 출력 정화**

- 질문은 정상인데 모델이 부적절한 답변 생성
- `after()`에서 감지 -> 안전한 문구로 교체 후 전달

보안 정책이니까 전역 설정으로 적용하는 게 일반적

