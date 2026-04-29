# Docker 이미지 Layer

## Layer 구조

Docker 이미지는 하나의 파일 덩어리가 아니라 여러 Layer가 순서대로 쌓인 구조다.

```
┌──────────────────────────────────────┐
│            Docker Image              │
├──────────────────────────────────────┤
│ Layer 5. 실행 명령                    │
│ ENTRYPOINT ["java", "-jar", "app.jar"]│
├──────────────────────────────────────┤
│ Layer 4. 애플리케이션 코드            │
│ app.jar                              │
├──────────────────────────────────────┤
│ Layer 3. 애플리케이션 의존성          │
│ 라이브러리, 설정 파일                 │
├──────────────────────────────────────┤
│ Layer 2. 실행 환경                    │
│ JVM, Java 설정                        │
├──────────────────────────────────────┤
│ Layer 1. 기본 OS 환경                 │
│ Alpine Linux / Ubuntu 등             │
└──────────────────────────────────────┘
```

Dockerfile의 명령어 하나가 하나의 Layer를 만든다.

```dockerfile
FROM amazoncorretto:21-alpine-jdk   # Layer 1 (베이스 이미지)
WORKDIR /app                         # Layer 2
COPY build/libs/*.jar app.jar        # Layer 3
ENTRYPOINT ["java", "-jar", "app.jar"] # Layer 4
```

---

## Dockerfile 주요 명령어

```dockerfile
# 1. 베이스 이미지 선택
FROM python:3.9

# 2. 작업 디렉터리 설정
WORKDIR /app

# 3. 의존성 파일만 먼저 복사
COPY requirements.txt .

# 4. 패키지 설치
RUN pip install -r requirements.txt

# 5. 소스 코드 복사
COPY . .

# 6. 컨테이너 실행 시 기본 명령어
CMD ["python", "app.py"]
```

| 명령어 | 역할 |
|---|---|
| `FROM` | 어떤 이미지 위에서 시작할지 지정. 모든 Dockerfile의 첫 줄. `python:3.9`처럼 OS + 런타임이 미리 세팅된 이미지를 베이스로 쓴다. |
| `WORKDIR` | 컨테이너 내부의 작업 디렉터리 지정. 이후 명령은 이 경로를 기준으로 실행된다. 디렉터리가 없으면 자동 생성. |
| `COPY` | 호스트 파일을 이미지 안으로 복사. `COPY 호스트경로 컨테이너경로` 형식. `COPY . .`은 현재 폴더 전체를 WORKDIR로 복사. |
| `RUN` | 이미지 빌드 중 실행할 명령어. 결과가 새 Layer로 저장된다. 패키지 설치, 파일 생성 등에 사용. |
| `CMD` | 컨테이너 실행 시 기본으로 실행할 명령어. `docker run` 시 덮어쓸 수 있다. |
| `ENTRYPOINT` | 컨테이너 실행 시 고정으로 실행할 명령어. `docker run`으로 덮어쓰기 어렵다. 실행 파일을 고정할 때 사용. |

### CMD vs ENTRYPOINT

```
CMD ["python", "app.py"]
→ docker run my-image bash  # bash로 덮어쓰기 가능

ENTRYPOINT ["python", "app.py"]
→ docker run my-image bash  # python app.py bash 로 실행됨 (덮어쓰기 안 됨)
```

> 실행 명령을 고정해야 하면 `ENTRYPOINT`, 기본값만 지정하고 유연하게 쓰려면 `CMD`.

---

## Layer 공유

여러 이미지가 동일한 베이스 Layer를 공유할 수 있다.

```
Image A                 Image B
┌──────────────┐        ┌──────────────┐
│ app-a.jar    │        │ app-b.jar    │
├──────────────┤        ├──────────────┤
│   Java 21    │        │   Java 21    │  ← 공유
├──────────────┤        ├──────────────┤
│ Alpine Linux │        │ Alpine Linux │  ← 공유
└──────────────┘        └──────────────┘
```

공유 Layer는 디스크에 한 번만 저장된다. `docker pull` 시에도 이미 가진 Layer는 다시 받지 않는다.

---

## Layer 캐싱

Docker는 이전 빌드와 명령어 및 입력 파일이 같으면 기존 Layer를 재사용한다.

```
빌드 1회차: 모든 Layer 새로 생성
빌드 2회차: 변경된 Layer부터 아래만 재빌드, 위는 캐시 재사용
```

**핵심:** 특정 Layer가 변경되면 그 아래 모든 Layer가 무효화된다.

```
FROM ... (캐시)
COPY requirements.txt . (캐시)
RUN pip install ... (캐시)
COPY . .  ← 코드 변경 시 여기서 캐시 깨짐
CMD ...   ← 재실행
```

따라서 **자주 변경되지 않는 것을 위에, 자주 바뀌는 코드는 아래에** 배치해야 캐시를 최대한 활용할 수 있다.