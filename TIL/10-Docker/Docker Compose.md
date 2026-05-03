# Docker Compose 개념
- 여러 개의 컨테이너를 하나의 서비스로 관리하는 도구
- YAML 파일로 컨테이너어 정의 및 실행 가능
- 개발 및 배포 환경에서 다중 컨테이너 애플리케이션을 쉽게 설정 가능

Docker Compose는 주로 개발 및 테스트 환경에서 사용된다.

## 1.Docker Compose 설치 확인
```shell
docker-compose --version 
```

## 2.Docker Compose 파일 작성 (docker.compose.yml)
```yaml
services:
  web:
    image: nginx:alpine
    ports:
      - "8080:80"
  redis:
    image: redis:alpine
```

- 실행 명령어
```shell
docker-compose up -d
```

## 3.컨테이너 중지 및 삭제
```shell
docker-compose down
```


Docker 리소스 강제 정리
```shell
# 실행 중인 모든 컨테이너 강제 중지 및 삭제
docker rm -f $(docker ps -aq)

# 사용하지 않는 모든 리소스 청소
docker system prune -a --volumnes
```

### 3. Docker Compose 실전 활용 예제

Spring boot + MySQL 기반 웹 애플리케이션 실행.

**3.1 Dockerfile**

```docker
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**3.2 docker-compose.yml**

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      # DB 접속 정보를 환경 변수로 설정 (application.properties보다 우선순위가 높음)
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/mydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - db

  db:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mydb
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```


**3.3 실행 명령어**

```bash
# 스프링 빌드
./gradlew clean build -x test
```

```bash
# Dockerfile & Compose 빌드
docker-compose up --build -d
```

**3.4 웹 애플리케이션 테스트**

```
curl http://localhost:8080
```

**3.6  컨테이너 중지 및 정리**

```
docker-compose down -v
```

Docker Compose를 활용하여 다중 컨테이너 기반의 애플리케이션을 쉽게 관리할 수 있다.