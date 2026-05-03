2. env에는 “환경마다 바뀌는 값”을 넣습니다
   대표적으로:

- SHOP_API_BASE_URL
- SHOP_API_KEY
- SHOP_API_TIMEOUT_MS
- SHOP_API_SECRET
- SHOP_API_USERNAME
- SHOP_API_PASSWORD

즉 외부 API 붙을 때 바뀔 가능성이 있는 값들입니다.

3. 실무에서 특히 env로 빼는 것

- 서버 주소
- API Key / Secret / Token
- DB 접속정보
- Redis, S3, OAuth 관련 키
- profile별로 달라지는 값

반대로 env로 안 빼는 것도 있어요.

- 거의 안 바뀌는 내부 상수
- 코드 의미를 설명하는 설정 구조 자체
- 너무 사소한 값들

보통 기준은 이거예요.

- 바뀔 수 있으면 env
- 민감하면 무조건 env
- 환경(dev/stage/prod)마다 다르면 env

실무 조합은 대체로 이 패턴이 제일 많습니다.

추천 조합

- application.yml: 설정 키 구조 정의
- 환경변수: 실제 값 주입
- Java: @ConfigurationProperties로 바인딩

예를 들면:

external:
shop:
base-url: ${SHOP_API_BASE_URL}
api-key: ${SHOP_API_KEY}

그리고 env:

SHOP_API_BASE_URL=https://api.shop.com
SHOP_API_KEY=xxxxx

이 방식이 좋은 이유는:

- yml은 읽기 좋고
- env는 배포환경별 관리가 쉽고
- 코드에서는 타입 안전하게 묶어서 쓸 수 있음

추가로 실무 팁 하나만 말하면,
external도 많이 쓰지만 더 명확하게 clients, integrations, apis 같은 이름도 자주 씁니다.


내부 외부 불일치 상태가 생긴것 
내부에서 문제가 생긴거니 
외부에 보상 트랜잭션을 요청 

외부 상품 재고는 차감 -> 내부에서 차감 실패 

내부에서 바로 복구작업 시작 

핵심은 서로 불일치 상태는 안만든다
정석으로는 내부 차감 먼저 

내부에서 차감 하는것이 왜 정석이냐면 -> 우리가 핸들링 하기 쉬운걸 되돌리고
되돌리기 어려운건 나중에 

---
외부API 호출을 트랜잭션 밖으로 빼야한다. 

외부 응답을 기다리는 동안 DB락을 잡는다. 

부분적으로 작업시키는 작업들은 -> 트랜잭션 애노테이션에 required_new로 작업해준다. 

가장 최상단이 롤백되기 위해서 -> 독립적 트랜잭션으로 본다. 




@Transactional 동작 방식 공부,
트랜잭션 전파 속성
트랜잭션 격리 수준

-추가
Outbox Pattern



분산 아키텍터 설계 

@Transactional 사용 때 분리한 메서드가 같은 클래스 안에 있고 this로 호출할 때 주의하기
< 요것도 공부해보세요 (힌트 키워드 : 프록시 )












--------------
이렇게 오니까 DTO도 먼저 이 껍데기대로 만들어야 합니다.

실전 꿀팁만 딱 정리하면:

JSON을 먼저 복붙해서 구조를 눈으로 쪼개기
맨 위 key부터 한 단계씩 적어보세요.
최상위: result, error, message
message 안: contents, pageable
contents 안: 상품 객체 필드들
이렇게 보면 DTO도 자연스럽게 중첩 구조로 나옵니다.

“배열인지 객체인지” 먼저 보기
이걸 제일 많이 헷갈려요.
{} 이면 객체 -> 클래스
[] 이면 리스트 -> List<...>
예:

message는 객체
contents는 배열
그래서 message는 클래스 하나
contents는 List<ProductDto>
이름이 안 맞으면 @JsonProperty 쓰기
자바 필드명과 JSON 키가 다를 때만 붙이면 됩니다.
예:

@JsonProperty("is_orderable")
Boolean isOrderable;
처음엔 필요한 필드만 만들어도 됨
응답이 20개 필드여도 내가 5개만 쓰면 일단 그 5개만 만들어도 Jackson은 잘 읽습니다.
처음부터 전부 만들려고 하면 더 헷갈려요.

DTO는 “계층대로” 만든다고 생각하기
예를 들어 이런 JSON이면:

{
"data": {
"user": {
"name": "kim"
}
}
}
DTO도 거의 그대로 갑니다.

class ResponseDto {
Data data;
}

class Data {
User user;
}

class User {
String name;
}
헷갈리면 “응답 전체 DTO”와 “실제 데이터 DTO”를 분리하기
보통 이렇게 나누면 편해요.
응답 wrapper DTO
실제 item DTO
예:

ExternalResponse
ExternalMessage
ExternalProductResponse
Postman/curl 응답 기준으로 만들기
Swagger 설명 말고, 실제 응답 본문을 보고 만드는 게 제일 정확해요.
문서랑 실제 응답이 다른 경우가 꽤 많습니다.

안 확신 서면 String으로 먼저 받아보기
진짜 막힐 땐 Feign/RestTemplate 응답을 일단 문자열로 찍어보고 DTO를 맞추면 빨라요.
“내가 생각한 JSON”이랑 “실제 JSON”이 다른 경우가 많거든요.

이번 케이스에 적용하면 사고 흐름은 이렇게였어요.

{
"result": true,
"error": {...},
"message": {
"contents": [...],
"pageable": {...}
}
}
그러면 DTO는:

ExternalResponse
Boolean result
ExternalError error
ExternalMessage message
ExternalMessage
List<ExternalProductResponse> contents
ExternalPageable pageable
이런 식으로 따라가면 됩니다.

한 줄 팁으로 압축하면:
“DTO는 내가 쓰기 편하게 만드는 게 아니라, 상대 JSON 계층 구조를 그대로 옮긴 뒤 필요한 필드만 남긴다”예요.

원하면 내가 다음 답변에서
“JSON 보고 DTO 만드는 3단계 공식”
이걸 네 코드 스타일에 맞춰 진짜 외우기 쉽게 정리해줄게.