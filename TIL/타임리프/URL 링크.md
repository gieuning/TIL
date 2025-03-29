# 2025-03-29
---

# 🌐 타임리프 URL 링크 표현식 (@{...})

> 타임리프에서 URL을 동적으로 생성할 때는 **`@{...}` 문법을 사용** 컨트롤러 경로, 쿼리 파라미터, 경로 변수까지 모두 표현이 가능하다.

---

## ✅ 기본 문법

| 문법                              | 설명                          | 결과 예시                             |
|-----------------------------------|-------------------------------|----------------------------------------|
| `@{/hello}`                       | 기본 경로                     | `/hello`                               |
| `@{/hello(param1=${val})}`        | 쿼리 파라미터 추가            | `/hello?param1=data`                   |
| `@{/hello/{id}(id=${user.id})}`   | 경로 변수                     | `/hello/5`                             |
| `@{/hello/{id}(id=${id}, type='A')}` | 경로 변수 + 쿼리 파라미터 | `/hello/5?type=A`                      |

- `()`에 있는 부분은 쿼리 파라미터로 처리된다.
- URL 경로상에 변수가 있으면 `()`` 부분은 경로 변수로 처리된다.
- 경로 변수와 쿼리 파라미터를 함께 사용할 수 있다.  
---

## ✅ 컨트롤러 예시

```java
@GetMapping("/link")
public String link(Model model) {
    model.addAttribute("param1", "data1");
    model.addAttribute("param2", "data2");
    return "basic/link";
}
```

```html
<ul>
  <!-- 기본 URL -->
  <li><a th:href="@{/hello}">기본 경로</a></li>

  <!-- 쿼리 파라미터 -->
  <li><a th:href="@{/hello(param1=${param1}, param2=${param2})}">쿼리 파라미터</a></li>

  <!-- 경로 변수 -->
  <li><a th:href="@{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}">경로 변수</a></li>

  <!-- 경로 변수 + 쿼리 파라미터 -->
  <li><a th:href="@{/hello/{param1}(param1=${param1}, param2=${param2})}">경로 + 쿼리</a></li>
</ul>
```

---
참고: https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#link-urls