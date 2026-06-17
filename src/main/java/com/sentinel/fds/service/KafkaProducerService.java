package com.sentinel.fds.service;

import com.sentinel.fds.model.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();    // 객체를 JSON 문자열로 변환
    private final String TOPIC = "financial-transactions";

    public void sendTransactionEvent(TransactionEvent event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);

            // Kafka로 비동기 전송 및 성공/실패 콜백 처리
            kafkaTemplate.send(TOPIC, event.getAccountNumber(), jsonMessage)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("✅ Kafka 전송 성공 -> 계좌: {}, 금액: {}원, 유형: [{}]",
                                    event.getAccountNumber(), event.getAmount(), event.getPatternDescription());
                        } else {
                            log.error("❌ Kafka 전송 실패: {}", ex.getMessage());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 에러 발생: {}", e.getMessage());
        }

    }
}
