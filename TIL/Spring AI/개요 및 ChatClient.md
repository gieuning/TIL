# Spring AI란?

> Spring 생태계에서 LLM을 쉽게 통합할 수 있도록 만든 라이브러리.
OpenAI, Gemini 등 다양한 모델을 동일한 인터페이스로 사용 가능하다.

## 기본구조

```java
chatClient
  .prompt()          // 대화 시작
  .system("...")     // AI 역할/지침 설정
  .user("...")       // 유저 메시지
  .call()            // AI 호출
  .content()         // 응답 텍스트 꺼내기
```

## 구성요소 
1. system() — AI한테 역할 부여
```java
system("너는 이커머스 전문 상담원이야. 친절하게 답변해.")

// defaultSystem으로 Bean 등록시 공통 적용 가능
builder.defaultSystem("공통 지침...")
```


2. user() — 실제 질문
```java
user("나이키 운동화 추천해줘")
// 동적으로 값 넣기
.user(u -> u.text("이 상품 어때요? {product}")
.param("product", productName))
```

3. call() vs stream()
```java
// 전체 응답을 한번에 받기
.call().content()

// 스트리밍 (ChatGPT처럼 타이핑 효과)
.stream().content()
.subscribe(token -> System.out.print(token));
```

4. 응답 꺼내는 방법
```java
// 단순 텍스트
.call().content()

// 객체로 바로 변환 (Structured Output)
.call().entity(ProductRecommendation.class)

// 리스트로 변환
.call().entity(new ParameterizedTypeReference<List<Product>>() {})

// 메타데이터 포함 (토큰 사용량 등)
.call().chatResponse()

```

## 대화 메모리 (이전 대화 기억)
```java
// 메모리 없으면 — AI가 이전 대화를 기억 못함
chatClient.prompt().user("내 이름은 철수야").call().content();
chatClient.prompt().user("내 이름이 뭐야?").call().content();
// → "저는 당신의 이름을 모릅니다" ❌

// 메모리 추가하면
ChatClient chatClient = builder
.defaultAdvisors(
MessageChatMemoryAdvisor.builder(new InMemoryChatMemory()).build()
)
.build();

chatClient.prompt().user("내 이름은 철수야").call().content();
chatClient.prompt().user("내 이름이 뭐야?").call().content();
// → "철수입니다!" ✅
```

---


## 실무 패턴 — Bean으로 등록
```java
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("당신은 쇼핑몰 AI 상담원입니다.")
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(
                    new InMemoryChatMemory()
                ).build()
            )
            .build();
    }
}
```
```java
// 서비스에서 그냥 주입받아서 씀
@Service
public class ChatService {

    private final ChatClient chatClient;

    public String chat(String message) {
        return chatClient.prompt()
            .user(message)
            .call()
            .content();
    }
}
```

---

