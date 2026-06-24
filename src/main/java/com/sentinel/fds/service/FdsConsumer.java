package com.sentinel.fds.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FdsConsumer {

    // errorHandler를 이 리스너에 바인딩합니다. (기본 빈 이름이 errorHandler면 생략 가능)
    // 똑같은 토픽을 여러 곳에서 동시에 읽고 싶다면, 반드시 groupId를 서로 다르게 설정해야만 카프카가 메시지를 복사해서 양쪽에 다 보내줍니다.
    @KafkaListener(topics = "financial-transactions", groupId = "fds-dlq-test-group")
    public void consume(String message) {
        log.info("📬 [Main Topic] 데이터 수신: {}", message);

        // 🚨 [테스트용 강제 에러 폭탄]
        // 실무에서 Python AI 서버가 다운되었거나 네트워크 에러(500, Connection Refused)가 난 상황을 시뮬레이션합니다.
        if (message.contains("공격") || true) { // 조건문은 편하게 조절하세요.
            log.info("❌ 에러 발생! AI 서버 통신 실패. 재시도를 시작합니다...");
            throw new RuntimeException("AI Server is Down! (Simulated Error)");
        }

        // 정상 로직 (에러가 안 나면 실행됨)
        // fastapiClient.predict(message);
    }

    // 🛡️ [DLQ 전용 감시 리스너 추가]
    // 메인 리스너가 3번 넘게 실패해서 격리 보관소(DLQ)로 들어온 데이터가 있는지 감시합니다.
    @KafkaListener(topics = "financial-transactions.DLQ",
            groupId = "fds-dlq-group",
            // 🌟 이 리스너는 무조건 파티션의 맨 처음(earliest)부터 쌓인 모든 데이터를 읽어옵니다.
            properties = {"auto.offset.reset=earliest"})
    public void consumeDlq(String failedMessage) {
        log.error("🚨 [🚨 DLQ 안전 금고] 유실될 뻔한 데이터가 안전하게 대피되었습니다: {}", failedMessage);
        log.error("👉 관리자 조치 필요: 파이썬 서버 복구 후 이 데이터를 재처리해야 합니다.");
    }
}
