package com.my.ex.dto.response;

import lombok.Data;

// 공통지문 관리 dto
@Data
public class ExamCommonpassageDto {
	private int commonPassageStartNum;
	private int commonPassageEndNum;
	private String commonPassageText;
}
