package ru.checkdev.desc.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewCountCategoryDto {
    private int id;
    private String name;
    private int total;
    private long topicsSize;
    private int position;
    private int interviewCount;
}
