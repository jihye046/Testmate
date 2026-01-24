package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.response.ExamTitleDto;

public interface IExamSelectionDao {
	List<ExamTypeDto> getExamTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getSubjectsByExamRound(Map<String, String> map);
	List<ExamTitleDto> getAllExamTitlesByFolderId(int folderId);
	int checkExistingExamInfo(ExamInfoDto examInfo);
	void saveParsedExamInfo(ExamInfoDto examInfo);
	void saveParsedQuestionInfo(Map<String, Object> question);
	int saveParsedChoiceInfo(ExamChoiceDto option);
	String getExamtypename(String examType);
	List<ExamQuestionDto> getExamQuestions(Map<String, Object> map);
	List<ExamQuestionDto> getExamQuestionsByExamId(int examId);
	List<ExamChoiceDto> getExamChoices(int examId);
	List<ExamQuestionDto> getCommonPassageInfo(Map<String, Object> map);
	int getTotalQuestionCount(int examId);
	int deleteExams(List<Integer> examIds);
	int findSubjectIdByName(Map<String, Object> map);
	List<String> getSubjectsForExamType(String examTypeCode);
	int findTypeIdByCode(String type);
}
