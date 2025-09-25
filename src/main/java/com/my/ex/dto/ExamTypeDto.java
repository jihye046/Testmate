package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamTypeDto {
	private int examTypeId;
	private String examTypeCode;
	private String examTypeName;
	private int isDeleted;
}
