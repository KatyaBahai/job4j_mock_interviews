package ru.checkdev.notification.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.NotificationEvent;
import ru.checkdev.notification.repository.UserTelegramRepository;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.telegram.Bot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class TgNotificationListener {

    private final UserTelegramRepository userTelegramRepository;
    private final Bot bot;
    private final InnerMessageService innerMessageService;

    @KafkaListener(topics = "${kafka.topic:notifications}", groupId = "notification-service")
    public void listen(NotificationEvent event) {
        var optionalChatId = userTelegramRepository.findChatIdByUserIdIfNotifiable(event.getUserId());

        InnerMessage innerMessage = InnerMessage.of()
                .userId(event.getUserId())
                .text(event.getMessage())
                .created(Timestamp.valueOf(LocalDateTime.now()))
                .read(false)
                .interviewId(event.getInterviewId())
                .build();
        CompletableFuture.supplyAsync(() -> innerMessageService.saveMessage(innerMessage));

        optionalChatId.ifPresent(chatId -> {
                    var sendNotification = new SendMessage(String.valueOf(chatId), event.getMessage());
                    sendNotification.setParseMode("Markdown");
                    bot.send(sendNotification);
                }
        );
    }
}
