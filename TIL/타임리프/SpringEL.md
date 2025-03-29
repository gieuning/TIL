# 2025-03-29
---

# SpringEL
> 타임리프에서 `${...}` 문법은 스프링에서 제공하는 표현식 언어로, 변수 접근, 메서드 호출, 연산, 컬렉션 처리 등이 가능하다.

## 기본 문법 - ${...}
- HTML에서 모델에 담긴 데이터를 꺼내서 출력할 때 사용
```html
<span th:text="${user.username}"></span> <!-- user.username 출력 -->
```

## SpringEL 사용 가능한 대상

| 표현식               | 설명                                              |
|----------------------|---------------------------------------------------|
| `${변수명}`          | 모델에 담긴 객체에 접근                           |
| `${객체.필드}`       | 객체의 필드 값에 접근 (`user.name`)              |
| `${객체.메서드()}`   | 메서드 호출 (`user.getUsername()`)               |
| `${리스트[0]}`       | 리스트나 배열의 인덱스 접근                       |
| `${맵['key']}`       | Map에서 key로 값 접근                             |
| `${param.name}`      | 요청 파라미터 (`request.getParameter`)            |
| `${session.name}`    | 세션 값 (`session.getAttribute`)                  |
| `${application.name}`| 서블릿 컨텍스트(Context) 접근                    |
| `${@beanName}`       | 스프링 빈 호출 (`@helloBean.hello('Spring')`)    |
| `${#dates.now()}`    | 유틸리티 객체 사용 (`#dates`, `#strings` 등)      |

**예시코드**
```java
@GetMapping("/example")
public String example(Model model) {
    User user = new User("기은", 25);
    model.addAttribute("user", user);
    model.addAttribute("users", List.of(user, new User("지민", 20)));

    Map<String, String> map = Map.of("key", "value");
    model.addAttribute("myMap", map);

    return "example";
}
```

```html
<!-- 필드 접근 -->
<p th:text="${user.username}"></p>

<!-- 메서드 호출 -->
<p th:text="${user.getUsername()}"></p>

<!-- 리스트 -->
<p th:text="${users[0].username}"></p>

<!-- Map -->
<p th:text="${myMap['key']}"></p>

<!-- 연산 -->
<p th:text="${user.age + 5}"></p> <!-- 30 -->

<!-- 삼항 연산 -->
<p th:text="${user.age > 20 ? '성인' : '미성년자'}"></p>

<!-- 스프링 빈 호출 -->
<p th:text="${@helloBean.hello('기은')}"></p>

<!-- 유틸리티 객체 -->
<p th:text="${#dates.format(#dates.createNow(), 'yyyy-MM-dd')}"></p>
```

### 지역 변수 선언
- `th:with`는 임시 변수를 선언할 때 사용한다. 조건문이나 특정 데이터 조작이 필요할 때 유용하다.

```html
<h1>지역 변수 - (th:with)</h1>
<div th:with="first=${users[0]}">
    <p>처음 사람의 이름은 <span th:text="${first.username}"></span></p>
</div>
```

- first는 지역 변수로, 해당 `<div>` 내부에서만 유효
