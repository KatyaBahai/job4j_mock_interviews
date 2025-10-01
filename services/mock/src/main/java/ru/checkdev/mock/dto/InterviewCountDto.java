package ru.checkdev.mock.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class InterviewCountDto {
    Long interviewCount;
    Integer topicId;

    public InterviewCountDto(Long interviewCount, Integer topicId) {
        this.interviewCount = interviewCount;
        this.topicId = topicId;
    }
}
