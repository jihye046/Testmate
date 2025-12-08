package com.my.ex.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.ex.config.EnvironmentConfig;
import com.my.ex.controller.ExamSelectionController;
import com.my.ex.dao.ExamSelectionDao;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.response.ExamCommonpassageDto;
import com.my.ex.dto.response.ExamTitleDto;

@Service
public class ExamSelectionService implements IExamSelectionService {

	@Autowired
	private ExamSelectionDao dao;
	
	@Override
	public List<ExamTypeDto> getExamTypes() {
		return dao.getExamTypes();
	}

	@Override
	public List<String> getExamRounds(String examTypeCode) {
		return dao.getExamRounds(examTypeCode);
	}

	@Override
	public List<String> getExamSubjects(String examTypeCode, String examRound) {
		Map<String, String> map = new HashMap<>();
		map.put("examTypeCode", examTypeCode);
		map.put("examRound", examRound);
		
		return dao.getExamSubjects(map);
	}
	
	@Override
	public List<ExamTitleDto> getAllExamTitlesByFolderId(int folderId) {
		return dao.getAllExamTitlesByFolderId(folderId);
	}

	@Override
	public boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions) {
		// 과목명으로 subject_id 조회 및 set
		Map<String, Object> map = new HashMap<>();
		map.put("examSubject", examInfo.getExamSubject());
		map.put("examTypeId", examInfo.getExamTypeId());
		Integer subjectId = dao.findSubjectIdByName(map);
		if(subjectId == null) throw new RuntimeException("subjectId가 null입니다. 과목명을 확인하세요.");
		examInfo.setSubjectId(subjectId);
		
		// 중복 시험 정보 확인
		if(dao.checkExistingExamInfo(examInfo) > 0) return false;
		
		// 시험 정보 저장
		dao.saveParsedExamInfo(examInfo); // 시험 정보 저장 후 examId 받아옴
		Integer examId = (Integer)examInfo.getExamId(); // DB 삽입 후 주입된 examId
		if(examId == null || examId <= 0) throw new RuntimeException();
		
		// 시험 문제 저장
		for(Map<String, Object> question : questions) {
			question.put("examId", examId);
			dao.saveParsedQuestionInfo(question); // 문제 저장 후 questionId 받아옴
			Integer questionId = (Integer)question.get("questionId"); // DB 삽입 후 주입된 questionId
			if(questionId == null || questionId <= 0) throw new RuntimeException();
			
			// 시험 선택지 저장
			@SuppressWarnings("unchecked")
			List<ExamChoiceDto> options = (List<ExamChoiceDto>) question.get("options");
			for(ExamChoiceDto choiceDto : options) {
				choiceDto.setExamId(examId);
				choiceDto.setQuestionId(questionId);
				
				int insertResult = dao.saveParsedChoiceInfo(choiceDto);
				if(insertResult <= 0) throw new RuntimeException();
			}
		}
		
		return true;
	}

	@Override
	public String getExamtypename(String examType) {
		return dao.getExamtypename(examType);
	}
	
	@Override
	public List<ExamQuestionDto> getExamQuestions(String examType, String examRound, String examSubject) {
		Map<String, Object> map = new HashMap<>();
		map.put("examType", examType);
		map.put("examRound", examRound);
		map.put("examSubject", examSubject);
		
		return dao.getExamQuestions(map);
	}
	
	@Override
	public List<ExamQuestionDto> getExamQuestionsByExamId(int examId) {
		return dao.getExamQuestionsByExamId(examId);
	}

	@Override
	public List<ExamChoiceDto> getExamChoices(int examId) {
		return dao.getExamChoices(examId);
	}
	
	@Override
	public Set<ExamCommonpassageDto> getCommonPassageInfo(String examType, String examRound, String examSubject) {
		Map<String, Object> map = new HashMap<>();
		map.put("examType", examType);
		map.put("examRound", examRound);
		map.put("examSubject", examSubject);
		List<ExamQuestionDto> questionDto = dao.getCommonPassageInfo(map);
		
		ExamCommonpassageDto commonpassageDto; 
//		List<ExamCommonpassageDto> distinctPassageList = new ArrayList<>();
		Set<ExamCommonpassageDto> distinctPassageSet = new HashSet<>();
		
		for(ExamQuestionDto dto: questionDto) {
			commonpassageDto = new ExamCommonpassageDto();
			String scope = dto.getPassageScope(); // 11~13
			
			commonpassageDto.setCommonPassageStartNum(Integer.parseInt(scope.split("~")[0])); // 11
			commonpassageDto.setCommonPassageEndNum(Integer.parseInt(scope.split("~")[1])); // 13
			commonpassageDto.setCommonPassageText(dto.getCommonPassage()); // 공통 지문
			distinctPassageSet.add(commonpassageDto);
//			distinctPassageList.add(commonpassageDto);
		}
//		System.out.println("Set size: " + distinctPassageSet.size()); // Set size: 5
//		System.out.println("List size: " + distinctPassageList.size()); // List size: 15
		
		return distinctPassageSet;
	}

	@Override
	public int getTotalQuestionCount(int examId) {
		return dao.getTotalQuestionCount(examId);
	}

	@Override
	public boolean deleteExams(List<Integer> examIds) {
		return dao.deleteExams(examIds) > 0;
	}

	@Override
	public List<String> getSubjectsForExamType(String examTypeCode) {
		return dao.getSubjectsForExamType(examTypeCode);
	}


}
