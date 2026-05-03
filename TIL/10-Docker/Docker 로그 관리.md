# Docker 로그 관리

- 도커 컨테이너의 로그 생성 방식과 관리 방법
- 로그 스트림, 로그 수집 및 포워딩, 로깅 드라이버 등을 다룸

> **alpine vs busybox**  
> alpine — 매우 가볍고 네트워크 도구 없음 (핀 강통)  
> busybox — 네트워크 설정 및 기본 유틸리티 포함

---

## 기본 로그 확인

### 컨테이너 실행 및 로그 생성

```bash
docker run --name log-test -d alpine sh -c '
  for i in $(seq 1 10); do
    echo "$i: stdout log"
    echo "$i: stderr log" >&2
  done'
```

- `echo "..." >&2` — 표준 에러(stderr)로 메시지 전송

### 로그 확인 명령어

```bash
# 모든 로그 출력
docker logs log-test

# 최근 5개 출력
docker logs --tail 5 log-test

# 실시간 스트림
docker logs -f log-test

# 타임스탬프 포함
docker logs --timestamps log-test
```

> 개발 환경에서는 로그 명령어를 자주 씀  
> 운영 환경에서는 직접 `docker logs`로 핸들링하지 않음 → 통합 로그 수집 도구 사용

### 컨테이너 정리

```bash
docker stop log-test && docker rm log-test
```

---

## 컨테이너 로그 수집 및 통합 관리

운영 환경에서는 서버가 10대면 컨테이너는 2~30개 — 하나씩 보는 건 불가능하므로 **통합 관리**가 필요

### ELK + 스프링 방식 (서비스 레벨 로그)

```
스프링 서버 → 실시간 스트리밍 → ELK
```

- ELK가 수집하는 건 `docker logs`가 아니라, **도커 안에서 실행되는 스프링 로그**
- 스프링이 실시간으로 ELK에 스트리밍하는 구조

### syslog 드라이버 방식 (시스템 레벨 로그)

가상화된 컨테이너 로그들을 한 통속으로 묶어서 수집할 때 사용

```bash
# 로그를 외부 syslog 서버로 전송
docker run --log-driver=syslog \
  --log-opt syslog-address=udp://localhost:514 \
  -d ubuntu echo "Logging to syslog"
```

- 로그를 수집할 컨테이너(syslog 서버)를 별도로 띄워둠
- 각 컨테이너에서 UDP로 해당 호스트 주소로 로그를 전송
- 한 곳에서 여러 컨테이너 로그를 한 번에 확인 가능

| 방식 | 수집 대상 | 사용 시점 |
|------|----------|----------|
| ELK + 스프링 | 애플리케이션 로그 (request, response, 쿼리) | 서비스 레벨 모니터링 |
| syslog 드라이버 | 시스템/OS 레벨 컨테이너 로그 | 인프라 레벨 모니터링 |

---

## 로그 로테이션

기본 로그 드라이버인 `json-file`에서 `max-size`, `max-file` 옵션으로 제어

```bash
docker run -d \
  --name=log-rotation-test \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  ubuntu bash -c 'while true; do echo "$(date) - Writing logs..."; sleep 1; done'

docker logs -f log-rotation-test
docker stop log-rotation-test && docker rm log-rotation-test
```

---

## 로그 모니터링 스크립트

쉘 스크립트 = 사람이 손으로 치는 걸 자동화한 것  
`while true;` 로 스레드를 열어 계속 감시

### 기본 모니터링 — `monitor-logs.sh`

```bash
#!/bin/bash
CONTAINER_NAME=$1
THRESHOLD=1000

while true; do
  LOG_SIZE=$(docker logs $CONTAINER_NAME 2>&1 | wc -l)
  CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")

  if [ $LOG_SIZE -gt $THRESHOLD ]; then
    echo "[$CURRENT_TIME] Warning: Log size ($LOG_SIZE lines) exceeds threshold ($THRESHOLD) for $CONTAINER_NAME"
  else
    echo "[$CURRENT_TIME] Log size ($LOG_SIZE lines) is normal for $CONTAINER_NAME"
  fi

  sleep 1
done
```

```bash
chmod +x monitor-logs.sh
```

