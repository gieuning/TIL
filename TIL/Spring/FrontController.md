# 2025-01
---

# 오늘 공부 내용

<details>
  <summary>예시 코드 보기</summary>

  ```java
  //여기에 코드를 작성

   ```

</details>

## FrontController 패턴 특징
- 프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받음
- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출
- 입구를 하나로 하면서 공통 처리 가능
- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 된다.


요청 파라미터 정보는 자바의 Map으로 대신 넘기도록 한다.
request 객체를 Model로 사용하는 대신에 별도의 Model 객체를 만들어서 반환 

Model도 request.setAttribute()를 통해 데이터를 저장하고 뷰에 전달
서블릿의 종속성을 제거하기 위해 Model을 직접 만들고, 추가로 view 이름까지 전달하는 객체를 생성


## viewResolver
- 컨트롤러가 반환한 논리 뷰 이름을 실제 물리 뷰 경로로 변경한다. 그리고 실제 물리 경로가 있는 MyView 객체를 반환
```java
private static MyView viewResolver(String viewName) {
    return new MyView("/WEB-INF/views/" + viewName + ".jsp");
}
```
- 논리 뷰 이름 : `members`
- 물리 뷰 경로 : "/WEB-INF/views/members.jsp"


- Http sublist request 있는 파라미터를 다 뽑아서 paramMap에 담는다. 
```java
private static Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();
    request.getParameterNames().asIterator().forEachRemaining(paramName -> request.getParameter(paramName));
    return paramMap;
}
```

model에 담긴 정보를 request.setAttribute(key, valuie)를 통해 전부 담는다. 

- JSP는 `request.getAttribute()`로 데이터를 조회하기 때문에, 모델의 데이터를 꺼내서 `request.setAttribute()`로 담아둔다.   
```java
 private static void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
    model.forEach((key, value) -> request.setAttribute(key, value));
  }
```



## 주요 구성 요소 및 동작 흐름
**클라이언트 요청**
클라이언트(사용자 또는 브라우저)가 HTTP 요청을 보냅니다.

**Front Controller**

요청을 처리하는 진입점 역할을 합니다.
Spring에서는 DispatcherServlet이 Front Controller 역할을 합니다.
요청 URL을 기반으로 매핑 정보를 조회하여 적합한 컨트롤러를 찾아 호출합니다.

**컨트롤러 (Controller)**

비즈니스 로직을 처리하거나 필요한 데이터를 준비합니다.
V4 구조에서 컨트롤러는 **ModelView 객체를 반환하지 않고, ViewName(뷰 이름)**만 반환합니다.
예를 들어, return "userForm";와 같은 방식으로 View의 이름을 반환합니다.
동시에 모델 데이터(Model)를 Front Controller에 전달합니다.

**ViewResolver 호출**

Front Controller는 컨트롤러가 반환한 ViewName(뷰 이름)을 이용해 ViewResolver를 호출합니다.
ViewResolver는 뷰 이름을 기반으로 실제 View 객체(MyView)를 생성하거나 반환합니다.
Spring에서는 주로 InternalResourceViewResolver를 통해 JSP와 같은 뷰를 연결합니다.
MyView 반환

ViewResolver는 최종적으로 View 객체를 반환합니다.
예를 들어, "userForm"이라는 ViewName이 "/WEB-INF/views/userForm.jsp"로 매핑됩니다.
View 렌더링

Front Controller는 View 객체(MyView)를 호출하고, 함께 전달받은 모델 데이터를 바탕으로 HTML을 렌더링합니다.
최종적으로 클라이언트에게 완성된 HTML 응답이 반환됩니

- 핸들러 어댑터: 중간에 어댑터 역할을 하는 어댑터가 추가되었는데 이름이 핸들러 어댇ㅂ터이다.
여기서 어댑터 역할을 해주는 덕분에 다양한 종류의 컨트롤러를 호출할 수 있다. 

- 핸들러: 컨트롤러의 이름을 더 넓은 범위인 핸들러로 변경했다. 그 이유는 이제 어댑터가 있기 때문에
꼭 컨트롤러의 개념 뿐만 아니라 어떠한 것이든 해당하는 종류의 어댑터만 있으면 다 처리할 수 있다. 