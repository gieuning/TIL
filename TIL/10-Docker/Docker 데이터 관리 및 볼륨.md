# Docker 데이터 관리 및 볼륨

## Docker 볼륨이란?
- 컨테이너 내부의 데이터가 사라지는 문제를 해결하기 위해 사용
- 호스트 시스템의 특정 경로에 데이터를 저장하여 지속성 제공
- 컨테이너를 삭제해도 데이터는 유지

---

## 스토리지 유형

| 유형 | 설명 | 데이터 지속성 | 사용 사례 |
|------|------|-------------|----------|
| Container Storage | 컨테이너 내부 임시 저장소 | 컨테이너 삭제 시 유실 | 테스트 환경, 임시 데이터 |
| Bind Mounts | 호스트 디렉토리를 컨테이너에 직접 마운트 | 호스트에 의존, 컨테이너보다 오래 지속 | 개발 환경, 설정/로그 파일 |
| Volumes | Docker가 관리하는 독립 저장소 | 컨테이너와 독립적으로 지속 | 프로덕션 DB, 중요 데이터 |
| Tmpfs Mount | 메모리에 저장 | 컨테이너 재시작 시 유실 | 민감한 임시 데이터, 캐시 |

- **Container Storage** — 컨테이너 생명주기와 동일. 삭제되면 데이터도 함께 사라짐
- **Bind Mounts** — 호스트 파일/디렉토리를 컨테이너와 실시간 공유. 개발할 때 주로 사용
- **Volumes** — Docker가 직접 관리. 컨테이너가 삭제되어도 볼륨 데이터는 유지
- **Tmpfs Mounts** — RAM 기반으로 속도가 매우 빠름. 컨테이너 종료 시 데이터 소멸

> **Container Storage**가 기본값이며, 스테이트리스 앱(API 서버 등)은 그대로 사용해도 무방.  
> DB나 파일 업로드처럼 **컨테이너를 교체해도 데이터가 살아있어야 하는 경우**에 **볼륨**을 사용

---

## 운영 환경에서는 어떤 방식을 쓸까?

앱의 성격에 따라 다르다.

| 서비스 유형 | 권장 방식 | 이유 |
|-----------|----------|------|
| API 서버, 스테이트리스 앱 | Container Storage (기본값) | 상태를 들고 있지 않아 컨테이너가 교체되어도 무방 |
| DB (MySQL, PostgreSQL 등) | Volumes | 컨테이너 교체 후에도 데이터 유지 필요 |
| 설정 파일, 인증서 | Bind Mounts 또는 Volumes | 호스트에서 직접 관리하거나 Docker가 관리 |
| 세션, 임시 캐시 | Tmpfs Mount | 빠른 속도가 필요하고 재시작 시 초기화되어도 무방 |

- Kubernetes, Docker Swarm 같은 오케스트레이션 환경에서는 **볼륨**이 핵심
  - 컨테이너가 다른 노드로 이동하거나 재배포되어도 데이터가 유지되어야 하기 때문
- 결국 **"이 데이터가 컨테이너 없이도 살아있어야 하는가"** 를 기준으로 판단

---

## Volumes 실습

### 볼륨 생성 및 컨테이너에 마운트

```bash
# 볼륨 생성
docker volume create my_volume

# 볼륨 확인
docker volume ls | grep my_volume

# 볼륨을 마운트하여 컨테이너 실행
docker run -d --name vol_test -v my_volume:/data busybox tail -f /dev/null

# 볼륨 상세 정보 확인
docker inspect my_volume

# 컨테이너 내부에 데이터 저장
docker exec -it vol_test sh -c "echo 'Hello Volume' > /data/test.txt"

# 컨테이너 삭제
docker rm -f vol_test

# 새 컨테이너로 동일 볼륨 마운트
docker run -d --name vol_test -v my_volume:/data busybox tail -f /dev/null

# 데이터 유지 확인
docker exec -it vol_test cat /data/test.txt
# Hello Volume
```

---

## Bind Mounts 실습

### 호스트 디렉토리를 컨테이너에 마운트

```bash
mkdir -p docker_data

echo "Hello Bind Mount" > docker_data/hello.txt

docker run --rm -v ./docker_data:/data busybox cat /data/hello.txt
# Hello Bind Mount
```

---

## Tmpfs Mount 실습

- 메모리에 데이터를 저장하므로 디스크 기반 스토리지보다 I/O 성능이 빠름
- 컨테이너가 종료되면 데이터 소멸 → 민감한 정보가 디스크에 남지 않아 보안에 유리

```bash
# tmpfs 마운트로 컨테이너 실행
docker run -d \
  --name tmpfs-test \
  --mount type=tmpfs,destination=/app,tmpfs-size=1000000 \
  ubuntu
```

| 옵션 | 설명 |
|------|------|
| `type=tmpfs` | 마운트 타입을 tmpfs로 설정 |
| `destination=/app` | 컨테이너 내 마운트 경로 |
| `tmpfs-size=1000000` | 크기를 바이트 단위로 설정 (약 1MB) |

### 실습 — 속도 측정 및 데이터 유실 확인

**Dockerfile**

```dockerfile
FROM ubuntu:latest

RUN apt-get update && apt-get install -y time vim

CMD ["bash"]
```

```bash
# 이미지 빌드
docker build -t ubuntu-tmpfs .

# tmpfs 마운트로 컨테이너 실행
docker run -it --name tmpfs-test1 \
  --mount type=tmpfs,destination=/app,tmpfs-size=1000000 \
  ubuntu-tmpfs

# 컨테이너 내부에서 10MB 파일 생성 후 읽기 속도 측정
cd /app
dd if=/dev/zero of=tempfile bs=1M count=10
time cat tempfile > /dev/null

# 컨테이너 재시작 후 데이터 유실 확인
# /app 디렉토리 내 파일이 사라진 것을 확인
```

---

## Volume vs Bind Mount 비교

| 특징 | Docker Volume | Bind Mount |
|------|--------------|------------|
| 저장 위치 | `/var/lib/docker/volumes/` (Docker 관리) | 지정된 호스트 디렉토리 |
| 컨테이너 독립성 | 컨테이너 삭제 후에도 데이터 유지 | 컨테이너와 직접 연결 |
| 권한 관리 | Docker가 관리하여 더 안전 | 호스트 파일 시스템 권한 필요 |
| 적합한 환경 | 프로덕션 (DB, 로그) | 로컬 개발, 설정 파일 공유 |