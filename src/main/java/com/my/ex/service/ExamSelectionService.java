package com.my.ex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.ex.dao.ExamSelectionDao;
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

}
