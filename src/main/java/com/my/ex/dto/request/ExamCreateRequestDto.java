package com.my.ex.dto.request;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ExamCreateRequestDto {
	private CreateExamInfo examInfo;
	private List<Question> questions; // 시험지 등록용
	private Question question; 		  // 시험지 수정용
	private Map<String, MultipartFile> fileMap;

	@Data
	public static class CreateExamInfo {
		private Integer examId;
		private String type;
		private String typeKor;
		private String round;
		private String subject;
		private int folderId;
	}
	
	@Data
	public static class Question {
		// 등록용
		private int answerText;
		
		// 수정용
		private Integer questionId; // 수정용
		private QuestionAnswer questionAnswer;
		
		// 공용
		private int questionNum;
		private char useCommonPassage;
		private CommonPassage commonPassage;
		private char useIndividualPassage;
		private IndividualPassage individualPassage;
		private String questionText; 
		private List<QuestionChoice> questionChoices;

		@Data
		public static class CommonPassage {
			private String type;
			private String content;
			private String rangeText;
			private int[] rangeArray;
			private String fileKey;
			private int id;
		}

		@Data
		public static class IndividualPassage {
			private String type;
			private String content;
			private String fileKey;
		}

		@Data
		public static class QuestionChoice {
			private int choiceId;
			private int choiceNum;
			private String choiceText;
			private String choiceLabel;
		}
		
		@Data
		public static class QuestionAnswer {
			private Integer answerId;
			private int correctAnswer;
		}
	}
	
}
