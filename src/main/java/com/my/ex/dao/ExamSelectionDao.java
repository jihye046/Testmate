package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamTypeDto;

@Repository
public class ExamSelectionDao implements IExamSelectionDao {

	private final String NAMESPACE = "com.my.ex.ExamSelectionMapper.";
	
	@Autowired
	private SqlSession session;
	
	@Override
	public List<ExamTypeDto> getExamTypes() {
		return session.selectList(NAMESPACE + "getExamTypes");
	}

	@Override
	public List<String> getExamRounds(String examTypeCode) {
		return session.selectList(NAMESPACE + "getExamRounds", examTypeCode);
	}

	@Override
	public List<String> getExamSubjects(Map<String, String> map) {
		return session.selectList(NAMESPACE + "getExamSubjects", map);
	}
	
	@Override
	public int checkExistingExamInfo(ExamInfoDto examInfo) {
		return session.selectOne(NAMESPACE + "checkExistingExamInfo", examInfo);
	}
	
	@Override
	public void saveParsedExamInfo(ExamInfoDto examInfo) {
		session.insert(NAMESPACE + "saveParsedExamInfo", examInfo);
	}

	@Override
	public void saveParsedQuestionInfo(Map<String, Object> question) {
		session.insert(NAMESPACE + "saveParsedQuestionInfo", question);
	}

	@Override
	public int saveParsedChoiceInfo(ExamChoiceDto option) {
		return session.insert(NAMESPACE + "saveParsedChoiceInfo", option);
	}

}
