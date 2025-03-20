# 2025-03-20
---

# HTTP 헤더
HTTP 헤더는 **클라이언트(브라우저)와 서버 간의 요청(Request) 및 응답(Response)에 포함되는 메타데이터**이다.  
헤더를 통해 **콘텐츠 형식, 인증 정보, 캐싱 정책, 쿠키, 보안 설정 등 다양한 정보를 주고받을 수 있다.**
- header-filed = filed-name ":" OWS filed-value OWS (OWS:띄어쓰기 허용)
---

## HTTP 헤더 용도
- HTTP 전송에 필요한 모든 부가정보
- 메시지 바디의 내용, 메시지 바디의 크기, 압축, 인증, 요청 클라이언트, 서버 정보, 캐시 관리 정보 등
- 표준 헤더가 너무 많다.
- 필요시 임의의 헤더 추가 가능 

## HTTP 헤더의 종류

HTTP 헤더는 크게 **4가지 유형**으로 분류된다.

| 헤더 유형 | 설명 | 예제 |
|----------|-----------------------------|-----------------------------|
| **일반 헤더** | 요청과 응답 모두에서 사용 | `Connection: keep-alive` |
| **요청 헤더** | 클라이언트가 서버로 요청할 때 포함 | `User-Agent: Mozilla/5.0` |
| **응답 헤더** | 서버가 클라이언트로 응답할 때 포함 | `Server: Apache/2.4.41` |
| **엔터티 헤더** | 본문(Body)과 관련된 정보 포함 | `Content-Type: application/json` |

---

## HTTP BODY(RFC2616 - 과거)
- 메시지 본문은 엔티티 본문을 전달하는데 사용
- 엔티티 본문은 요청이나 응답에서 전달할 실제 데이터
  - 메시지 본문 안에 엔티티 본문을 담아서 전송한다고 이해하자
- 엔티티 헤더는 엔티티 본문의 데이터를 해석할 수 있는 정보 제공
  - 데이터 유형(html, json), 데이터 길이, 압축 정보 등등
```http request
HTTP/1.1 200 OK
# 엔티티 헤더 시작
Content-Type: text/html;charset=UTF-8 # 메시지 본문에 들어가는 text인지 html인지 
Content-Length: 3423 # 본문의 크기를 명시 
# 엔티티 헤더 종료

# 메시지 본문, 엔티티 본문
<html>
    <body> ... </body>
</html>
```

## RFC723x 변화
- 엔티티(Entity) -> 표현(Representation)
- 표현(Representation) = 표현 메타데이터(Representation Metadata) + 표현 데이터 (Representation Data)
---

## HTTP BODY (REC7230) - HTTP/1.1의 메시지 구조를 정의하는 문서 
- 메시지 본문 = 페이로드
- 메시지 본문을 통해 표현 데이터 전달
  - 메시지 본문에는 JSON, XML, HTML, 이미지, 바이너리 파일 등 다양한 데이터가 포함될 수 있다. 
- 표현은 요청이나 응답에서 전달할 실제 데이터
- 표현 헤더는 표현 데이터를 해석할 수 있는 정보 제공
  - 데이터 유형(html, json), 데이터 길이, 압축 정보 등등
---
## 표현
- HTTP 요청(Request) 또는 응답(Response)에서 전달되는 실제 데이터
- 표현(Representation) = 표현 메타데이터(Representation Metadata) + 표현 데이터 (Representation Data)

### 표현 헤더
- 표현 데이터를 해석할 수 있도록 정보를 제공

| 헤더               | 설명 | 예제                                    |
|------------------|----|---------------------------------------|
| Content-Type     |  표현 데이터의 형식  | Content-Type: text/html;charset=UTF-8 |
| Content-Encoding |  표현 데이터의 압축 방식  | Content-Encoding: gzip                |
| Content-Language |  표현 데이터의 자연 언어  | Content-Language: KO                  |
| Content-Length   | 표현 데이터의 길이   | Content-Length: 3423                  |
---
## 협상(콘텐츠 네고시에이션) - 클라이언트가 선호하는 표현 요청

