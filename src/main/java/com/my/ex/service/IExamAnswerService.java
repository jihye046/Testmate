package com.my.ex.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamInfoDto;

public interface IExamAnswerService {
	Map<Integer, String> parsePdfToAnswers(MultipartFile file) throws Exception ;
	Integer getQuestionId(Map<String, Object> map); 
	boolean saveParsedAnswerData(ExamInfoDto examInfo, List<ExamAnswerDto> answers);
}
