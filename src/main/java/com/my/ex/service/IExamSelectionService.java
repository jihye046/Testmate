package com.my.ex.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.RequestBody;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.request.ExamCreateRequestDto;
import com.my.ex.dto.response.ExamCommonpassageDto;
import com.my.ex.dto.response.ExamTitleDto;
import com.my.ex.dto.service.ParsedExamData;

public interface IExamSelectionService {
	List<ExamTypeDto> getExamTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getExamSubjects(String examTypeCode, String examRound);
	List<ExamTitleDto> getAllExamTitlesByFolderId(int folderId);
	boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions);
	String getExamtypename(String examType);
	List<ExamQuestionDto> getExamQuestions(String examType, String examRound, String examSubject);
	List<ExamQuestionDto> getExamQuestionsByExamId(int examId);
	List<ExamChoiceDto> getExamChoices(int examId);
	Set<ExamCommonpassageDto> getCommonPassageInfo(String examType, String examRound, String examSubject);
	int getTotalQuestionCount(int examId);
	boolean deleteExams(List<Integer> examIds);
	List<String> getSubjectsForExamType(String examTypeCode);
	int findTypeIdByCode(String type);
	boolean saveExamByForm(ExamCreateRequestDto request);
	ParsedExamData buildParsedExamData(ExamCreateRequestDto request);
}
