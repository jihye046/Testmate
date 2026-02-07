package com.my.ex.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamInfoDto {
	private int examId;			// pk
	private int examTypeId; 	// 시험 종류
	private String examRound; 	// 시험 회차
	private String examSubject; // 시험 과목
	private Integer subjectId;	// 시험 과목 id
	private String sessionNo; 	// 시험 교시
	private char isDeleted;		// 시험지 삭제 여부
	private Integer folderId;
	private LocalDate created_date;  // 시험지 등록일
	
	// 🔥response용으로만 쓰이는 필드
	private String examTypeEng, examTypeKor; // ex) 'middle-geomjeong', '중졸 검정고시'
	
	public ExamInfoDto(String examRound, String examSubject, String sessionNo) {
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.sessionNo = sessionNo;
	}

	public ExamInfoDto(String examRound, String examSubject) {
		this.examRound = examRound;
		this.examSubject = examSubject;
	}

	public ExamInfoDto(String examRound, String examSubject, String examTypeEng, String examTypeKor) {
		this.examRound = examRound;
		this.examSubject = examSubject;
		this.examTypeEng = examTypeEng;
		this.examTypeKor = examTypeKor;
	}
	
}
