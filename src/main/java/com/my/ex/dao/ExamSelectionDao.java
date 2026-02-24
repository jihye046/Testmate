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
	public List<ExamTypeDto> getAllExamTypes() {
		return session.selectList(NAMESPACE + "getAllExamTypes");
	}

	@Override
	public List<ExamTypeDto> getExamPaperTypes() {
		return session.selectList(NAMESPACE + "getExamPaperTypes");
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
	public List<ExamChoiceDto> getExamChoicesByExamId(int examId) {
		return session.selectList(NAMESPACE + "getExamChoicesByExamId", examId);
	}
	
	@Override
	public List<ExamChoiceDto> getExamChoicesByQuestionId(int questionId) {
		return session.selectList(NAMESPACE + "getExamChoicesByQuestionId", questionId);
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

	@Override
	public ExamInfoDto getExamInfoByExamId(int examId) {
		return session.selectOne(NAMESPACE + "getExamInfoByExamId", examId);
	}

	@Override
	public ExamQuestionDto getExamQuestionByQuestionId(int questionId) {
		return session.selectOne(NAMESPACE + "getExamQuestionByQuestionId", questionId);
	}

	@Override
	public int findFolderIdByExamId(int examId) {
		return session.selectOne(NAMESPACE + "findFolderIdByExamId", examId);
	}

	@Override
	public void updateQuestion(ExamQuestionDto questionDto) {
		session.update(NAMESPACE + "updateQuestion", questionDto);
	}

	@Override
	public void updateQuestionChoices(ExamChoiceDto choiceDtos) {
		session.update(NAMESPACE + "updateQuestionChoices", choiceDtos);
	}

	@Override
	public int getExamIdByExamTypeId(Map<String, Object> map) {
		return session.selectOne(NAMESPACE + "getExamIdByExamTypeId", map);
	}

	@Override
	public List<Integer> getQuestionIdByExamId(int examId) {
		return session.selectList(NAMESPACE + "getQuestionIdByExamId", examId);
	}

	@Override
	public int getExamTypeIdByExamTypeCode(String examTypeCode) {
		return session.selectOne(NAMESPACE + "getExamTypeIdByExamTypeCode", examTypeCode);
	}

}
