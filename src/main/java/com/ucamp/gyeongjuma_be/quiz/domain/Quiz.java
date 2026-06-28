package com.ucamp.gyeongjuma_be.quiz.domain;

import com.ucamp.gyeongjuma_be.common.domain.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Quiz extends BaseEntity {

    private Long quizId;
    private String question;
}
