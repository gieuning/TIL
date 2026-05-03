# RAG (Retrieval-Augmented Generation)

RAG는 **문서 + 질문을 합쳐서 프롬프트에 보내는** 방식이다.

```
[사용자 질문]
    ↓
VectorStore에서 유사 문서 검색 (similaritySearch)
    ↓
검색된 문서 + 질문 → 프롬프트로 합쳐서 LLM에 전달
    ↓
[답변 반환]
```

---

## 필요한 Bean

```java
private final ChatClient chatClient;   // 질문(LLM 호출)을 위해 사용
private final VectorStore vectorStore; // 유사 문서 검색을 위해 사용
```

---

## Document

Spring AI에서 `Document`는 VectorStore에 저장되고 검색되는 **텍스트 청크 단위**다.

```java
Document doc = new Document("텍스트 내용", Map.of("filename", "연차규정.pdf"));
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | `String` | 고유 식별자 (자동 생성) |
| `text` | `String` | 실제 텍스트 내용 |
| `metadata` | `Map<String, Object>` | 부가 정보 (파일명, document_id 등) |

- VectorStore에 저장될 때 `text`가 **임베딩 벡터로 변환**되어 함께 저장된다
- `metadata`는 벡터 변환 없이 그대로 저장되며, `filterExpression`으로 필터링에 활용할 수 있다
- `doc.getText()` / `doc.getMetadata()` / `doc.getId()` 로 접근

```java
// 검색 결과에서 꺼내 쓰는 예시
docs.stream()
    .map(doc -> String.format("[%s]: %s",
        doc.getMetadata().getOrDefault("filename", "Unknown"),
        doc.getText()))
    .collect(Collectors.joining("\n\n---\n\n"));
```

---

## similaritySearch

`VectorStore.similaritySearch()`는 질문 텍스트를 벡터로 변환한 뒤, DB에서 코사인 거리가 가까운 `Document`를 찾아 반환한다.

### SearchRequest 파라미터

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
        .query(query)                                              // 검색할 질문 텍스트 → 내부에서 벡터로 변환
        .topK(topK)                                               // 결과 최대 개수 (LIMIT N)
        .similarityThreshold(threshold)                           // 유사도 최솟값 (0.0 ~ 1.0)
        .filterExpression("document_id == '" + documentId + "'") // 메타데이터 필터 (선택)
        .build()
);
```

| 파라미터 | 설명 | 비고 |
|---|---|---|
| `query` | 검색할 질문 텍스트 | 내부에서 벡터로 변환됨 |
| `topK` | 결과 최대 개수 | SQL의 `LIMIT N`과 동일 |
| `similarityThreshold` | 유사도 최솟값 | 0.7~0.8을 많이 사용 |
| `filterExpression` | 메타데이터 기반 필터 | 특정 문서 범위로 검색 좁힐 때 |

- `threshold` **0.7** → 70% 이상 유사한 문서만 반환
- `threshold` **0.0** → 유사도 관계없이 무조건 `topK`개 반환

### 내부 동작 흐름

```
query (String)
  → EmbeddingModel → 벡터 [0.12, -0.34, ...] (3072차원)
  → PostgreSQL에 SQL 실행:

SELECT * FROM vector_store
ORDER BY embedding <=> '[0.12, -0.34, ...]'  -- 코사인 거리 (<=> 는 pgvector 연산자)
WHERE similarity >= threshold
LIMIT topK

  → Document 리스트로 변환해서 반환
```

---

## VectorStore가 DB를 인식하는 방법

Spring AI가 `DataSource`를 자동으로 주입받기 때문에 별도 설정 없이 DB에 연결된다.

```
application.yml
  spring.datasource.url: jdbc:postgresql://localhost:5432/sparta
    ↓
  DataSource Bean (Spring이 자동 생성)
    ↓
  VectorStoreConfig.java 에서 주입받음
```

---

## 전체 서비스 코드 예시

<details>
<summary>RagService.java 전체 보기</summary>

```java
@Service
@RequiredArgsConstructor
public class RagService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  private static final String RAG_PROMPT_TEMPLATE = """
      다음 문서들을 참고하여 질문에 답변해주세요.
      문서에 없는 내용은 답변하지 마세요.
      답변은 한국어로 작성해주세요.

      [참고 문서]
      %s

      [질문]
      %s

      [답변]
      """;

  // 전체 문서에서 질문
  @Transactional
  public AnswerResponse ask(String question) {
    List<Document> relevantDocs = searchDocuments(question, 5, 0.0);
    if (relevantDocs.isEmpty()) {
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
    return AnswerResponse.builder()
        .answer(generateAnswer(question, relevantDocs))
        .build();
  }

  // 특정 문서 범위 내에서 질문 (filterExpression 활용)
  @Transactional
  public AnswerResponse askInDocument(String question, String documentId) {
    List<Document> relevantDocs = searchDocumentsWithFilter(question, documentId, 5);
    if (relevantDocs.isEmpty()) {
      throw new DomainException(DomainExceptionCode.AI_RESPONSE_ERROR);
    }
    return AnswerResponse.builder()
        .answer(chatClient.prompt()
            .system("당신은 전문 문서 기반 응답 시스템입니다. 제공된 문서 내용만 사용하세요.")
            .user(String.format(RAG_PROMPT_TEMPLATE, combineDocuments(relevantDocs), question))
            .call()
            .content())
        .build();
  }

  // 답변 + 참고한 문서 출처 함께 반환
  public RagResponse askWithSource(String question) {
    List<Document> docs = searchDocuments(question, 5, 0.7);
    String answer = generateAnswer(question, docs);

    List<RagResponse.DocumentSource> sources = docs.stream()
        .map(doc -> RagResponse.DocumentSource.builder()
            .filename((String) doc.getMetadata().get("filename"))
            .documentId(doc.getId())
            .preview(doc.getText().substring(0, Math.min(doc.getText().length(), 100)))
            .build())
        .toList();

    return RagResponse.builder().answer(answer).sources(sources).build();
  }

  // LLM 없이 검색 결과만 요약
  @Transactional
  public SearchSummaryResponse getSearchSummary(String query) {
    List<Document> docs = searchDocuments(query, 5, 0.0);
    String summary = chatClient.prompt()
        .user("다음 검색 결과들을 한문장으로 요약해줘: " + combineDocuments(docs))
        .call()
        .content();
    return SearchSummaryResponse.builder().query(query).summary(summary).build();
  }

  // --- private 메서드 ---

  @Transactional
  public List<Document> searchDocuments(String query, Integer topK, Double threshold) {
    return vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(query)
            .topK(topK)
            .similarityThreshold(threshold)
            .build()
    );
  }

  public List<Document> searchDocumentsWithFilter(String query, String documentId, int topK) {
    return vectorStore.similaritySearch(
        SearchRequest.builder()
            .query(query)
            .topK(topK)
            .filterExpression("document_id == '" + documentId + "'")
            .build()
    );
  }

  private String generateAnswer(String question, List<Document> docs) {
    return chatClient.prompt()
        .user(String.format(RAG_PROMPT_TEMPLATE, combineDocuments(docs), question))
        .call()
        .content();
  }

  private String combineDocuments(List<Document> documents) {
    return documents.stream()
        .map(doc -> String.format("[%s]: %s",
            doc.getMetadata().getOrDefault("filename", "Unknown"),
            doc.getText()))
        .collect(Collectors.joining("\n\n---\n\n"));
  }
}
```

</details>