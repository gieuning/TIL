# Spring AI - PgVector 벡터 스토어

> PostgreSQL의 pgvector 확장을 사용해 임베딩 벡터를 저장하고 유사도 기반 검색을 수행한다.

## 의존성

```gradle
dependencyManagement {
  imports {
    mavenBom "org.springframework.ai:spring-ai-bom:1.0.0"
  }
}

dependencies {
  implementation 'org.springframework.ai:spring-ai-starter-model-openai'
  implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
}
```

## DB 세팅

```shell
docker run -d --name local-postgres --restart always \
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=sparta \
  -p 5432:5432 pgvector/pgvector:pg16
```

```sql
-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 설치 확인
SELECT * FROM pg_extension WHERE extname = 'vector';
```

## 테이블

```sql
CREATE TABLE items (
  id serial PRIMARY KEY,
  content text,
  embedding vector(1536)  -- 1536차원 벡터 저장
);

-- 유사도 기반 검색 (L2 거리가 가장 가까운 5개)
SELECT content FROM items
ORDER BY embedding <-> '[0.12, 0.05, ...]'
LIMIT 5;
```

## application.yml

```yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration
  ai:
    openai:
      api-key: ${GOOGLE_AI_GEMINI_API_KEY}
      embedding:
        base-url: https://generativelanguage.googleapis.com/v1beta/openai/
        options:
          model: gemini-embedding-001
      chat:
        base-url: "https://generativelanguage.googleapis.com/v1beta/openai/"
        completions-path: "/chat/completions"
        options:
          model: "gemini-2.5-flash-lite"
          temperature: 0.7
    vectorstore:
      pgvector:
        initialize-schema: false       # 직접 SQL로 테이블 관리 시 false
        dimensions: 3072               # 임베딩 모델 차원 수
        distance-type: COSINE_DISTANCE # 유사도 계산 방식
        index-type: hnsw               # 고속 검색 인덱스
```

## VectorStoreConfig

```java
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.vectorstore.pgvector")
public class VectorStoreConfig {

  private int dimensions;

  @Bean
  public VectorStore vectorStore(DataSource dataSource, EmbeddingModel embeddingModel) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        // 임베딩 벡터의 차원 수를 설정합니다.
        .dimensions(dimensions

        // 벡터 간의 유사도를 계산하는 방식을 설정합니다.
        .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)

        // 검색 속도를 높이기 위한 인덱스 알고리즘을 설정합니다.
        // HNSW는 대규모 데이터셋에서 빠르고 정확한 근사 최근접 이웃 검색을 지원합니다.
        .indexType(PgVectorStore.PgIndexType.HNSW)
            
        // 애플리케이션 시작 시 자동으로 테이블 스키마를 생성할지 여부를 결정합니다.
        // 직접 SQL로 테이블을 관리하므로 false로 설정하여 기존 구조를 유지합니다.
        .initializeSchema(false)
    
        // 기존에 존재하는 벡터 테이블을 삭제하고 새로 만들지 설정합니다.
        // 데이터 유실 방지를 위해 false로 설정하는 것이 안전합니다.
        .removeExistingVectorStoreTable(false)
    
        // 시작 시 DB 테이블의 컬럼 구성이나 차원이 설정값과 일치하는지 검증합니다.
        // 차원 설정과 실제 DB의 vector 타입 일치 여부를 체크합니다.
        .vectorTableValidationsEnabled(true)
    
        // 데이터가 저장될 PostgreSQL의 스키마 이름을 지정합니다.
        // 별도의 커스텀 스키마를 쓰지 않는다면 기본값인 "public"을 사용합니다.
        .schemaName("public")
    
        // 벡터 데이터가 실제로 저장될 테이블의 이름을 지정합니다.
        // 여기서는 직접 생성하신 "vector_store" 테이블과 연결됩니다.
        .vectorTableName("vector_store")
        .build();
  }
}
```

### 주요 옵션

| 옵션 | 설명 |
|------|------|
| `dimensions` | 임베딩 벡터 차원 수. 모델과 반드시 일치해야 함 |
| `distanceType` | 유사도 계산 방식. `COSINE_DISTANCE` / `L2_DISTANCE` / `INNER_PRODUCT` |
| `indexType` | 검색 인덱스. `HNSW`는 대규모 데이터에서 빠른 근사 최근접 이웃 검색 지원 |
| `initializeSchema` | `false` → 직접 SQL로 테이블 관리. `true` → 앱 시작 시 자동 생성 |
| `removeExistingVectorStoreTable` | `true`면 시작 시 테이블 삭제 후 재생성. 데이터 유실 주의 |
| `vectorTableValidationsEnabled` | 시작 시 DB 테이블 차원과 설정값 일치 여부 검증 |

---