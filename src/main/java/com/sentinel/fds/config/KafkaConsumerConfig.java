package com.sentinel.fds.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    // 🌟 스프링 부트가 application.properties의 SASL 보안 설정을 주입해 만든 안전한 템플릿입니다.
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // 1. application.properties의 SASL 보안 설정이 포함된 컨수머 공장을 연결합니다.
        factory.setConsumerFactory(consumerFactory);

        // 2. 에러가 났을 때 보안 인증을 통과하여 DLQ 토픽으로 배달해 줄 Recoverer 세팅
        // 🌟 [핵심 수정] 생성자에 "어느 토픽, 어느 파티션으로 보낼지" 결정하는 람다 함수를 넣습니다.
        // 파티션 번호에 '-1'을 주면 "파티션을 고집하지 말고 카프카가 알아서 빈 곳에 넣어라"는 뜻이 됩니다.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLQ", -1)
        );

        // 3. 0.5초 간격으로 총 3번 재시도 후 DLQ로 던지는 에러 핸들러 생성
        FixedBackOff fixedBackOff = new FixedBackOff(500L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);

        // 4. 🌟 이 공장에서 만들어지는 모든 리스너(@KafkaListener)에 이 DLQ 대피 시스템을 기본 탑재합니다.
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

//    // 1. 에러 발생 시 데이터를 DLQ 토픽으로 배달해 주는 회복자(Recoverer) 등록
//    @Bean
//    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, String> template) {
//        // 기본적으로 원래토픽명.DLQ 라는 이름의 토픽으로 데이터를 보냅니다.
//        // 예: financial-transactions -> financial-transactions.DLQ
//        return new DeadLetterPublishingRecoverer(template);
//    }
//
//    // 2. 재시도 정책 및 DLQ 연동을 담당하는 에러 핸들러 등록
//    @Bean
//    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
//        // FixedBackOff(대기시간ms, 최대재시도횟수)
//        // 1초 간격으로 총 3번 재시도(Retry)를 하고, 그래도 실패하면 recoverer(DLQ)를 실행합니다.
//        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3L);
//        return new DefaultErrorHandler(recoverer, fixedBackOff);
//    }
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
//            ConsumerFactory<String, String> consumerFactory,
//            DefaultErrorHandler errorHandler) { // 위에서 만든 에러 핸들러를 주입받음
//
//        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//
//        // 🌟 이 라인을 반드시 추가해야 공장 전체에 DLQ 정책이 확실하게 반영됩니다!
//        factory.setCommonErrorHandler(errorHandler);
//
//        return factory;
//    }
}
