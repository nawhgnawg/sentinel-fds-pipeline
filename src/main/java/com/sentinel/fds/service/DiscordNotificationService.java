package com.sentinel.fds.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DiscordNotificationService {

    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendAlert(String message) {
        try {
            // 디스코드가 요구하는 JSON 포멧 {"content": "보낼 메세지"} 만들기
            Map<String, String> payload = new HashMap<>();
            payload.put("content", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            // 디스코드로 쏘기
            restTemplate.postForObject(discordWebhookUrl, entity, String.class);
            log.info("🚨 디스코드 알림 발송 완료!");
        } catch (Exception e) {
            log.error("❌ 디스코드 알림 발송 실패: {}", e.getMessage());
        }
    }
}
