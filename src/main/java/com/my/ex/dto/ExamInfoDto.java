package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamInfoDto {
	private int examId;			// pk
	private int examTypeId; 	// 시험 종류
	private String examRound; 	// 시험 회차
	private String examSubject; // 시험 과목
	private String sessionNo; 	// 시험 교시
	private char isDeleted;		// 시험지 삭제 여부
	
	public ExamInfoDto(String examRound, String examSubject, String sessionNo) {
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.sessionNo = sessionNo;
	}
	
}
