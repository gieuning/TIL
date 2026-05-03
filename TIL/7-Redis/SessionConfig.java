// 세션 데이터 직렬화 설정

// 기본적으로 세션 속성은 Java 직렬화 방식으로 저장되어,
// Redis CLI에서 조회 시 사람이 읽기 어려운 바이트 스트림 형태로 출력.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession // Redis를 세션 저장소로 사용하도록 활성화
public class SessionConfig {

  @Bean
  public GenericJackson2JsonRedisSerializer springSessionDefaultRedisSerializer() {
    // 세션 속성 값을 Java 직렬화 대신 JSON 형태로 저장하도록 설정
    return new GenericJackson2JsonRedisSerializer();
  }
}
