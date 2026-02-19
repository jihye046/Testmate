package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.response.ExamResultDto;

@Repository
public interface IExamAnswerDao {
	Integer getQuestionId(Map<String, Object> map);
	int saveParsedAnswerData(ExamAnswerDto answersList);
	List<ExamAnswerDto> getAnswersByExamId(int examId);
	int updateAnswers(ExamAnswerDto answerDto);
	List<ExamAnswerDto> getAnswersByQuestionIds(List<Integer> questionIds);
	ExamAnswerDto getAnswerByQuestionId(Integer questionId);
	void updateQuestionAnswer(ExamAnswerDto dto);
	List<ExamResultDto> checkAnswers(List<ExamChoiceDto> dto);
}
