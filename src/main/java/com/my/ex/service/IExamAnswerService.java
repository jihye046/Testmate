package com.my.ex.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamInfoDto;

public interface IExamAnswerService {
	Map<Integer, String> parsePdfToAnswers(MultipartFile file) throws Exception ;
	Integer getQuestionId(Integer qNum, ExamInfoDto examInfoDto); 
	boolean saveParsedAnswerData(ExamInfoDto examInfo, List<ExamAnswerDto> answers);
	List<ExamAnswerDto> getAnswersByExamId(int examId);
	int labelToNumber(String label);
	String examTypeCodeWithoutAnswer(String examTypeCode);
	List<ExamAnswerDto> buildParsedAnswerData(Map<Integer, String> answerMap, ExamInfoDto examInfoDto);
	boolean updateAnswers(List<ExamAnswerDto> answerDtos);
	List<ExamAnswerDto> getAnswersByQuestionIds(List<Integer> questionIds);
	ExamAnswerDto getAnswerByQuestionId(Integer questionId);
	String examTypeCodeWithAnswer(String examTypeCode);
	Map<String, Object> checkAnswers(Map<String, Object> map);
}
