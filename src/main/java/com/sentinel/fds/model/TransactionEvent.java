package com.sentinel.fds.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class TransactionEvent {
    private String eventId;          // 이벤트 고유 ID
    private String accountNumber;     // 고객 계좌 번호
    private String userId;            // 고객 식별자
    private TransactionType type;     // 거래 유형 (이체, 로그인 등)
    private long amount;              // 이체 금액 (원)
    private String clientIp;          // 접속 IP
    private String deviceOs;          // 접속 기기 OS (iOS, Android, Windows)
    private String location;          // 접속 국가/도시
    private String timestamp;         // 발생 시간 (ISO8601)
    private String patternDescription;// 거래 패턴 정보 (정상/이상 징후 메모)
}