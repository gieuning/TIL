# 2025-03-17
---

## POST - 신규 자원 등록 특징  
- 클라이언트는 서버에 그냥 요청하는 것 
  - 나 이거 회원 등록해줘 
- 클라이언트는 등록될 리소스의 URI를 모른다.
  - 회원 등록 /members -> POST
  - POST /members
- 서버가 새로 등록된 리소스 URI를 생성해준다. (서버가 리소스 URI 결정)
  - 내가 /members POST로 보내고 회원 등록할 데이터를 넘기면 서버가 알아서 회원의 아이디를 만들고 리소스 URI에 대한 경로도 서버에서 판단해서 만들어서 내려준다.
```http request 
HTTP/1.1 201 Created 
Location: /members/100  # 리소스 URI에 대한 경로 서버가 알아서 만들어줌
```  
- 컬렉션
  - 서버가 관리하는 리소스 디렉토리
  - 서버가 리소스의 URI를 생성하고 관리
  - 여기서 컬렉션은 /members  

## 파일 관리 시스템 - PUT 기반 등록
- 클라이언트가 리소스 URI을 알고 있어야 한다.
  - 파일 등록 /files{filename} -> PUT
  - PUT /files/star.jpg
- 클라이언트가 직접 리소스의 URI를 지정 (클라이언트가 리소스 URI를 결정)
- 스토어(Store)
  - 클라이언트가 관리하는 리소스 저장소 
  - 클라이언트가 리소스의 URI를 알고 관리
  - 여기서 스토어는 /files 

파일 업로드 하면 기존 파일을 지우고 다시 올려야 하기 때문에 `PUT`이 적합 
- 파일 목록 /files -> GET
- 파일 조회 /files/{filename} -> GET
- 파일 등록 /files/{filename} -> PUT
- 파일 삭제 /files/{filename} -> DELETE
- 파일 대량 등록 /files -> POST 

대부분 POST 신규 자원 등록을 쓴다. (컬렉션) PUT은 비중은 거의 없다.

## HTMl FORM 사용
- HTML FORM은 GET, POST만 지원
- AJAX 같은 기술을 사용해서 해결 가능 
- 컨트롤 URI
  - GET, POST만 지원하므로 제약이 있다.
  - 이런 제약을 해결하기 위해 동사로 된 리소스 경로 사용
  - POST의 /new, /edit, /delete가 컨트롤 URI
  - HTTP 메서드로 해결하기 애매한 경우 사용(HTTP API 포함)

HTML FORM 설계

- 회원 목록 /members -> GET
- 회원 등록 폼 /members/new -> GET
- 회원 등록 /members/new, /members -> POST
- 회원 조회 /members/{id} -> GET
- 회원 수정 폼 /members/{id}/edit -> GET
- 회원 수정 /members/{id}/edit, /members/{id} -> POST
- 회원 삭제 /members/{id}/delete -> POST 

---

# 정리
참고하면 좋은 URI 설계 개념 
• 문서(document)
    • 단일 개념(파일 하나, 객체 인스턴스, 데이터베이스 row)
    • 예) /members/100, /files/star.jpg

• 컬렉션(collection) - POST
    • 서버가 관리하는 리소스 디렉터리
    • 서버가 리소스의 URI를 생성하고 관리
    • 예) /members

• 스토어(store) - PUT
    • 클라이언트가 관리하는 자원 저장소
    • 클라이언트가 리소스의 URI를 알고 관리
    • 예) /files

• 컨트롤러(controller), 컨트롤 URI
    • 문서, 컬렉션, 스토어로 해결하기 어려운 추가 프로세스 실행
    • 동사를 직접 사용
    • 예) /members/{id}/delete

`https://restfulapi.net/resource-naming`


---
이 문서는 김영한님의 HTTP 강의를 참고하여 만들었습니다.