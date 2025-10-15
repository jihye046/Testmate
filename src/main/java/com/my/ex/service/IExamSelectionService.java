package com.my.ex.service;

import java.util.List;
import java.util.Map;

import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamTypeDto;

public interface IExamSelectionService {
	List<ExamTypeDto> getExamTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getExamSubjects(String examTypeCode, String examRound);
	boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions);
}
