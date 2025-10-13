package ru.checkdev.notification.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.checkdev.notification.dto.NotificationEvent;

@Component
@RequiredArgsConstructor
public class NotificationProducer {
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topic:notifications}")
    private String topic;

    public void send(NotificationEvent event) {
        kafkaTemplate.send(topic, event);
    }
}
