package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamTypeDto {
	private int examTypeId; 		// pk
	private String examTypeCode; 	// 시험 종류 코드
	private String examTypeName;  	// 시험 종류 이름
	private int isDeleted; 			// 삭제 여부(관리자용)
}
