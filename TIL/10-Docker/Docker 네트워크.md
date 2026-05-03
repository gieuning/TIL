# Docker 네트워크 및 데이터 관리

## Docker 네트워크란?
- Docker 컨테이너 간 통신을 관리하는 기능
- 기본적으로 컨테이너는 격리된 환경에서 실행되며, 네트워크를 통해 서로 통신 가능

---

## Docker 네트워크 종류

| 드라이버 | 설명 | 주요 특징 | 사용 예시 |
|---------|------|----------|----------|
| bridge | 기본 네트워크 | 컨테이너 간 통신 가능, DNS 지원 | 단일 호스트 내 컨테이너 간 통신 |
| host | 호스트 네트워크 사용 | 포트 매핑 불필요, 성능 최적화 | 성능이 중요한 애플리케이션 |
| none | 네트워크 없음 | 보안성이 높음, 외부 연결 불가 | 네트워크를 사용하지 않는 컨테이너 |
| overlay | 여러 호스트 연결 | Swarm 모드 필요, 컨테이너 간 자동 DNS | 여러 호스트에 걸친 서비스 |
| macvlan | 물리 네트워크 인터페이스 사용 | 개별 MAC 주소 할당 | 네트워크 장비와 직접 통신 |

---

## Bridge 네트워크

### 기본 Bridge — 이름 통신 불가

```shell
# 현재 docker 네트워크 확인
docker network ls

# 기본 bridge 네트워크로 컨테이너 실행
# sleep infinity: 컨테이너 내부에서 종료되지 않는 프로세스를 실행하여 유지
docker run -d --name svc1 --network bridge busybox sleep infinity
docker run -d --name svc2 --network bridge busybox sleep infinity

# 이름으로 ping 시도 → 실패
docker exec -it svc1 ping svc2
# ping: bad address 'svc2'

# IP로 ping 시도 → 성공
docker exec -it svc1 ping 172.17.0.5
# PING 172.17.0.5 (172.17.0.5): 56 data bytes
# 64 bytes from 172.17.0.5: seq=0 ttl=64 time=0.347 ms
```

**기본 bridge에서 이름 통신이 안 되는 이유**
1. 기본 bridge 네트워크는 컨테이너 이름 기반 DNS 해석을 지원하지 않음
2. IP를 직접 사용해야 하지만, 컨테이너 재시작 시 IP가 변경될 수 있어 관리가 어려움

### 사용자 정의 Bridge — 이름 통신 가능 (권장)

```shell
# 사용자 정의 네트워크 생성
docker network create my-network

# 동일 네트워크로 컨테이너 실행
docker run -d --name svc3 --network=my-network busybox sleep infinity
docker run -d --name svc4 --network=my-network busybox sleep infinity

# 이름으로 ping → 성공
docker exec -it svc3 ping svc4
# PING svc4 (172.21.0.3): 56 data bytes
# 64 bytes from 172.21.0.3: seq=0 ttl=64 time=0.300 ms
# 64 bytes from 172.21.0.3: seq=1 ttl=64 time=0.161 ms
```

> 같은 사용자 정의 네트워크 안에 묶인 컨테이너끼리는 **이름(도메인)으로 직접 통신** 가능

---

## Host 네트워크

- 컨테이너가 호스트 시스템의 네트워크 인터페이스를 그대로 사용
- 포트 매핑이 필요 없음
- 컨테이너가 마치 호스트에서 직접 실행되는 것처럼 동작
- 개발 환경에서 편리하게 사용

```
외부IP:80 → 컨테이너:80  (== 내 PC 네트워크와 동일)
```

---

## None 네트워크

- 컨테이너가 어떠한 외부 네트워크와도 연결되지 않는 모드
- 외부 네트워킹이 완전히 차단되며, 로컬호스트 내에서만 독립적으로 실행
- 보안이 중요한 서비스에 사용

```shell
# None 네트워크 컨테이너 생성
docker run -it --network none --name no-net-container alpine:latest sh

# 컨테이너 내부에서 네트워크 연결 확인
ping google.com
# 연결 불가
```

---

## Overlay 네트워크

- Docker Swarm 클러스터 내 여러 노드에 걸친 컨테이너 간 통신을 가능하게 하는 네트워크
- 여러 호스트에서 실행 중인 컨테이너 간에 안전하고 격리된 네트워크 구축 가능
- 내부적으로 **VXLAN** 기술을 사용하여 물리 네트워크 위에 가상 네트워크 구축

**특징**
- 여러 Docker 호스트 간의 네트워크 격리 제공
- 스케일 아웃 아키텍처를 위한 분산 애플리케이션 네트워킹 단순화
- Layer 2 네트워크를 Layer 3 인프라 위에 구축

---

## macvlan 네트워크

- 각 컨테이너에 고유한 MAC 주소를 할당하여 물리 네트워크와 동일한 방식으로 통신
- 기존 물리적 네트워크 인프라에 완벽하게 통합 가능
- 레거시 애플리케이션 컨테이너화에 유용
- 컨테이너가 물리 네트워크에 직접 연결된 것처럼 동작

---

## 핵심 비교

| 구분 | 기본 Bridge | 사용자 정의 네트워크 |
|------|------------|------------------|
| IP 통신 | 가능 | 가능 |
| 이름(DNS) 통신 | **불가** | **가능** |
| 보안 | 낮음 | 높음 |
| 권장 환경 | 간단한 테스트 | 운영 / 개발 |

> 컨테이너 이름으로 통신하려면 반드시 **사용자 정의 네트워크**를 사용해야 한다.