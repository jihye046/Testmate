package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamInfoDto {
	private int examId; // pk
	private int examTypeId; // exam_type fk
	private String examRound; // 시험 회차
	private String examSubject; // 시험 과목
}
