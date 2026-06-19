package com.sentinel.fds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.fds.model.TransactionEvent;
import com.sentinel.fds.model.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 접속하면 공격이 시작
    @GetMapping("/api/test/attack")
    public String simulateAttack() {
        // 해커의 고정된 IP 주소 설정
        String attackerIp = "211.55.66.77";

        log.info("🚨 모의 해킹 스크립트 가동: IP {} 에서 5회 연속 공격 시도", attackerIp);

        try {
            for (int i = 0; i < 5; i++) {
                TransactionEvent event = TransactionEvent.builder()
                        .accountNumber("TARGET-ACCOUNT-999")
                        .amount(0L)
                        .clientIp(attackerIp)
                        .patternDescription("BRUTE_FORCE_LOGIN") // 무차별 대입 공격 패턴
                        .type(TransactionType.LOGIN_ATTEMPT)
                        .build();

                String jsonMessage = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("financial-transactions", jsonMessage);
            }
            return "🔥 해커 모의 공격(동일 IP 5회 연속)이 성공적으로 전송되었습니다! 디스코드를 확인하세요.";

        } catch (Exception e) {
            log.error("공격 시뮬레이션 중 에러: {}", e.getMessage());
            return "공격 실패: " + e.getMessage();
        }
    }
}
