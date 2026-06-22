package com.sentinel.fds.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.fds.model.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DiscordNotificationService discordNotificationService;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    // 파이썬 머신러닝 서버 주소 (도커 내부 통신이 아닌 로컬 통신 기준)
    private final String ML_API_URL = "http://localhost:8000/predict";

    // "financial-transactions" 우체통을 실시간으로 감시하는 리스너
    @KafkaListener(topics = "financial-transactions", groupId = "fds-alert-group")
    public void consumeTransactionEvent(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

            // 1. Python AI 서버에 보낼 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<TransactionEvent> requestEntity = new HttpEntity<>(event, headers);

            // 2. AI 서버에 판별 요청 (HTTP POST)
            String response = restTemplate.postForObject(ML_API_URL, requestEntity, String.class);

            // 3. AI 서버의 응답(JSON) 해석하기
            // readTree() - 클래스를 따로 만들 필요가 없습니다. JSON에서 내가 필요한 특정 필드 몇 개만 쏙쏙 골라낼 때 압도적으로 편리
            // .get("필드명")으로 값을 꺼냄
            JsonNode responseNode = objectMapper.readTree(response);
            boolean isFraud = responseNode.get("is_fraud").asBoolean();
            double probability = responseNode.get("fraud_probability").asDouble();

            // 4. AI가 '사기(True)'라고 확신했다면 알림 발송!
            if (isFraud) {
                String alertMessage = String.format(
                        "🤖 **[AI 보안 경보] 머신러닝 이상 탐지!** 🤖\n" +
                                "▶ **AI 확신도:** `%.0f%%` (사기 확률)\n" +
                                "▶ **공격 IP:** `%s`\n" +
                                "▶ **발생 금액:** `%d 원`\n" +
                                "▶ **의심 패턴:** `%s`",
                        probability * 100,
                        event.getClientIp(),
                        event.getAmount(),
                        event.getPatternDescription()
                );

                log.warn("🤖 AI 사기 판별 완료 (확률: {}%) - IP: {}", probability * 100, event.getClientIp());
                discordNotificationService.sendAlert(alertMessage);
            }

        } catch (Exception e) {
            log.error("AI 서버 통신 및 메시지 처리 중 에러 발생: {}", e.getMessage());
        }
    }


    /**
     * 기존의 Redis를 이용한 일정 count가 넘어가면 위험 감지 신호 보내기
     */
    /*
    // "financial-transactions" 우체통을 실시간으로 감시하는 리스너
    @KafkaListener(topics = "financial-transactions", groupId = "fds-alert-group")
    public void consumeTransactionEvent(String message) {
        try {
            // 카프카에서 읽어온 JSON 문자열을 다시 Java 객체로 변환
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            String clientIp = event.getClientIp();

            // 정상 거래(NORMAL_TRANSACTION)가 아닌 경우에만 알림 발송!
            if (!"NORMAL_TRANSACTION".equals(event.getPatternDescription())) {

                // 1. Key 만들기
                String redisKey = "FDS:IP: " + clientIp;

                // 2. 해당 IP의 카운트를 1 증가
                Long count = redisTemplate.opsForValue().increment(redisKey);

                // 3. 처음 기록하는 IP면, 60초 뒤에 지워지도록 타이머(TTL) 설정
                if (count != null && count == 1) {
                    redisTemplate.expire(redisKey, Duration.ofSeconds(60));
                }

                log.info("🔍 감시 중: IP [{}] - 현재 누적 이상 거래 횟수: {}", clientIp, count);

                // 4. 만약 1분 안에 누적 횟수가 5번을 도달했다면?
                if (count != null && count >= 5) {
                    String alertMessage = String.format(
                            "🚨 **[긴급 보안 경보] 지능형 공격 감지!** 🚨\n" +
                                    "▶ **설명:** 1분 내 동일 IP 연속 이상 거래 발생\n" +
                                    "▶ **공격 IP:** `%s`\n" +
                                    "▶ **발생 횟수:** `%d 회`\n" +
                                    "▶ **최근 공격 유형:** `%s`",
                            clientIp,
                            count,
                            event.getPatternDescription()
                    );

                    log.warn("🚨 임계치 도달! 디스코드 알림을 전송합니다. IP: {}", clientIp);
                    discordNotificationService.sendAlert(alertMessage);

                    // (선택) 알림을 보낸 후에는 도배 방지를 위해 카운트를 초기화하거나 블랙리스트로 넘길 수 있습니다.
                    redisTemplate.delete(redisKey);
                }

            }

        } catch (Exception e) {
            log.error("메시지 처리 중 에러 발생: {}", e.getMessage());
        }
    }
    */
}
