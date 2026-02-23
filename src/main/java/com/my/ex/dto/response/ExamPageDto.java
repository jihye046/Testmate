package com.my.ex.dto.response;

import java.util.List;
import java.util.Set;

import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamQuestionDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamPageDto {
	private List<ExamQuestionDto> examQuestions;
	private ExamQuestionDto examQuestion;
	
	private int examId;
	
	private String examTypeEng;							  // middle-geomjeong
	private String examType; 							  // 중졸 검정고시
	private String examRound; 							  // 2025년 제1회
	private String examSubject; 						  // 국어
	private List<ExamChoiceDto> examChoices; 			  // 선택지 List<>
	private Set<ExamCommonpassageDto> distinctPassageDto; // 중복제거한 공통지문 List<>
	
	private List<ExamAnswerDto> examAnswers;			  // 시험지 전체 정답 조회용
	private ExamAnswerDto examAnswer;					  // 특정 문제 수정 시 해당 문제 정답 조회용
	
	private int folderId;								  // 폴더id
	
	// 공통지문 관리 dto
	@Data
	public static class ExamCommonpassageDto {
		private int commonPassageStartNum;
		private int commonPassageEndNum;
		private String commonPassageText;
	}

	// 사용자: 시험지 조회용 (전체 문제/선택지/공통 지문)
	public ExamPageDto(List<ExamQuestionDto> examQuestions, int examId, String examType, String examRound, String examSubject,
			List<ExamChoiceDto> examChoices, Set<ExamCommonpassageDto> distinctPassageDto) {
		this.examQuestions = examQuestions;
		this.examId = examId;
		this.examType = examType;
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.examChoices = examChoices;
		this.distinctPassageDto = distinctPassageDto;
	}

	// 관리자: 시험지 검토 화면 조회용 (전체 문제/선택지/공통 지문/정답지) 
	public ExamPageDto(List<ExamQuestionDto> examQuestions, String examType, String examRound, String examSubject,
			List<ExamChoiceDto> examChoices, Set<ExamCommonpassageDto> distinctPassageDto,
			List<ExamAnswerDto> examAnswers) {
		this.examQuestions = examQuestions;
		this.examType = examType;
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.examChoices = examChoices;
		this.distinctPassageDto = distinctPassageDto;
		this.examAnswers = examAnswers;
	}

	// 관리자: 관리자가 특정 시험 문제를 수정할 때, 해당 문제 정답만 조회
	public ExamPageDto(ExamQuestionDto examQuestion, String examTypeEng, String examType, String examRound, String examSubject,
			List<ExamChoiceDto> examChoices, ExamAnswerDto examAnswer, int folderId) {
		this.examQuestion = examQuestion;
		this.examTypeEng = examTypeEng;
		this.examType = examType;
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.examChoices = examChoices;
		this.examAnswer = examAnswer;
		this.folderId = folderId;
	}

}