- HTTP에서 클라이언트가 원하는 형식의 데이터를 요청 할 수 있도록 하는 기능
- 서버는 클라이언트의 요청을 참고하여 가장 적절한 데이터를 응답한다.
- 예) 클라이언트가 JSON 필요 !!!, 브라우저가 한국어 콘텐츠를 원해 !!!, 서버는 클라이언트가 원하는 형식으로 응답을 보내준다.
---

| 헤더 | 설명 | 예제 |
|------|------------------------------|------------------------------|
| **Accept** | 클라이언트가 원하는 응답 타입 (MIME) | `Accept: application/json` |
| **Accept-Language** | 클라이언트가 선호하는 언어 | `Accept-Language: ko-KR, en-US` |
| **Accept-Encoding** | 클라이언트가 지원하는 인코딩 방식 | `Accept-Encoding: gzip, deflate` |
| **Content-Type** | 서버가 제공하는 응답의 데이터 유형 | `Content-Type: text/html; charset=UTF-8` |
---

### 협상과 우선순위1 - Quality Values(q)
HTTP 콘텐츠 협상에서 클라이언트가 원하는 형식의 우선순위를 정하는 방법

---
- Quality Values(q) 값 사용
- 0 ~ 1, 클수록 높은 우선순위를 가진다. (생략 할 경우 1)
- Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
  - 우선순위: ko-KR (1.0) > ko (0.9) > en-US (0.8) > en (0.7)

### 협상과 우선순위2
클라이언트가 서버에 요청할 때, 가장 구체적인 형식이 높은 우선순위를 가진다.

