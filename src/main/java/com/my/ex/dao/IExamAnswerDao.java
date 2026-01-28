package com.my.ex.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamAnswerDto;

@Repository
public interface IExamAnswerDao {
	Integer getQuestionId(Map<String, Object> map);
	int saveParsedAnswerData(ExamAnswerDto answersList);
}
