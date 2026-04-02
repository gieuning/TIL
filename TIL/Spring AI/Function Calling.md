# Function Calling이란?

LLM이 단순히 텍스트를 생성하는 것을 넘어, 외부 시스템이나 특정 도구를 직접 호출할 수 있게 해주는 기술
(API, 데이터베이스, 계산기 등)

LLM이 스스로 판단해서 "이 질문에 답하려면 외부 도구의 도움이 필요해!" 라고 결정하고,
개발자가 준비한 함수를 실행할 수 있는 매개변수를 추출해주는 과정

-> LLM이 직접 코드를 실행하는 게 아니라, 어떤 함수를 어떤 파라미터로 실행해야 할지 "설계도"를 그려주는 것

---

## 동작 흐름

```
1. 개발자가 함수 정의 (실제 로직 작성)
2. LLM에 함수 등록
3. 사용자 질문 입력 → "서울 날씨 알려줘"
4. LLM 판단 → "날씨 조회 함수가 필요하겠다, city = 서울"
5. 함수 실행 → getWeather(city = "서울")
6. 결과를 LLM에 전달 → 최종 답변 생성
```

---

## 코드로 보면

**Tool 등록**

```java
@Tool(description = "특정 도시의 현재 날씨 정보를 조회합니다")
public WeatherResponse getWeather(WeatherRequest request) {
    // 실제 날씨 API 호출 로직
}
```

`@Tool`의 `description`을 LLM이 읽고 어떤 함수를 써야 할지 판단함

-> description을 잘 써야 LLM이 정확하게 함수를 선택함

**ChatClient에 등록**

```java
chatClient
    .prompt()
    .user(userMessage)
    .tools(functionTools) // 등록
    .call()
    .content();
```

---

## LLM이 함수를 고르는 기준

`description`을 보고 유사한 것을 찾아서 판단
`request`는 JSON 파싱이 가능한 객체여야 함 (`@NoArgsConstructor` 필요)

```
유저: "서울에 현재 날씨를 알려줘"
    ↓
LLM: description 훑어봄
    ↓
"특정 도시의 현재 날씨 정보를 조회합니다" → 이거네
    ↓
WeatherRequest의 city 파라미터 → "서울" 채워서 실행
```

---

## MCP (Model Context Protocol)

Function Calling을 표준화한 플러그인 방식
-> 다양한 외부 도구를 LLM에 연결하는 공통 규격