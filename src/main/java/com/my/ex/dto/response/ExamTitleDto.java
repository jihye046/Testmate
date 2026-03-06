package com.my.ex.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamTitleDto {
	private int examTypeId;
	private String examTypeCode;
	private String examTypeName;
	
	private int examId;
	private String examRound;
	private String examSubject;
	/**
	 * 화면 표시용 과목명 (정답지 여부에 따라 '과목명 - 정답지' 형태로 가공됨)
	 */
	private String displaySubject;
	
	private int totalCount; // 문항 수
	
	private int folderId;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate createdDate;
	
	// jsp 호출용
	public String getDisplayTitle() {
		return examTypeName + " " + examRound + " " + examSubject;
	}
}
