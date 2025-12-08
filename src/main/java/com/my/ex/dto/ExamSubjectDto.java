package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamSubjectDto {
	private int subjectId;
	private String examSubject;
	private int examTypeId;
	private char isdeleted;
}
