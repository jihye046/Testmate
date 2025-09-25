package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamInfoDto {
	private int examId;
	private int examTypeId;
	private String examRound;
	private String examSubject;
}
