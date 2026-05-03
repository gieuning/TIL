기본 구조

[동사] + By + [조건1] + [연산자] + [조건2] + ...

  ---
1. 동사 (접두어)

┌──────────┬──────────────────────────────┐
│  메서드  │             의미             │
├──────────┼──────────────────────────────┤
│ findBy   │ SELECT (List 또는 Optional)  │
├──────────┼──────────────────────────────┤
│ existsBy │ SELECT EXISTS (boolean 반환) │
├──────────┼──────────────────────────────┤
│ countBy  │ SELECT COUNT (long 반환)     │
├──────────┼──────────────────────────────┤
│ deleteBy │ DELETE                       │
└──────────┴──────────────────────────────┘

  ---
2. 조건 키워드

┌──────────────┬─────────────┬────────────────────────┐
│    키워드    │     SQL     │          예시          │
├──────────────┼─────────────┼────────────────────────┤
│ And          │ AND         │ findByNameAndStatus    │
├──────────────┼─────────────┼────────────────────────┤
│ Or           │ OR          │ findByNameOrEmail      │
├──────────────┼─────────────┼────────────────────────┤
│ Not          │ !=          │ findByStatusNot        │
├──────────────┼─────────────┼────────────────────────┤
│ Like         │ LIKE        │ findByNameLike         │
├──────────────┼─────────────┼────────────────────────┤
│ Containing   │ LIKE '%값%' │ findByNameContaining   │
├──────────────┼─────────────┼────────────────────────┤
│ StartingWith │ LIKE '값%'  │ findByNameStartingWith │
├──────────────┼─────────────┼────────────────────────┤
│ In           │ IN (...)    │ findByStatusIn         │
├──────────────┼─────────────┼────────────────────────┤                                      
│ IsNull       │ IS NULL     │ findByDeletedAtIsNull  │
├──────────────┼─────────────┼────────────────────────┤                                      
│ Between      │ BETWEEN     │ findByCreatedAtBetween │
├──────────────┼─────────────┼────────────────────────┤                                      
│ GreaterThan  │ >           │ findByAgeGreaterThan   │
├──────────────┼─────────────┼────────────────────────┤                                      
│ LessThan     │ <           │ findByAgeLessThan      │
└──────────────┴─────────────┴────────────────────────┘
   
---                                                                                          
3. 연관 객체 탐색 (_)

// ChatMessage 엔티티 안에
@ManyToOne                                                                                   
private ChatConversation conversation;  // conversation 객체

// conversation 안에
private UUID id;

// _ 로 연관 객체 필드 탐색
findByConversation_IdAndStatus(UUID id, StatusType status)                                   
// → WHERE conversation_id = ? AND status = ?

// _ 없이도 추론 가능하지만 명확하지 않음
findByConversationIdAndStatus(...)  // 동작하지만 모호함
                                                                                               
---
4. 파라미터 바인딩 규칙

이름이 쿼리 구조, 파라미터 순서가 값 바인딩

findByConversation_IdAndStatus(UUID id, StatusType status)
//                    ↑ 1번째 ?          ↑ 2번째 ?

순서가 틀리면 런타임 오류 발생:                                                              
// ❌ 순서 바뀌면 타입 불일치 오류
findByConversation_IdAndStatus(StatusType status, UUID id)
                                                                                               
---
5. 대소문자 규칙

- 필드명 첫 글자는 반드시 대문자 (카멜케이스로 토큰 구분)
- 키워드(And, Or, Not 등)도 대문자 시작

findByConversationIdAndStatus   // ✅                                                         
findByconversationIdAndstatus   // ❌ 파싱 실패
                                                                                               
---
6. 정렬 / 페이징

findByStatusOrderByCreatedAtDesc(StatusType status)
// → WHERE status = ? ORDER BY created_at DESC

findByStatus(StatusType status, Pageable pageable)                                           
// → WHERE status = ? + 페이징
                                                                                               
---             
핵심 요약

▎ Spring Data JPA는 메서드 이름만 보고 SQL을 자동 생성한다.
▎ 인자값은 이름에 등장한 조건 순서대로 바인딩된다.                                           
▎ 복잡한 쿼리는 @Query로 직접 작성하는 게 낫다.    