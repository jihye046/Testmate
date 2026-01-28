package com.my.ex.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.my.ex.dao.ExamAnswerDao;
import com.my.ex.dao.ExamSelectionDao;
import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.parser.geomjeong.parse.answer.GeomjeongAnswerParser;
import com.my.ex.parser.geomjeong.upload.answer.UploadedGeomjeongAnswerPdfTextExtractor;

@Service
public class ExamAnswerService implements IExamAnswerService {
	
	@Autowired
	private UploadedGeomjeongAnswerPdfTextExtractor extractor;
	
	@Autowired
	private GeomjeongAnswerParser answerParser;
	
	@Autowired
	private ExamAnswerDao dao;
	
	@Autowired
	private ExamSelectionDao examSelectionDao;
	
	@Override
	public Map<Integer, String> parsePdfToAnswers(MultipartFile file) throws Exception {
		// PDF 텍스트 추출
		String text = extractor.extract(file);
		
		if(text == null || text.trim().isEmpty()) {
			throw new Exception("PDF에서 텍스트를 읽을 수 없습니다. PDF 파일인지 확인해주세요.");
		}
		
		return answerParser.parse(text);
	}

	@Override
	public Integer getQuestionId(Map<String, Object> map) {
		return dao.getQuestionId(map);
	}

	@Override
	@Transactional
	public boolean saveParsedAnswerData(ExamInfoDto examInfo, List<ExamAnswerDto> answers) {
		// 답안지 정보 저장
		// 중복 답안지 정보 확인
		if(examSelectionDao.checkExistingExamInfo(examInfo) > 0) return false;
		
		// 폴더 id 저장
		if(examInfo.getFolderId() == null) {
			examInfo.setFolderId(1);
		}
		
		// 답안지 정보 저장
		examSelectionDao.saveParsedExamInfo(examInfo); // 답안지 정보 저장 후 examId으로 설정됨
		Integer examId = (Integer)examInfo.getExamId(); // DB 삽입 후 주입된 examId 가져옴
		if(examId == null || examId <= 0) throw new RuntimeException();
		
		// 답안지 저장
		for(ExamAnswerDto answer : answers) {
			dao.saveParsedAnswerData(answer);
		}
		
		return false;
	}
	
}