### 실습용 로그 생성 컨테이너

```dockerfile
FROM ubuntu:20.04
RUN apt-get update && apt-get install -y stress
CMD while true; do date; echo "Test log message"; sleep 1; done
```

```bash
docker build -t log-generator .
docker run -d --name log-test1 log-generator

./monitor-logs.sh log-test1
docker logs -f log-test1
```

### 고급 모니터링 — `advanced-monitor-logs.sh`

로그 파일 저장 + 디스크 사용량 추적 + 임계값 초과 시 컨테이너 자동 재시작

```bash
#!/bin/bash
CONTAINER_NAME=$1
THRESHOLD=1000
LOG_FILE="container_logs.txt"

monitor_logs() {
  LOG_SIZE=$(docker logs $CONTAINER_NAME 2>&1 | wc -l)
  CURRENT_TIME=$(date "+%Y-%m-%d %H:%M:%S")
  DISK_USAGE=$(docker inspect $CONTAINER_NAME --format='{{.LogPath}}' | xargs du -h 2>/dev/null | cut -f1)

  echo "[$CURRENT_TIME] Container: $CONTAINER_NAME" >> $LOG_FILE
  echo "Log lines: $LOG_SIZE" >> $LOG_FILE
  echo "Disk usage: $DISK_USAGE" >> $LOG_FILE

  if [ $LOG_SIZE -gt $THRESHOLD ]; then
    docker logs $CONTAINER_NAME > "${CONTAINER_NAME}_logs_${CURRENT_TIME}.txt"
    docker container restart $CONTAINER_NAME
  fi
}

while true; do
  if docker ps | grep -q $CONTAINER_NAME; then
    monitor_logs
  fi
  sleep 60
done
```

```bash
chmod +x advanced-monitor-logs.sh
sh advanced-monitor-logs.sh test-container

# 강제로 로그 1500줄 생성 (임계값 초과 테스트)
docker exec -it test-container bash -c 'for i in {1..1500}; do echo "Test log entry $i"; done'

cat container_logs.txt
docker logs test-container
```

---

## 디스크 사용량 모니터링

### 현황 확인

```bash
docker system df       # 전체 요약
docker system df -v    # 상세 정보
docker builder du      # 빌드 캐시 사용량 확인
```

> 이미지, 컨테이너를 지웠는데 용량이 줄지 않는다면 → **빌드 캐시** 확인

### 모니터링 스크립트 — `monitor-disk-usage.sh`

임계값(80%) 초과 시 경고 + 가장 큰 컨테이너 5개 출력

```bash
#!/bin/bash
LOG_FILE="disk_usage_log.txt"
THRESHOLD_PERCENT=80

log_message() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a $LOG_FILE
}

check_disk_usage() {
  DOCKER_ROOT=$(docker info | grep "Docker Root Dir" | cut -d: -f2 | tr -d ' ')
  USAGE_PERCENT=$(df -h $DOCKER_ROOT | awk 'NR==2 {print $5}' | tr -d '%')

  log_message "Docker root ($DOCKER_ROOT) usage: ${USAGE_PERCENT}%"

  if [ $USAGE_PERCENT -gt $THRESHOLD_PERCENT ]; then
    log_message "Warning: ${USAGE_PERCENT}% — Top 5 largest containers:"
    docker ps -s --format "{{.Size}}\t{{.Names}}" | sort -hr | head -n 5 | while read line; do
      log_message "$line"
    done
  fi
}

monitor_volumes() {
  docker volume ls -q | while read vol; do
    USAGE=$(docker run --rm -v $vol:/vol alpine du -sh /vol)
    log_message "Volume $vol: $USAGE"
  done
}

while true; do
  check_disk_usage
  monitor_volumes
  sleep 300
done
```

---

## 리소스 정리 (prune)

찌꺼기 데이터가 생기지 않도록 주기적으로 정리

```bash
docker builder prune          # 빌드 캐시 정리
docker volume prune -f        # 사용하지 않는 볼륨 정리
docker image prune -a -f      # 미사용 이미지 전체 정리
docker system prune -f        # 컨테이너 + 네트워크 + 이미지 한 번에 정리
docker system df -v           # 정리 후 디스크 현황 확인
```