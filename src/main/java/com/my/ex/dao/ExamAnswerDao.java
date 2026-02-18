package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamAnswerDto;

@Repository
public class ExamAnswerDao implements IExamAnswerDao {

	private final String NAMESPACE = "com.my.ex.ExamAnswerMapper.";
	
	@Autowired
	private SqlSession session;
	
	@Override
	public Integer getQuestionId(Map<String, Object> map) {
		return session.selectOne(NAMESPACE + "getQuestionId", map);
	}

	@Override
	public int saveParsedAnswerData(ExamAnswerDto answersList) {
		return session.insert(NAMESPACE + "saveParsedAnswerData", answersList);
	}

	@Override
	public List<ExamAnswerDto> getAnswersByExamId(int examId) {
		return session.selectList(NAMESPACE + "getAnswersByExamId", examId);
	}

	@Override
	public int updateAnswers(ExamAnswerDto answer) {
		return session.update(NAMESPACE + "updateAnswers", answer);
	}

	@Override
	public List<ExamAnswerDto> getAnswersByQuestionIds(List<Integer> questionIds) {
		return session.selectList(NAMESPACE + "getAnswersByQuestionIds", questionIds);
	}

	@Override
	public ExamAnswerDto getAnswerByQuestionId(Integer questionId) {
		return session.selectOne(NAMESPACE + "getAnswerByQuestionId", questionId);
	}

	@Override
	public void updateQuestionAnswer(ExamAnswerDto dto) {
		session.update(NAMESPACE + "updateQuestionAnswer", dto);
	}

}
