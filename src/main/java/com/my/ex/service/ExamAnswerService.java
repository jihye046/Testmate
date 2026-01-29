package com.my.ex.service;

import java.util.ArrayList;
import java.util.HashMap;
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
	public Integer getQuestionId(Integer qNum, ExamInfoDto examInfoDto) {
		Map<String, Object> map = new HashMap<>();
		map.put("examInfoDto", examInfoDto);
		map.put("questionNum", qNum);
		
		return dao.getQuestionId(map);
	}

	@Override
	@Transactional
	public boolean saveParsedAnswerData(ExamInfoDto examInfo, List<ExamAnswerDto> answers) {
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
			answer.setExamId(examId);
			dao.saveParsedAnswerData(answer);
		}
		
		return true;
	}

	@Override
	public List<ExamAnswerDto> getAnswers(int examId) {
		return dao.getAnswers(examId);
	}

	@Override
	public int labelToNumber(String label) {
		switch(label) {
			case "①": return 1;
			case "②": return 2;
			case "③": return 3;
			case "④": return 4;
			case "⑤": return 5;
			default: throw new IllegalArgumentException("알 수 없는 정답 라벨: " + label);
		}
	}

	// 정답지와 연결될 시험지의 examTypeCode를 추출
	@Override
	public String examTypeCodeWithoutAnswer(String examTypeCode) {
		return examTypeCode.substring(0, examTypeCode.length() - "Answer".length());
	}

	// 관리자가 정답지 PDF 업로드 시 DB 저장용 구조로 변환
	// saveParsedAnswerData() 호출 전에 사용됨
	@Override
	public List<ExamAnswerDto> buildParsedAnswerData(Map<Integer, String> answerMap, ExamInfoDto examInfoDto) {
		List<ExamAnswerDto> answersList = new ArrayList<>();
		for(Map.Entry<Integer, String> entry : answerMap.entrySet()) {
			// 연결할 문제지의 questionId를 조회
			Integer qNum = entry.getKey();
			Integer questionId = getQuestionId(qNum, examInfoDto);
			
			// 연결할 문제지가 등록되어있지 않은 경우
			if(questionId == null) {
				return null;
			}
			
			// 라벨 스타일(①)의 정답을 숫자(1) 형식으로 변환
			int answer = labelToNumber(entry.getValue());
			
			// db 구조로 변환한 dto를 List에 추가
			ExamAnswerDto answerDto = new ExamAnswerDto();
			answerDto.setQuestionId(questionId);
			answerDto.setCorrectAnswer(answer);
			answersList.add(answerDto);
		}
		
		return answersList;
	}

	@Override
	public boolean updateAnswers(List<ExamAnswerDto> answers) {
		int successCount = 0;
		for(ExamAnswerDto answer : answers) {
			successCount += dao.updateAnswers(answer);
		}
		
		return successCount == answers.size();
	}
	
}
