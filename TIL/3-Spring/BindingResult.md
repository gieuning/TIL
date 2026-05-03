# 2025-04-07
---

# BindingResult - 바인딩된 결과값 담기
> `BindingResult`는 Spring에서 제공하는 검증 오류를 보관하는 인터페이스로, 폼 데이터 바인딩과 검증 과정에서 발생하는 오류를 처리하는 핵심 컴포넌트이다.
- 스프링이 제공하는 검증 오류를 보관하는 객체

---

**주요 특징**
- 오류 정보 보관: 데이터 바인딩 및 검증 과정에서 발생한 오류를 저장하낟.
- 예외 처리: `@ModelAttribute`에 데이터 바인딩 오류가 발생해도 컨트롤러가 정상 호출이 된다.
- 자동 모델 추가: `BindingResult`는 자동으로 모델에 추가되어 뷰에서 오류 정보에 접근할 수 있다.

---

**주의사항**
- 파라미터 순서: `BindingResult`는 반드시 `@ModelAttribute` 바로 다음에 위치해야 한다.
- 바인딩 실패 처리: `BindingResult`가 없으면 바인딩 오류 시 400 오류가 발생하고 컨트롤러가 호출되지 않는다.

---

**코드 예시**
```java
@PostMapping("/add")
public String addItem(@ModelAttribute Item item, BindingResult bindingResult) {
    // 검증 로직
    if (!StringUtils.hasText(item.getItemName())) {
        bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
    }
    
    // 검증에 실패하면 다시 입력 폼으로
    if (bindingResult.hasErrors()) {
        return "form";
    }
    
    // 성공 로직
    return "redirect:/success";
}
```
---

**타임리프 스프링 검증 오류 통합 기능**
타임리프는 스프링의 `BindingResult`를 활용해서 편리하게 검증 오류를 표현하는 기능을 제공
- `#fields`: `#fields`로 `BindingResult`가 제공하는 검증 오류에 접근할 수 있다.
- `th:errors`: 해당 필드에 오류가 있는 경우에 태그를 출력한다. `th:if`의 편의 버전이다.
- `th:errorclass`: `th:field`에서 지정한 필드에 오류가 있으면 `class` 정보를 추가한다.

**작동 원리**
1. `ArgumentResolver`가 호출되면서 파라미터가 바인딩된다.

2. 바인딩 과정에서 오류가 발생하면 `BindingResult`에 오류 정보가 담긴다.

3. `@ModelAttribute`는 필드 단위로 바인딩되므로, 일부 필드에 오류가 있어도 다른 필드는 정상적으로 바인딩한다.

4. 컨트롤러에서 `bindingResult.hasErrors()`로 오류 여부를 확인할 수 있다.

---
`BindingResult`는 스프링 MVC의 검증 시스템에서 중요한 역할을 하며, 사용자 입력 오류를 효과적으로 처리하고
피드백을 제공하는 데 필수적인 도구이다.