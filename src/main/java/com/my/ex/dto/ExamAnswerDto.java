package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamAnswerDto {
	private int answerId; 			// pk
	private int questionId; 		// exam_question fk
	private String correctLabel;	// 정답 선택지 식별자(①, ②, ③)
}
