package com.sentinel.fds.service;

import com.sentinel.fds.model.TransactionEvent;
import com.sentinel.fds.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionSimulator {

    private final KafkaProducerService producerService;
    private final Random random = new Random();

    // 1초(1000ms)마다 금융 거래 이벤트로 자동 생성
    @Scheduled(fixedRate = 1000)
    public void runSimulator() {
        int dice = random.nextInt(100);     // 0 ~ 99 의 주사위

        if (dice < 85) {
            // 85% 확률로 아주 평범하고 안전한 대다수의 고객 거래 발생
            generateNormalTransaction();
        } else if (dice < 93) {
            // 8% 확률로 보이스피싱 의심 시나리오 (단시간 내 950만 원 이상 연쇄 이체)
            generateVoicePhishingScenario();
        } else {
            // 7% 확률로 계좌 탈취 의심 시나리오 (평소와 다른 해외 해외 IP + 기기 변경 후 즉시 이체)
            generateAccountTakeoverScenario();
        }
    }

    private void generateNormalTransaction() {
        // TransactionEvent 객체 생성
        TransactionEvent normal = TransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .accountNumber("3333-01-" + (100000 + random.nextInt(900000)))
                .userId("user_" + random.nextInt(5000))
                .type(TransactionType.TRANSFER)
                .amount(10000 + random.nextInt(500000))
                .clientIp("192.168.1." + random.nextInt(254))
                .deviceOs(random.nextBoolean() ? "iOS" : "Android")
                .location("Seoul, South Korea")
                .timestamp(Instant.now().toString())
                .patternDescription("NORMAL_TRANSACTION")
                .build();

        // Kafka 전송
        producerService.sendTransactionEvent(normal);
    }

    private void generateVoicePhishingScenario() {
        // 보이스피싱 피해자는 단시간에 은행 이체 한도 직전까지 돈을 계속 쪼개서 보냅니다.
        TransactionEvent fraud = TransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .accountNumber("3333-02-999999") // 고정된 피해자 계좌 시뮬레이션
                .userId("victim_kim")
                .type(TransactionType.TRANSFER)
                .amount(9500000) // 지연 인출 제도를 피하기 위한 1천만 원 미만 쪼개기 송금
                .clientIp("210.123.45." + random.nextInt(254))
                .deviceOs("Android")
                .location("Busan, South Korea")
                .timestamp(Instant.now().toString())
                .patternDescription("CRITICAL_SUSPICIOUS_RAPID_DRAINAGE")
                .build();

        producerService.sendTransactionEvent(fraud);
    }

    private void generateAccountTakeoverScenario() {
        // 평소 한국에서 접속하던 유저가 기기가 바뀌자마자 아프리카 IP로 접속해 거액을 송금하는 상황
        TransactionEvent hack = TransactionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .accountNumber("3333-03-777777")
                .userId("user_lee")
                .type(TransactionType.TRANSFER)
                .amount(45000000) // 4500만 원 거액 이체
                .clientIp("185.220.101." + random.nextInt(254)) // 익명화 노드 토르 IP 대역 시뮬레이션
                .deviceOs("Windows_Unknown")
                .location("Lagos, Nigeria")
                .timestamp(Instant.now().toString())
                .patternDescription("CRITICAL_SUSPICIOUS_LOCATION_CHANGE")
                .build();

        producerService.sendTransactionEvent(hack);
    }
}
