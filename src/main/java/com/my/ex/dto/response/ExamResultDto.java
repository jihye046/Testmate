package com.my.ex.dto.response;

import lombok.Data;

@Data
public class ExamResultDto {
	private int questionId;
	private int choiceId;
	private int correctAnswer;
	private int userAnswer;
	private String isCorrect;
}
