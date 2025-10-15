package com.my.ex.parser;

import lombok.Data;

// 파싱된 시험 기본 정보를 담기 위한 클래스
@Data
public class ExamInfo {
	private int examTypeId; // 시험 종류
	private String examRound; // 시험 회차
	private String examSubject; // 시험 과목
	private String sessionNo; // 시험 교시
	
	public ExamInfo(String examRound, String examSubject, String sessionNo) {
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.sessionNo = sessionNo;
	}
	
}
