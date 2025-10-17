package com.my.ex.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamCommonpassageDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;

public interface IExamSelectionService {
	List<ExamTypeDto> getExamTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getExamSubjects(String examTypeCode, String examRound);
	boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions);
	String getExamtypename(String examType);
	List<ExamQuestionDto> getExamQuestions(String examType, String examRound, String examSubject);
	List<ExamChoiceDto> getExamChoices();
	Set<ExamCommonpassageDto> getCommonPassageInfo(String examType, String examRound, String examSubject);
}
