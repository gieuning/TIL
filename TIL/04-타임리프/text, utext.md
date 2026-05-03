# 2025-03-23
---

# 텍스트 - text, utext
타임리프트이 가장 기본 기능인 텍스트를 출력하는 기능

> 타임리프는 기본적으로 HTMl 태그의 속성에 기능을 정의해서 동작한다. HTML의 콘텐츠에 데이터를 출력 할 때는 다음과 같이
`th:text`를 사용한다. -> `<span th:text="${data}">`

HTML 태그의 속성이 아니라 HTML 콘텐츠 영역안에서 직접 데이터를 출력하고 싶으면 다음과 같이 `[[...]]`를 사용
- 컨텐츠 안에서 직접 출력하기 = `[[${data}]]`

---

## Escape
HTML에서 `<`, `>`, `&` 같은 특수 문자를 **문자 그대로 출력**되도록 바꾸는 것을 말한다.
예를 들어 사용자가 입력한 문자열에 `<b>`가 포함되어 있다면, 브라우저는 이를 **굵은 글씨 태그**로 해석한다. 
하지만 Escape 처리를 하면 그저 `<b>`라는 **문자**로 보이게 된다.


## HTML 엔티티
HTML에서 특수 문자를 **문자 그대로** 출력하기 위해 사용하는 것

| 문자   | HTML 엔티티 |
|--------|-------------|
| `<`    | `&lt;`      |
| `>`    | `&gt;`      |
| `&`    | `&amp;`     |
| `"`    | `&quot;`    |
| `'`    | `&apos;`    |

---

## Unescape 
- 타임리프는 아래 기능을 제공

| 표현 방식         | Escape 여부 | 설명                          |
|------------------|-------------|-------------------------------|
| `th:text`        | ✅ O         | HTML 특수 문자 자동 escape     |
| `[[${...}]]`     | ✅ O         | 텍스트 콘텐츠 영역에서 escape  |
| `th:utext`       | ❌ X         | **Escape 하지 않음**, HTML 해석 |
| `[(${...})]`     | ❌ X         | **Escape 하지 않음**, HTML 해석 |

---

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

    <h1>text vs utext</h1>

    <ul>
        <li>th:text = <span th:text="${data}"></span> </li>
        <li>th:utext = <span th:utext="${data}"></span> </li>
    </ul>

    <h1><span th:inline="none">[[...]] vs [(...)]</span></h1>
    <ul>
        <li><span th:inline="none">[[...]]</span>[[${data}]]</li>
        <li><span th:inline="none">[[...]]</span>[(${data})]]</li>
    </ul>
</body>
</html>
```

**결과**
```html
text vs utext
th:text = <b> Hello Spring! </b>
th:utext = Hello Spring!
[[...]] vs [(...)]
[[...]]<b> Hello Spring! </b>
[[...]] Hello Spring! ]
```

---
escape를 사용하지 않아서 HTML이 정상 렌더링 되지 않는 수 많은 문제가 발생하니 escape를 기본적으로 하고,
꼭 필요할 때만 unescape를 사용해야 한다. 
