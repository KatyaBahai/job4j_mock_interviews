package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.dto.NotificationEvent;
import ru.checkdev.notification.dto.WisherApprovedDTO;
import ru.checkdev.notification.repository.UserTelegramRepository;
import ru.checkdev.notification.service.kafka.NotificationProducer;
import ru.checkdev.notification.telegram.Bot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationMessagesServiceTest {
    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private NotificationMessagesService service;

    @Test
    void whenSendFeedbackNotificationThenProducerSendsCorrectEvent() {
        // given
        FeedbackNotificationDTO dto = new FeedbackNotificationDTO(
                2,
                "sName",
                "iName",
                3
        );

        NotificationEvent expectedEvent = NotificationEvent.forFeedback(dto);

        service.sendFeedbackNotification(dto);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProducer, times(1)).send(captor.capture());
        verifyNoMoreInteractions(notificationProducer);

        NotificationEvent actualEvent = captor.getValue();
        assertThat(actualEvent).isEqualTo(expectedEvent);
    }

    @Test
    void whenSendMessagesToCategorySubscribersThenProducerSendsEventForEachSubscriber() {
        // given
        CategoryWithTopicDTO dto = new CategoryWithTopicDTO(
                1,
                "Java",
                2,
                "Streams API",
                5,
                6
        );

        List<Integer> subscriberIds = List.of(10, 11, 12);

        List<NotificationEvent> expectedEvents = subscriberIds.stream()
                .map(userId -> NotificationEvent.forCategory(userId, dto))
                .toList();

        service.sendMessagesToCategorySubscribers(subscriberIds, dto);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProducer, times(subscriberIds.size())).send(captor.capture());
        verifyNoMoreInteractions(notificationProducer);

        List<NotificationEvent> actualEvents = captor.getAllValues();
        assertThat(actualEvents).containsExactlyElementsOf(expectedEvents);
    }

    @Test
    void whenSendApprovedNotificationThenProducerSendsCorrectEvent() {
        WisherApprovedDTO dto = WisherApprovedDTO.of()
                .interviewId(7)
                .wisherId(8)
                .wisherUserId(15)
                .interviewTitle("Interview about Spring")
                .interviewLink("http://localhost:8080/interview/7")
                .contactBy("contact@example.com")
                .build();

        NotificationEvent expectedEvent = NotificationEvent.forApproved(dto);

        service.sendApprovedNotification(dto);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationProducer, times(1)).send(captor.capture());
        verifyNoMoreInteractions(notificationProducer);

        NotificationEvent actualEvent = captor.getValue();
        assertThat(actualEvent).isEqualTo(expectedEvent);
    }
}