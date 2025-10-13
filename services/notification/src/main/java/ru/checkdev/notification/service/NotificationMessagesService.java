package ru.checkdev.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.dto.NotificationEvent;
import ru.checkdev.notification.dto.WisherApprovedDTO;
import ru.checkdev.notification.service.kafka.NotificationProducer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationMessagesService {
    private final NotificationProducer notificationProducer;

    /**
     * Метод находит chatId всех пользователей, подписанных на категорию и согласившихся на получение оповещений в телеграмм,
     * и передаёт каждый в метод sendNotificationToCategorySubscriber для рассылки оповещений.
     *
     * @param categorySubscribersIds список id подписчиков на категорию.
     * @param categoryWithTopicDTO DTO категории и темы собеседования для оповещения.
     */
    public void sendMessagesToCategorySubscribers(List<Integer> categorySubscribersIds,
                                                  CategoryWithTopicDTO categoryWithTopicDTO) {
        categorySubscribersIds.forEach(userId -> {
                    NotificationEvent event = NotificationEvent.forCategory(userId, categoryWithTopicDTO);
                    notificationProducer.send(event);
                });
    }

    /**
     * Метод формирует сообщение об обновлении в категории и отправляет пользователю телеграмм по указанному chatId.
     *
     * @param chatId id чата пользователя.
     * @param categoryWithTopicDTO DTO с данными для формирования сообщения.
     */
   /*  public void sendNotificationToCategorySubscriber(long chatId, CategoryWithTopicDTO categoryWithTopicDTO) {
        bot.send(new SendMessage(
                        String.valueOf(chatId),
                        "В категории "
                                + categoryWithTopicDTO.getCategoryName()
                                + " появилось новое собеседование."
                                + System.lineSeparator()
                                + "Ссылка на собеседование: "
                                + uriProvider.getUri(SERVICE_ID)
                                + "/interview/" + categoryWithTopicDTO.getInterviewId()
                )
        );
    } */

    /**
     * Метод формирует сообщение об отзыве и отправляет пользователю, которому оставлен отзыв.
     *
     * @param feedbackNotification данные для формирования отзыва конкретному пользователю.
     */
    public void sendFeedbackNotification(FeedbackNotificationDTO feedbackNotification) {
        NotificationEvent event = NotificationEvent.forFeedback(feedbackNotification);
        notificationProducer.send(event);
    }

    /**
     * Метод формирует сообщение о приглашении на собеседование и отправляет пользователю.
     *
      * @param wisherApprovedDTO данные для формирования и отправки приглашения на собеседование.
     */
    public void sendApprovedNotification(WisherApprovedDTO wisherApprovedDTO) {
        NotificationEvent event = NotificationEvent.forApproved(wisherApprovedDTO);
        notificationProducer.send(event);
    }
}
