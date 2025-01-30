# @ModelAttribute 
- 요청 데티러르 객체로 바인딩하는 기능을 제공하는 애노테이션
- 특히 폼, 데이터를 받아서 객체를 자동으로 생성하고, 해당 객체를 Model에 추가하는 역할
- 자동으로 `Model`에 객체 추가
  - `@ModelAttribute`를 사용하면 자동으로 `Model`에 해당 객체를 추가해 뷰에서 접근 가능하게 만든다.

## @ModelAttribute 동작
```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item) {
    itemRepository.save(item);
    return "basic/item";
}
```
- `Item` 객체를 자동 새성 - `new Item()`
- HTTP 요청 파라미터(itemName, price, quantity) 값을 객체 필드에 매핑 - setter 호출
- `Model`에 객체를 자동 추가
    - 기본적으로 `model.addAttribute("item", item);` 코드가 자동 실핼

## @ModelAttribute 생략 가능 
- 때로는 생략도 좋지만, 생략을 안 하는것도 좋다. <br>

**명시적으로 사용**
```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item) {
    itemRepository.save(item);
    return "basic/item";
}
```

**생략 가능**
```java
@PostMapping("/add")
public String addItem(Item item) {
    itemRepository.save(item);
    return "basic/item";
}
```

<details>
    <summary>@ModelAttribute 예제 </summary>

```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item) {
    itemRepository.save(item);
    return "basic/item";
}
```
- `Item` 객체를 자동 새성 - `new Item()`
- HTTP 요청 파라미터(itemName, price, quantity) 값을 객체 필드에 매핑 - setter 호출
- `Model`에 객체를 자동 추가
  - 기본적으로 `model.addAttribute("item", item);` 코드가 자동 실핼

```html
<form action="/add" method="post">
    <input type="text" name="itemName" value="떡">
    <input type="number" name="price" value="5000">
    <input type="number" name="quantity" value="10">
    <button type="submit">추가</button>
</form>
```
- 이 폼이 전송되면, Spring이 자동으로 아래와 같은 객체를 생성

```java
Item item = new Item();
item.setItemName("떡");
item.setPrice(5000);
item.setQuantity(10);
```

</details>

## 정리
✅ @ModelAttribute의 핵심 기능:
- 요청 데이터를 객체에 자동 바인딩 (Item item)
- 자동으로 Model에 추가 (model.addAttribute("item", item); 생략 가능)
- 메서드 레벨에서 공통 데이터를 Model에 추가 가능

✅ @ModelAttribute vs @RequestParam
- @ModelAttribute: 폼 데이터를 객체로 변환 (자동 바인딩)
**@ModelAttribute**
```java
//  @PostMapping("/add")
  public String addItemV2(@ModelAttribute("item") Item item) {
    /*
    @ModelAttribute을 사용하므로서 Item 객체를 만들어주고 set 작업까지 전부 다 해준다.
    Item item = new Item();
    item.setItemName(itemName);
    item.setPrice(price);
    item.setQuantity(quantity);
    */

    itemRepository.save(item);

//    model.addAttribute("item", item); 자동으로 만들어짐

    return "basic/item";
  }
```
- @RequestParam: 단일 값 바인딩 (String, int 등 기본 타입)

**@RequestParam**
```java
public String addItemV1(@RequestParam("itemName")String itemName,
                      @RequestParam("price") int price,
                      @RequestParam("quantity") Integer quantity,
                      Model model) {
    Item item = new Item();
    item.setItemName(itemName);
    item.setPrice(price);
    item.setQuantity(quantity);
    
    itemRepository.save(item);
    
    model.addAttribute("item", item);
    
    return "basic/item";
}
```
- 필드마다 `@RequestParam`을 붙여 개별적으로 값을 가져와야 한다. 

✅ @ModelAttribute 생략 가능
- 매개변수에 객체 타입을 사용하면 Spring이 자동 적용.
