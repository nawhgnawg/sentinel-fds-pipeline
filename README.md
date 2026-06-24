# 🛡️ Zero-Trust 기반 실시간 이상거래 탐지 시스템 (FDS) 파이프라인

## 📖 프로젝트 개요
본 프로젝트는 금융권 실무 환경의 보안 및 대용량 트래픽 처리 요구사항을 충족하기 위해 설계된 **이벤트 기반(Event-Driven) 이상거래 탐지 시스템(FDS)** 입니다.

단순한 기능 구현을 넘어, 분산 시스템 간의 완벽한 비동기 처리와 **제로 트러스트(Zero-Trust) 기반의 인프라 보안망(망분리 및 인증 체계)** 을 로컬 환경에 완벽하게 구축하여, 안정적이고 확장이 용이한 비즈니스 아키텍처를 설계하는 데 집중했습니다.

## 🗺️ 전체 시스템 아키텍처

아래는 Spring Boot, Apache Kafka, Python AI 서버, ELK Stack을 활용한 전체 데이터 흐름 및 보안 아키텍처 다이어그램입니다.

![FDS 아키텍처 다이어그램](./images/diagram.jpg)

## 🛠️ 핵심 구축 단계 및 기술적 의사결정

### Level 1. 초고속 인메모리 룰 엔진 (Redis)
* 목적: 대규모 트래픽 발생 시 메인 서버의 부하를 줄이기 위한 1차 방어선 구축.
* 구현: Redis를 활용하여 "특정 IP에서 1분 이내 5회 이상의 비정상 접근"을 인메모리 상에서 O(1)의 속도로 즉각 탐지 및 차단.

### Level 2. 비동기 AI 판별 및 알림 파이프라인 (Kafka & FastAPI)
* 목적: 데이터 수집(Producer)과 분석(Consumer)의 결합도를 낮추어 서버 생존성 보장.
* 구현: 
  * Apache Kafka를 도입하여 금융 거래 로그를 실시간 스트리밍 처리.
  * Spring Boot Consumer가 Python FastAPI(머신러닝 서버)로 판별을 의뢰하고, 위험도가 기준치(85%)를 초과할 경우 즉시 Discord Webhook으로 알림 전송.

### Level 3. Zero-Trust 기반 인프라 보안망 구축
* 목적: 무분별한 네트워크 접근을 차단하고, 각 컴포넌트 간의 통신에 강력한 신원 증명 요구.
* 구현:
  * Kafka: SASL/PLAIN 인증을 적용하여 JAAS 설정 파일이 확인된 클라이언트만 통신 허용.
  * ELK Stack: Elasticsearch에 X-Pack Security를 활성화하여 Logstash 및 Kibana 통신 시 개별 시스템 계정 및 암호화된 자격 증명 강제.

## 🎯 기술 스택
* **Backend**: Java 17, Spring Boot, Spring Data Redis, Spring Kafka
* **AI/ML**: Python, FastAPI, Pydantic
* **Infra & Event Streaming**: Docker, Docker Compose, Apache Kafka, Zookeeper
* **Data & Monitoring**: Redis, Elasticsearch, Logstash, Kibana (ELK)
* **Collaboration**: GitHub, Discord Webhook


## 🚀 실행 방법 (How to Run)

**1. 인프라 실행 (Kafka & Zookeeper)**
```bash
docker-compose up -d
```
**2. Spring Boot 및 Python AI 서버 실행**
* Spring Boot 애플리케이션 실행 (Port: 8080)
* FastAPI 서버 기동 (Port: 8000)

**3. 모의 데이터 전송 및 모니터링**
* 브라우저 또는 Postman을 통해 ```http://localhost:8080/api/test/attack``` 호출
* Kibana 접속 ```http://localhost:5601``` 후 실시간 대시보드 확인