package com.my.ex.dto.request;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ExamCreateRequestDto {
	private CreateExamInfo examInfo;
	private List<Questions> questions;
	
	@Getter
	@Setter
	public static class CreateExamInfo {
		private String type;
		private String round;
		private String subject;
		private int folderId;
	}
	
	@Getter
	@Setter
	public static class Questions {
		private int questionNum;
		private char useCommonPassage;
		private CommonPassage commonPassage;
		private char useIndividualPassage;
		private IndividualPassage individualPassage;
		private String questionText;
		private List<QuestionChoices> questionChoices;
		private char answerText;
		
		@Getter
		@Setter
		public static class CommonPassage {
			private String type;
			private String content;
			private String rangeText;
			private int[] rangeArray;
			private int id;
		}
		
		@Getter
		@Setter
		public static class IndividualPassage {
			private String type;
			private String content;
		}
		
		@Getter
		@Setter
		public static class QuestionChoices {
			private int choiceNum;
			private String choiceContent;
			private String choiceLabel;
		}
	}
	
}
