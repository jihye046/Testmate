package com.my.ex.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamAnswerDto {
	private int answerId; 			// pk
	private int questionId; 		// exam_question fk
	private int correctAnswer;		// 정답
	private int examId;				// exam_info fk
	
	// 🔥response용으로만 쓰이는 필드
	private int questionNum;

	
	public ExamAnswerDto(int answerId, int correctAnswer) {
		this.answerId = answerId;
		this.correctAnswer = correctAnswer;
	}
	
	public ExamAnswerDto(int questionId, int correctAnswer, int examId) {
		this.questionId = questionId;
		this.correctAnswer = correctAnswer;
		this.examId = examId;
	}

}
