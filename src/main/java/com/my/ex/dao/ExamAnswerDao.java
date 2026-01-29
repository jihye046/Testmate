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
	public List<ExamAnswerDto> getAnswers(int examId) {
		return session.selectList(NAMESPACE + "getAnswers", examId);
	}

	@Override
	public int updateAnswers(ExamAnswerDto answer) {
		return session.update(NAMESPACE + "updateAnswers", answer);
	}

}
