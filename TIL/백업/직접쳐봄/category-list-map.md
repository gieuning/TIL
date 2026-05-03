# List 변환과 Map 사용 기준

## 단순 목록 조회

카테고리를 단순 목록으로 반환할 때는 `Map`이 필요 없다.

예상 응답:

```json
[
  {
    "id": 1,
    "name": "음식",
    "parentId": null
  },
  {
    "id": 2,
    "name": "한식",
    "parentId": 1
  }
]
```

이 경우에는 `List<Category>`를 `List<CategoryResponse>`로 변환하면 된다.

```java
@Transactional(readOnly = true)
public List<CategoryResponse> findAll() {
  List<Category> categories = categoryRepository.findAll();

  return categories.stream()
      .map(this::toResponse)
      .toList();
}
```

`map(this::toResponse)`는 각 `Category`를 `CategoryResponse`로 변환한다는 뜻이다.

```text
List<Category>
→ Category 하나씩 꺼냄
→ CategoryResponse로 변환
→ List<CategoryResponse> 반환
```

---

## Map이 필요한 경우

`Map`은 나중에 특정 데이터를 빠르게 다시 찾아야 할 때 사용한다.

핵심 기준:

```text
id로 다시 꺼내 쓸 일이 있으면 Map을 만든다.
```

예를 들어 부모 카테고리를 찾아야 할 때:

```java
CategoryResponse parentResponse =
    categoryResponseMap.get(category.getParent().getId());
```

`Map`을 쓰면 key로 바로 찾을 수 있다.

```text
1 -> 음식 Response
2 -> 한식 Response
3 -> 중식 Response
```

`parentId`가 `1`이면 `음식 Response`를 바로 꺼낼 수 있다.

---

## 카테고리 트리 구조

카테고리를 부모-자식 구조로 반환하려면 `Map`을 사용하는 것이 좋다.

예상 응답:

```json
[
  {
    "id": 1,
    "name": "음식",
    "childCategories": [
      {
        "id": 2,
        "name": "한식",
        "childCategories": []
      }
    ]
  }
]
```

이 경우에는 자식 카테고리를 부모 카테고리의 `childCategories`에 넣어야 한다.

```java
Map<Long, CategoryResponse> categoryResponseMap = new HashMap<>();

for (Category category : categories) {
  CategoryResponse response = CategoryResponse.builder()
      .id(category.getId())
      .name(category.getName())
      .childCategories(new ArrayList<>())
      .build();

  categoryResponseMap.put(category.getId(), response);
}
```

먼저 모든 카테고리를 `Map`에 넣어둔다.

그 다음 다시 반복하면서 부모가 있으면 부모 응답 객체를 꺼내 자식 목록에 추가한다.

```java
List<CategoryResponse> rootCategories = new ArrayList<>();

for (Category category : categories) {
  CategoryResponse categoryResponse = categoryResponseMap.get(category.getId());

  if (category.getParent() == null) {
    rootCategories.add(categoryResponse);
  } else {
    CategoryResponse parentResponse =
        categoryResponseMap.get(category.getParent().getId());

    if (parentResponse != null) {
      parentResponse.getChildCategories().add(categoryResponse);
    }
  }
}

return rootCategories;
```

---

## 정리

| 상황 | 추천 방식 |
|------|-----------|
| 단순 목록 반환 | `stream().map()` 또는 일반 `for`문 |
| DTO로 변환만 함 | `Map` 필요 없음 |
| 부모-자식 트리 구성 | `Map<Long, CategoryResponse>` 사용 |
| 반복문 안에서 id로 계속 찾아야 함 | `Map` 사용 |
| Redis에 `category:{id}`처럼 key-value로 저장 | `Map` 또는 key-value 구조가 유용 |

결론:

```text
단순히 변환해서 반환하면 List만 사용한다.
관계를 연결하거나 id로 빠르게 다시 찾아야 하면 Map을 사용한다.
```


이미지를 말아올려서 컨테이너를 띄운다.
인스턴스에서 승격?

EC2 인스턴스?
개발계 이미지 발사 -> 개발계 테스트 성공 -> 배포에 바로 올리나

도커 허브 -> 깃으로 코드 받듣이
깃허브에 접근해서 코드를 받는다.
도커 허브, 바로 받아올수있다.
인스턴스 접근 후에 도커 허브에서 pull 땡겨서 이미지를 올린다. 


요새는 도커 이미지 말아 올려서 발사 -> 컨테이너 시킨다
경량화 되기도함

도커허브는 무료로 쓰면 public 이미지
유료로 해야만 private
이미지 파일을 가져가서 코드를 깔 수 있다. -> 조심

도커파일은 이미지 를 어떻게 말아올리지 설계하는것이고

그 안에 모든걸 다 가쳐져있다.

이미지 파일 만든건 서버에 대한 파일이 다 있는것

이미지 파일은 도커런을 한뒤 컨테이너로 뒤운다.

도커파일이라는게 빌드그레이들?

빌드스크립트고 도커파일이라는데

스프링부트로 만든 로직들이 -> 이미지

이미지 = 프로세스(프로그램, 프로젝트)

이미지 파일을 바로 컨테이너화 시켜서 실행가능

도커파일은 스크립트

하나의 이미지로 하나의 컨테이로 뛰운다

하나의 이미지로 여러개의 컨테이너도 가능

여러개 이미지 하나의 컨테이너 불가

하나의 이미지를 가지고 여러개 실행 가능

이미지 하나가 = 프로그램

