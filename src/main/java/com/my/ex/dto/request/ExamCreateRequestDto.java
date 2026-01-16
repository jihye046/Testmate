package com.my.ex.dto.request;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ExamCreateRequestDto {
	private CreateExamInfo examInfo;
	private List<Questions> questions;
	private Map<String, MultipartFile> fileMap;

	@Data
	public static class CreateExamInfo {
		private String type;
		private String round;
		private String subject;
		private int folderId;
	}
	
	@Data
	public static class Questions {
		private int questionNum;
		private char useCommonPassage;
		private CommonPassage commonPassage;
		private char useIndividualPassage;
		private IndividualPassage individualPassage;
		private String questionText;
		private List<QuestionChoices> questionChoices;
		private char answerText;

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
		public static class QuestionChoices {
			private int choiceNum;
			private String choiceText;
			private String choiceLabel;
		}
	}
	
}
