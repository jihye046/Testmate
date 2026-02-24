package com.my.ex.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.my.ex.dao.ExamAnswerDao;
import com.my.ex.dao.ExamSelectionDao;
import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.request.ExamCreateRequestDto.Question.QuestionAnswer;
import com.my.ex.dto.response.ExamPassPolicy;
import com.my.ex.dto.response.ExamResultDto;
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
//	@Transactional
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
	public List<ExamAnswerDto> getAnswersByExamId(int examId) {
		return dao.getAnswersByExamId(examId);
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
	
	// 정답지와 연결될 시험지의 examTypeCode에 Answer를 붙임
	@Override
	public String examTypeCodeWithAnswer(String examTypeCode) {
		if(examTypeCode.endsWith("Answer")) {
			return examTypeCode;
		}
		return examTypeCode + "Answer";
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

	@Override
	public List<ExamAnswerDto> getAnswersByQuestionIds(List<Integer> questionIds) {
		return dao.getAnswersByQuestionIds(questionIds);
	}

	@Override
	public ExamAnswerDto getAnswerByQuestionId(Integer questionId) {
		return dao.getAnswerByQuestionId(questionId);
	}
	
	public void updateQuestionAnswer(Integer questionId, QuestionAnswer answer, Integer examId) {
		Integer answerId = answer.getAnswerId();
		int correctAnswer = answer.getCorrectAnswer();
		
		if((answerId == null || answerId == 0) 
				&& correctAnswer > 0) {
			ExamAnswerDto dto = new ExamAnswerDto(questionId, correctAnswer, examId);
			dao.saveParsedAnswerData(dto);
		} else {
			ExamAnswerDto dto = new ExamAnswerDto(answerId, correctAnswer);
			dao.updateQuestionAnswer(dto);
		}
	}

	@Override
	public Map<String, Object> checkAnswers(Map<String, Object> map) {
		// 0. 필수 파라미터 검증 (시험 ID 존재 여부 확인)
		if(map.get("examId") == null) throw new IllegalArgumentException("examId가 없음");
		int examId = Integer.parseInt(map.get("examId").toString());
		
		List<ExamResultDto> questionResults = new ArrayList<>();
		
		// 1. 전체 문항 조회 (채점 기준 데이터)
		// 해당 시험(examId)의 전체 문항 ID 조회
		List<Integer> allQuestionIds = examSelectionDao.getQuestionIdByExamId(examId);
		

		// 2. 사용자가 제출한 답안 추출
		// key 형식: question_XXX (XXX = questionId)
		List<ExamChoiceDto> submittedDtos = new ArrayList<>();
		
		for(String key : map.keySet()) {
			if(!key.equals("examId") && key.contains("_")) {
				// key에서 questionId 추출
				String questionIdStr = key.substring(key.indexOf("_") + 1);
				Integer questionId = Integer.parseInt(questionIdStr);
				
				// key를 이용해서 사용자가 선택한 choiceId 추출
				int choiceId = (Integer.parseInt(map.get(key).toString()));
				
				submittedDtos.add(new ExamChoiceDto(choiceId, questionId));
			} 
		}
		
		
		// 3. 사용자가 제출한 문제 채점 처리
		// 제출된 답안이 존재하는 경우에만 채점 
		if(!submittedDtos.isEmpty()) {
			questionResults.addAll(dao.checkAnswers(submittedDtos));
		}
		
		
		// 4. 미제출(미응답) 문제 처리
		// 제출된 문제들의 questionId만 Set으로 추출
		Set<Integer> submittedIds = submittedDtos.stream()
				.map(ExamChoiceDto::getQuestionId)
				.collect(Collectors.toSet());

		// 전체 문항 중 제출되지 않은 questionId 추출
		List<Integer> missedIds = allQuestionIds.stream()
				.filter(id -> !submittedIds.contains(id))
				.collect(Collectors.toList());
		
		if(!missedIds.isEmpty()) {
			// 미응답 문항의 정답을 한번에 조회
			List<ExamResultDto> missedAnswers = dao.findAnswersByQuestionIds(missedIds);
			for(ExamResultDto dto : missedAnswers) {
				dto.setChoiceId(0);    // 사용자 미선택
				dto.setUserAnswer(0);  // 사용자 미선택
				dto.setIsCorrect("N"); // 오답 처리
				questionResults.add(dto);
			}
		}
		
		
		// 5. 점수 계산
		long correctCount = questionResults.stream()
				.filter(r -> r.getIsCorrect().equals("Y"))
				.count();
		int totalScore = (int) (100 / allQuestionIds.size() * correctCount); 
		
		
		// 6. 합격 여부 판단
		int examTypeId = Integer.parseInt(map.get("examTypeId").toString());
		boolean isPassed = ExamPassPolicy.checkPass(examTypeId, totalScore);
		
		
		// 7. 응답
		Map<String, Object> response = new HashMap<>();
		response.put("result", questionResults);
		response.put("totalScore", totalScore);
		response.put("isPassed", isPassed);
		
		return response;
	}
	
}
