# Spring AI 핵심 - ChatClient

> Spring AI에서 AI 모델과 대화하는 핵심 인터페이스. 빌더 패턴으로 프롬프트를 구성하고 다양한 형태로 응답을 받을 수 있다.

## 메서드

| 메서드 | 역할 |
|--------|------|
| `.prompt()` | 프롬프트 빌더 시작점 |
| `.system()` | AI 페르소나·지침 설정. 매 요청마다 숨겨진 첫 메시지로 전달됨 |
| `.user()` | 사용자 입력 메시지. 람다로 템플릿 파라미터 주입 가능 |
| `.call()` | 모델 호출. 여기서 실제 HTTP 요청이 나감 |
| `.content()` | 응답 텍스트만 String으로 반환. 메타정보 필요 없을 때 사용 |

## ChatConfig 설정

```java
@Configuration
public class ChatConfig {

  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    // yml에 등록된 설정을 바탕으로 ChatClient를 생성하며,
    // 공통 지침(defaultSystem)만 여기서 추가.
    return builder
        .defaultSystem("""
            당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
            사용자의 질문에 정확하고 이해하기 쉽게 답변해주세요.
            """)
        .build();
  }
}
```

- 위와 같이 설정하면 모든 요청에 공통 프롬프트가 적용된다.
- 서비스에서 `.system()`으로 따로 프롬프트 등록 시 그게 우선순위를 가짐

## 기본 사용

```java
// Bean 등록
public AiChatController(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
}

// 기본 호출
chatClient.prompt()
    .user(message)
    .call()
    .content();

// System Message + Prompt Template
chatClient.prompt()
    .system("너는 전문 번역가야.")
    .user(u -> u.text("다음을 {lang}로 번역해줘: {text}")
        .param("lang", "영어")
        .param("text", "안녕하세요"))
    .call()
    .content();
```

## 응답 형태 비교

| 메서드 | 반환 타입 | 용도 |
|--------|-----------|------|
| `.content()` | `String` | 텍스트만 필요할 때 |
| `.entity(Foo.class)` | `Foo` | DTO로 바로 역직렬화 |
| `.chatResponse()` | `ChatResponse` | 토큰 사용량 등 메타정보 포함 |

## ChatResponse

> `.chatResponse()`로 받으면 응답 텍스트 외에 다양한 메타정보에 접근 가능

```java
ChatResponse response = chatClient.prompt()
    .user(message)
    .call()
    .chatResponse();

// 응답 텍스트
String text = response.getResult().getOutput().getText();

// 종료 이유 (STOP / LENGTH / CONTENT_FILTER 등)
String finishReason = response.getResult().getMetadata().getFinishReason();

// 토큰 사용량
Usage usage = response.getMetadata().getUsage();
usage.getPromptTokens();      // 입력 토큰 수
usage.getCompletionTokens();  // 출력 토큰 수
usage.getTotalTokens();       // 전체 토큰 수
```

### ChatResponse 구조

```
ChatResponse
├── getResult()                       → Generation (단일 응답)
│   ├── getOutput()                   → AssistantMessage
│   │   └── getText()                 → 응답 텍스트 (String)
│   └── getMetadata()                 → ChatGenerationMetadata
│       └── getFinishReason()         → STOP / LENGTH / CONTENT_FILTER
│
└── getMetadata()                     → ChatResponseMetadata
    └── getUsage()                    → Usage
        ├── getPromptTokens()         → 입력 토큰 수
        ├── getCompletionTokens()     → 출력 토큰 수
        └── getTotalTokens()          → 합계
```

> `getResults()` (복수)는 모델이 여러 후보 응답을 반환할 때 사용. 일반적으로는 `getResult()` 하나만 옴.

## Structured Output

> 문자열로 받아서 파싱하면 AI가 매번 다른 문장 구조로 답해 파싱 로직이 깨질 수 있음 → `.entity()`로 해결

```java
// DTO 정의
@Getter
@NoArgsConstructor
public class ProductAnalysisResponse {
    @JsonProperty("sentiment") String sentiment;  // positive/neutral/negative
    @JsonProperty("score") int score;             // 1~10
    @JsonProperty("summary") String summary;
}

// .entity()로 바로 변환
ProductAnalysisResponse result = chatClient.prompt()
    .user(u -> u.text(promptText).param("review", review))
    .call()
    .entity(ProductAnalysisResponse.class);
```

## Streaming

> 응답이 길거나 UX상 실시간으로 보여줘야 할 때 `.stream()` 사용

```java
Flux<String> stream = chatClient.prompt()
    .user(message)
    .stream()
    .content();  // Flux<String> 반환
```

`.call()` 대신 `.stream()`으로 교체하면 토큰 단위로 스트리밍 가능.

## ChatOptions — 창의성 조절

> `temperature`: 0에 가까울수록 일관·정확, 1에 가까울수록 창의·다양

```java
// 창의적인 답변 (이야기, 마케팅)
.options(ChatOptions.builder()
    .temperature(0.9)
    .maxTokens(500)
    .build())

// 정확한 답변 (요약, 번역)
.options(ChatOptions.builder()
    .temperature(0.1)
    .maxTokens(200)
    .build())
```

---