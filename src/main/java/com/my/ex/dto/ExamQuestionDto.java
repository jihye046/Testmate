package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamQuestionDto {
	private int questionId;
	private int examId; // exam_info fk
	private String sessionNo; // 교시
	private int questionNum; // 문제 번호
	private String questionText; // 문제 내용
	private String questionImage; // 문제 이미지 URL
	private String questionPassage; // 문제 지문 텍스트
	private String passageScope; // 지문 적용 범위 (예: [11~13])
}
