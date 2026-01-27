package com.my.ex.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.response.ExamTitleDto;

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
	public List<String> getSubjectsByExamRound(Map<String, String> map) {
		return session.selectList(NAMESPACE + "getSubjectsByExamRound", map);
	}
	
	@Override
	public List<ExamTitleDto> getAllExamTitlesByFolderId(int folderId) {
		return session.selectList(NAMESPACE + "getAllExamTitlesByFolderId", folderId);
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

	@Override
	public String getExamtypename(String examType) {
		return session.selectOne(NAMESPACE + "getExamtypename", examType);
	}
	
	@Override
	public List<ExamQuestionDto> getExamQuestions(Map<String, Object> map) {
		return session.selectList(NAMESPACE + "getExamQuestions", map);
	}
	
	@Override
	public List<ExamQuestionDto> getExamQuestionsByExamId(int examId) {
		return session.selectList(NAMESPACE + "getExamQuestionsByExamId", examId);
	}

	@Override
	public List<ExamChoiceDto> getExamChoices(int examId) {
		return session.selectList(NAMESPACE + "getExamChoices", examId);
	}

	@Override
	public List<ExamQuestionDto> getCommonPassageInfo(Map<String, Object> map) {
		return session.selectList(NAMESPACE + "getCommonPassageInfo", map);
	}

	@Override
	public int getTotalQuestionCount(int examId) {
		return session.selectOne(NAMESPACE + "getTotalQuestionCount", examId);
	}

	@Override
	public int deleteExams(List<Integer> examIds) {
		return session.update(NAMESPACE + "deleteExams", examIds);
	}

	@Override
	public int findSubjectIdByName(Map<String, Object> map) {
		return session.selectOne(NAMESPACE + "findSubjectIdByName", map);
	}

	@Override
	public List<String> getSubjectsForExamType(String examTypeCode) {
		return session.selectList(NAMESPACE + "getSubjectsForExamType", examTypeCode);
	}

	@Override
	public int findTypeIdByCode(String type) {
		return session.selectOne(NAMESPACE + "findTypeIdByCode", type);
	}

	@Override
	public String findExistingExamFolderId(ExamInfoDto examInfoDto) {
		return session.selectOne(NAMESPACE + "findExistingExamFolderId", examInfoDto);
	}

}