---
- 더 구체적인 미디어 타입이 우선
- accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
  - text/html → 가장 우선 (HTML 페이지 요청이므로)
  - application/xhtml+xml → HTML이 없으면 XHTML 제공
  - image/avif, image/webp, image/apng → 이미지 포맷 요청 가능
  - application/xml;q=0.9 → XML이 필요할 경우
  - application/signed-exchange;v=b3;q=0.7 → 가장 낮은 우선순위
  - */*;q=0.8 → 위 모든 형식이 없으면 다른 모든 형식 허용

## 전송방식 
- 단순 전송 - Content-Length 방식 (Content의 길이를 알고 있을 때)
- 압축 전송 - Content-Encoding: gzip
- 분할 전송 - Transfer-Encoding: chunked
  - Content-Length를 넣으면 안된다 -> 길이가 예상이 안되기 때문에 
- 범위 전송 - Content-Range: bytes 1001-2000 / 2000

## 일반 정보 (정보성 헤더)
- 정보성 헤더는 클라이언트 또는 서버에 대한 추가적인 정보를 제공하는 헤더 
- 주로 요청 또는 응답에서 사용되며, 로그 분석, 보안, 트래픽 분석 등에 활용된다.
---

| 헤더 | 설명 | 사용 위치 |
|------|---------------------------------|----------|
| **From** | 유저 에이전트의 이메일 정보 (잘 사용되지 않음) | 요청(Request) |
| **Referer** | 이전 웹 페이지 주소 (유입 경로 분석) | 요청(Request) |
| **User-Agent** | 클라이언트 애플리케이션 정보 | 요청(Request) |
| **Server** | 요청을 처리한 서버의 소프트웨어 정보 | 응답(Response) |
| **Date** | 메시지가 생성된 날짜 | 응답(Response) |
---

### From
- 보안 및 개인정보 보호 문제로 인해 일반적으로 잘 사용되지 않는다.
- 검색 엔진 같은 곳에서 주로 사용하며 요청에서 사용한다.
- **검색 엔진 크롤러(bot) 등이 주로 사용**하여 사이트 소유자에게 연락할 정보를 제공한다.

---

### Referer - 이전 웹 페이지 주소
- 현재 요청된 페이지의 이전 웹 페이지 주소를 포함
- A 페이지 → B 페이지로 이동하면, B 페이지 요청 시 Referer: A 포함됨
- 유입 경로 분석, 보안(출처 확인), 광고 추적 등에 사용
  - 내 웹사이트를 어느 사이트를 통해서 들어왔지? -> 데이터 분석시 사용  
- 브라우저의 프라이버시 보호 모드에서는 Referer가 생략될 수 있음

---

### User-Agent - 유저 에이전트 애플리케이션 정보
- 클라이언트(웹 브라우저, 모바일 앱 등)의 정보를 포함
- 운영체제(OS), 브라우저 종류, 버전 등의 정보 포함
- 서버에서 클라이언트 유형을 식별하여 최적의 응답을 제공할 때 사용
  - 어떤 종류의 브라우저에서 장애가 발생하는지 파악 가능(서버에서 유용)

```http request
GET / HTTP/1.1
Host: example.com
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/109.0.0.0 Safari/537.36
```

--- 
## Server - 서버 소프트웨어 
- 요청을 처리한 웹 서버의 소프트웨어 정보를 제공
- 서버의 종류(Apache, Nginx, IIS 등)와 버전 포함
- 보안상의 이유로 이 정보를 숨기거나 수정하는 경우가 많다.

```http request
HTTP/1.1 200 OK
Server: Apache/2.4.41 (Ubuntu)
```

### Date - 메시지 생성 날짜
- HTTP 메시지가 생성된 날자와 시간을 포함
- 캐싱, 로그 분석 등에 사용된다.

---

## 특별한 정보
일부 HTTP 헤더는 특정한 상황에서 중요한 역할을 수행하며, 리다이렉트, 접근 제어, 재시도 정책 등에 사용된다.
여기서는 Host, Location, Allow, Retry-After 헤더의 역할과 활용 방법을 정리한다. 

| 헤더 | 설명                    | 사용 위치 |
|------|-----------------------|----------|
| **Host** | 요청한 호스트 정보(도메인)       | 요청(Request) |
| **Location** | 리다이렉션할 새 URL을 지정      | 응답(Response) |
| **Allow** | 리소스가 허용하는 HTTP 메서드 목록 | 응답(Response) |
| **Retry-After** | 클라이언트가 다시 요청해야 하는 시간  | 응답(Response) |

---

### Host
- 요청을 보낼 서버의 도메인 정보를 포함
- 하나의 서버가 여러 도메인을 처리해야 할 때
- 하나의 IP 주소에 여러 도메인이 적용되어 있을 때
- - **HTTP/1.1 이상에서는 필수 헤더**이며, 누락 시 `400 Bad Request` 오류 발생

### Location
- 클라이언트를 새로운 URL로 리다이렉트할 때 사용
- 웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동

### Allow
- 특정 리소스가 지원하는 HTTP 메서드 목록을 반환
- 405 (Method Not Allowed) 에서 응답에 포함해야한다.  

### Retry-After
- 서버가 일시적으로 요청을 처리할 수 없을 때, 클라이언트에게 다시 요청해야 하는 시간을 안내
- 주로 503 Service Unavailable, 429 Too Many Requests 응답과 함께 사용된다.

---

## 인증
웹에서 보안이 중요한 페이지나 API 요청을 처리할 때, 인증(Authorization)이 필요하다. <br/>
이때, HTTP에서는 **`Authorization`**과 **`WWW-Authenticate`** 헤더를 사용하여 인증을 수행한다.

| 헤더 | 설명                   | 사용 위치 |
|------|----------------------|----------|
| **Authorization** | 클라이언트가 서버에 인증 정보를 전달 | 요청(Request) |
| **WWW-Authenticate** | 리소스 접근시 필요한 인증 방법 정  | 응답(Response) |

---

