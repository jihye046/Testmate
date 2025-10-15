package com.my.ex.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamChoiceDto {
	private int choiceId; 		// pk
	private int questionId; 	// exam_question fk
	private String choiceLabel; // 선택지 식별자(①, ②, ③)
	private String choiceText; 	// 선택지 내용
	private String choiceImage;	// 선택지 이미지 URL(있으면)
	
	public ExamChoiceDto(String choiceLabel, String choiceText) {
		this.choiceLabel = choiceLabel;
		this.choiceText = choiceText;
	}

}
