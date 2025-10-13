package ru.checkdev.notification.dto;

import lombok.*;
import ru.checkdev.notification.service.EurekaUriProvider;

@Data
@NoArgsConstructor
@Builder
public class NotificationEvent {
    /**
     * \ APPROVED, CATEGORY, FEEDBACK - from service
     * package ru.checkdev.notification.service.NotificationMessagesService
     **/
    private String type;
    private Integer userId;
    private String message;
    private Integer interviewId;
    private static final String BASE_URL = "http://localhost:8080";

    public NotificationEvent(String type, Integer userId, String message, Integer interviewId) {
        this.type = type;
        this.userId = userId;
        this.message = message;
        this.interviewId = interviewId;
    }

    public static NotificationEvent forCategory(Integer userId, CategoryWithTopicDTO dto) {
        var message = "В категории "
                + dto.getCategoryName()
                + " появилось новое собеседование."
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + BASE_URL
                + "/interview/" + dto.getInterviewId();

        return NotificationEvent.builder()
                .type("CATEGORY")
                .userId(userId)
                .message(message)
                .interviewId(dto.getInterviewId())
                .build();
    }

    public static NotificationEvent forFeedback(FeedbackNotificationDTO dto) {
        var message = "Пользователь "
                + dto.getSenderName()
                + " оставил Вам отзыв о собеседовании на тему "
                + dto.getInterviewName()
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + BASE_URL
                + "/interview/" + dto.getInterviewId();

        return NotificationEvent.builder()
                .type("FEEDBACK")
                .userId(dto.getRecipientId())
                .message(message)
                .interviewId(dto.getInterviewId())
                .build();
    }

    public static NotificationEvent forApproved(WisherApprovedDTO dto) {
        var message = String.format(
                "Вы приглашены на собеседование \"[%s](%s)\".%sСвяжитесь с автором: %s",
                dto.getInterviewTitle(),
                dto.getInterviewLink(),
                System.lineSeparator(),
                dto.getContactBy());

        return NotificationEvent.builder()
                .type("APPROVED")
                .userId(dto.getWisherUserId())
                .message(message)
                .interviewId(dto.getInterviewId())
                .build();
    }
}
