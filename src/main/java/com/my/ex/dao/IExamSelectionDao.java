package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamTypeDto;

public interface IExamSelectionDao {
	List<ExamTypeDto> getExamTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getExamSubjects(Map<String, String> map);
	int checkExistingExamInfo(ExamInfoDto examInfo);
	void saveParsedExamInfo(ExamInfoDto examInfo);
	void saveParsedQuestionInfo(Map<String, Object> question);
	int saveParsedChoiceInfo(ExamChoiceDto option);
}
