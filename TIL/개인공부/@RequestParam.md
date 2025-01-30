# @RequestParam
- HTTP 요청의 `Query String` 또는 `Form 데이터`를 개별 파라미터로 바인딩하는 기능을 제공하는 애노테이션
- 주로 `GET` 요청의 쿼리 파라미터나 `POST` 요청의 폼 데이터를 컨트롤러 메서드의 매개변수로 받을 때 사용

## @RequestPram 사용
**GET 요청에서 사용**
- 요청 예시 `GET /search?keyword=프로틴&page=2`
```java
@GetMapping("/search")
public String search(@RequestParam String keyword, @RequestParam int page) {
    System.out.println("검색어: " + keyword);
    System.out.println("페이지 번호: " + page);
    return "searchResults";
}
```
- `keyword="프로틴"` -> `String keyword`에 매핑
- `pagre=2` -> `int page`에 매핑

**POST 요청에서 사용(폼 데이터 바인딩)**
- 요청 예시
```html
<form action="/add" method="post">
    <input type="text" name="itemName" value="프로틴">
    <input type="number" name="price" value="2000">
    <input type="number" name="quantity" value="10">
    <button type="submit">추가</button>
</form>
```

```java
@PostMapping("/add")
public String addItem(@RequestParam String itemName, 
                      @RequestParam int price, 
                      @RequestParam int quantity) {
    System.out.println("상품명: " + itemName);
    System.out.println("가격: " + price);
    System.out.println("수량: " + quantity);
    return "itemAdded";
}
```


## @RequestParam의 주요 옵션
1. required = false (필수 파라미터 해제)
- 해당 파라미터가 없어도 요청이 정상적으로 처리 
- 기본 타입 사용 시 예외 발생
```java
@GetMapping("/search")
public String search(@RequestParam(required = false) String keyword) {
    System.out.println("검색어: " + keyword);
    return "searchResults";
}
```
- 클라이언트가 `keyword`를 보내지 않아도 정상적으로 실행 
- `keyword` 값이 `null`이 된다. 
2. defaultValue 
- 파라미터가 없을 경우 기본값을 설정 가능
```java
@GetMapping("/search")
public String search(@RequestParam(defaultValue = "프로틴") String keyword, 
                     @RequestParam(defaultValue = "1") int page) {
    System.out.println("검색어: " + keyword);
    System.out.println("페이지 번호: " + page);
    return "searchResults";
}
```

## @RequestParam을 사용할 때 주의할 점
- 기본적으로 @RequestParam은 필수 값
  - → required = false 또는 defaultValue를 설정하면 예외 방지 가능.
- 데이터 타입을 맞춰야 함
  - → 예를 들어, @RequestParam int price에 price=abc가 들어오면 변환 에러 발생.
- 배열 또는 리스트로 받을 때 URL 형식 주의
  - → ?category=프로틴&category=마프&category=단백질 형태로 넘겨야 함.


## 정리
- 개별적으로 파라미터를 사용 할 때는 `@RequestParam`
- 객체 바인딩 `@ModelAttribute`