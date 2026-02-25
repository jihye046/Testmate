package com.my.ex.dto.request;

import lombok.Data;

@Data
public class ExamSearchDto {
	private String keyword;
	private String type;
	private String subject;
	private String year;
	private String round;
	private int activeFolderId;
}
