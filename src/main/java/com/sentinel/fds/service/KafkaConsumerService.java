package com.sentinel.fds.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.fds.model.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DiscordNotificationService discordNotificationService;

    // "financial-transactions" 우체통을 실시간으로 감시하는 리스너
    @KafkaListener(topics = "financial-transactions", groupId = "fds-alert-group")
    public void consumeTransactionEvent(String message) {
        try {
            // 카프카에서 읽어온 JSON 문자열을 다시 Java 객체로 변환
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

            // 정상 거래(NORMAL_TRANSACTION)가 아닌 경우에만 알림 발송!
            if (!"NORMAL_TRANSACTION".equals(event.getPatternDescription())) {

                String alertMessage = String.format(
                        "🚨 **[보안 경보] 이상 거래 감지!** 🚨\n" +
                                "▶ **유형:** `%s`\n" +
                                "▶ **계좌:** `%s`\n" +
                                "▶ **IP:** `%s`\n" +
                                "▶ **금액:** `%d 원`",
                        event.getPatternDescription(),
                        event.getAccountNumber(),
                        event.getClientIp(),
                        event.getAmount()
                );

                log.warn("위험 감지! 디스코드 알림을 전송합니다. 유형: {}", event.getPatternDescription());
                discordNotificationService.sendAlert(alertMessage);
            }

        } catch (Exception e) {
            log.error("메시지 처리 중 에러 발생: {}", e.getMessage());
        }
    }

}
