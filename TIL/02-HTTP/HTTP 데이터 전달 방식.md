# 2025-03-17
---
# 클라이언트에서 서버로 데이터 전송

데이터 전달 방식은 크게 2가지이다. 

1. 쿼리 파라미터를 통한 데이터 전송
- **GET**
- 주로 서버로부터 데이터를 조회(읽기)하는 데 사용된다.
- 주로 정렬 필터(검색어)
- 예시: /search?query=떡&sort=popular

2. 메시지 바디를 통한 데이터 전송
  - POST, PUT, PATCH
  - 회원 가입, 상품 주문, 리소스 등록, 리소스 변경 

## 정적 데이터 조회
- 이미지, 정적 텍스트 문서
- 정적 데이터는 일반적으로 쿼리 파라미터 없이 리소스 경로로 단순하게 조회 가능
- 예시: /assets/logo.png, /docs/terms.html

## 동적 데이터 조회
- 검색 결과, 게시판 목록 등 동적으로 변화하는 데이터
- 조회 조건을 줄여주는 필터, 조회 결과를 정렬하는 정렬 조건에 주로 사용
- 조회는 GET 사용
- GET은 쿼리 파라미터 사용해서 데이터 전달
- 예시: /articles?page=1&sort=latest

---

## HTML Form Submit시 POST 전송
- 회원 가입, 상품 주문, 데이터 변경
- Content-Type: application/x-www-form-urlencoded 사용
  - form의 내용을 메시지 바디를 통해서 전송(key=value, 쿼리 파라미터 형식)
  - 전송 데이터를 url encoding 처리
    - ex) abc김 -> abc%EA%B9%80
- HTML Form은 GET 전송도 가능
- Content-Type: multipart/form-data
  - 파일 업로드 같은 바이너리 데이터 전송시 사용
  - 다른 종류의 여러 파일과 폼의 내용 함께 전송 가능

## HTTP API를 통한 데이터 전송
- 서버 to 서버
  - 백엔드 시스템 통신
- 앱 클라이언트
  - 아이폰, 안드로이드
- 웹 클라이언트
  - HTML에서 Form 전송 대신 자바 스크립트를 통한 통신에 사용(AJAX)
  - 리액트, 뷰 같은 웹 클라이언트와 API 통신
- POST, PUT, PATCH: 메시지 바디를 통해 데이터 전송
- GET: 조회, 쿼리 파라미터로 데이터 전달
- Content-Type: application/json을 주로 사용
  - TEXT, XML, JSON 등등

---

출처: 김영한의 모든 개발자를 위한 HTTP 웹 기본 지식을 참고해서 작성했습니다.
