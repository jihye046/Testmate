package com.my.ex.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ExamTitleDto {
	private int examTypeId;
	private String examTypeCode;
	private String examTypeName;
	
	private int examId;
	private String examRound;
	private String examSubject;
	
	private int totalCount; // 문항 수
	
	private int folderId;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdDate;
	
	// jsp 호출용
	public String getDisplayTitle() {
		return examTypeName + " " + examRound + " " + examSubject;
	}
}
