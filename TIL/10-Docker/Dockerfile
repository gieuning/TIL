# 1단계: 빌드 환경
# Gradle과 JDK가 설치된 큰 이미지를 'builder'로 지정
FROM gradle:8.5-jdk21 AS builder 

# 작업 공간은 /app
WORKDIR /app  

# 캐시 최적화를 위해 Gradle 관련 파일 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
# 💡 build.gradle 파일이 변경되지 않으면, 아래 의존성 다운로드 과정은 캐시를 사용해 건너뜁니다!

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

# 의존성 캐시 다운로드 (소스코드 없이 의존성만 미리 받아둠)
RUN ./gradlew dependencies --no-daemon

# 전체 소스 복사 후 빌드
COPY . .

# (만약을 위해) 다시 gradlew에 실행 권한 부여
RUN chmod +x gradlew

# Spring Boot JAR 빌드 (테스트는 생략하여 빌드 속도 향상)
RUN ./gradlew clean bootJar -x test --no-daemon
# 장애가 나서 테스트를 빼야 한다. -> 최악
# 장애가 아니라, 깃허브 PR 할때 테스트케이스 돼서 검증이 된 케이스야 -> 괜찬

# => /build/libs/*.jar 파일이 생성

# 2단계: 실행 환경
FROM amazoncorretto:21-alpine
# 라이브러리 및 안전성
# Java 17 실행 환경만 있는 초경량 이미지 사용

WORKDIR /app # 작업 공간은 /app

# 실행 시 사용될 프로파일 지정 (기본값: dev)
ARG PROFILE=dev # 빌드 시 `--build-arg`로 값을 받을 수 있는 변수 선언
# 아그먼튜 -> dev -> yml
ENV SPRING_PROFILES_ACTIVE=${PROFILE} 
# 컨테이너 내부 환경변수 설정

# 'builder' 스테이지에서 빌드된 JAR 파일만 복사해와서 app.jar로 이름 변경
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트를 외부에 노출할 것이라고 명시
EXPOSE 8080

# 컨테이너 시작 시 실행될 최종 명령어
ENTRYPOINT ["sh", "-c", "java -jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} app.jar"]
# 기본적으로는 CMD 같은 것
# CMD는 내용은 변경할 수 없음 -> 외부 인자를 받을 수 없음
# ENTRYPOINT는 env 실행할때 받는다.  -> 실행 시 외부 인자를 받을 수 있음