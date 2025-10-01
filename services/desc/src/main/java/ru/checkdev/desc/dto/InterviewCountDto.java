package ru.checkdev.desc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class InterviewCountDto {
    int interviewCount;
    int topicId;

    public InterviewCountDto(int interviewCount, int topicId) {
        this.interviewCount = interviewCount;
        this.topicId = topicId;
    }
}
