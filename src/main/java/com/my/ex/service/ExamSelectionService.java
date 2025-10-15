package com.my.ex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.ex.dao.ExamSelectionDao;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamTypeDto;

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
	public boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions) {
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
				choiceDto.setQuestionId(questionId);
				
				int insertResult = dao.saveParsedChoiceInfo(choiceDto);
				if(insertResult <= 0) throw new RuntimeException();
			}
		}
		
		return true;
	}

}
