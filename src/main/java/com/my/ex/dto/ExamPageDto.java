package com.my.ex.dto;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamPageDto {
	private List<ExamQuestionDto> examQuestions;
	private String examType;
	private String examRound;
	private String examSubject;
	private List<ExamChoiceDto> examChoices;
	private Set<ExamCommonpassageDto> distinctPassageDto;
}
