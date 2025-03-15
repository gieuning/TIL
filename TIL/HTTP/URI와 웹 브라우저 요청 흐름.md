# 2025-03-16
---

# URI(Uniform Resource Identifier)
- 인터넷에서 특정 리소스를 식별하기 위한 **문자열 형식의 주소 체계**이다.
- 웹에서 문서, 이미지, 동영상, API 엔드포인트 등을 식별하는 데 사용
- URI는 **URL(Uniform Resource Locator)**과 **URN(Uniform Resource Name)**을 포함하는 개념.

- 리소스를 통합하는 식별 방법

자원 자체를 식별 

URL(Uniform Resource Locator)
- 리소스 위치(주소)를 나타내는 URI
- 웹 브라우저에서 입력하는 주소가 URL

URN(Uniform Resource Name)
- 리소스의 이름을 나타내는 URI(위치 정보는 포함하지 않음)
- 특정 네임스페이스에서 리소스를 고유하게 식별
  - urn:isbn:0451450523 (도서번호)

**위치는 변할 수 있지만, 이름은 변하지 않는다.**

## URL 전체 문법
- scheme://[userinfo@]host[:port][/path][?query][#fragment]
- `https://www.example.com:443/products/item123?color=blue#reviews`

- 프로토콜(https) - scheme
- 호스트명(www.example.com) 
- 포트 번호(443)
- 패스 (/products/item123)
- 쿼리 파라미터 (?color=blue)


## 브라우저 요청(Request) 흐름

1. **URL 입력**
    - 사용자가 브라우저 주소창에 URL을 입력하여 요청 시작

2. **DNS 조회**
    - 브라우저는 도메인 이름을 IP 주소로 변환하기 위해 DNS 서버에 요청
    - **예시**: `www.example.com` → `93.184.216.34`

3. **TCP 연결 설정 (3-Way Handshake)**
    - 브라우저가 서버와 **TCP 연결을 맺음**
    - 클라이언트 → 서버: `SYN`
    - 서버 → 클라이언트: `SYN+ACK`
    - 클라이언트 → 서버: `ACK`

4. **HTTP 요청(Request) 전송**
    - 연결이 맺어진 후 브라우저가 서버에 HTTP 요청 전송
   ```http
   GET /index.html HTTP/1.1
   Host: www.example.com
   ```