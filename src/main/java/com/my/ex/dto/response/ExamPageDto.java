package com.my.ex.dto.response;

import java.util.List;
import java.util.Set;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamQuestionDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamPageDto {
	private List<ExamQuestionDto> examQuestions;
	private String examType; 							  // 중졸 검정고시
	private String examRound; 							  // 2025년 제1회
	private String examSubject; 						  // 국어
	private List<ExamChoiceDto> examChoices; 			  // 선택지 List<>
	private Set<ExamCommonpassageDto> distinctPassageDto; // 중복제거한 공통지문 List<>
	
	// 공통지문 관리 dto
	@Data
	public static class ExamCommonpassageDto {
		private int commonPassageStartNum;
		private int commonPassageEndNum;
		private String commonPassageText;
	}
}
