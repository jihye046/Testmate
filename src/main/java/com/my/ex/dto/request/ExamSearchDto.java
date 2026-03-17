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
	private int page;

	// DB 조회용
	private int startRow;
	private int endRow;

	public void setPage(int page) {
		this.page = page == 0 ? 1 : page;
	}
}
