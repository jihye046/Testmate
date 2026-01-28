package com.my.ex.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.ibatis.mapping.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.my.ex.config.EnvironmentConfig;
import com.my.ex.dao.ExamSelectionDao;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.request.ExamCreateRequestDto;
import com.my.ex.dto.request.ExamCreateRequestDto.Questions;
import com.my.ex.dto.request.ExamCreateRequestDto.Questions.QuestionChoices;
import com.my.ex.dto.response.ExamPageDto;
import com.my.ex.dto.response.ExamTitleDto;
import com.my.ex.dto.service.ParsedExamData;
import com.my.ex.parser.geomjeong.upload.exam.UploadedGeomjeongPdfTextExtractor;
import com.my.ex.parser.geomjeong.parse.exam.GeomjeongExamParser;
import com.my.ex.parser.geomjeong.upload.exam.GeomjeongPdfTextNormalizer;

import groovyjarjarantlr4.v4.parse.ANTLRParser.throwsSpec_return;

@Service
public class ExamSelectionService implements IExamSelectionService {

	@Autowired
	private ExamSelectionDao dao;
	
	@Autowired
	private EnvironmentConfig config;
	
	@Autowired
	private UploadedGeomjeongPdfTextExtractor extractor;
	
	@Autowired
	private GeomjeongPdfTextNormalizer normalizer;
	
	@Autowired
	private GeomjeongExamParser gedExamParser;
	
	@Override
	public List<ExamTypeDto> getExamTypes() {
		return dao.getExamTypes();
	}

	@Override
	public List<String> getExamRounds(String examTypeCode) {
		return dao.getExamRounds(examTypeCode);
	}

	@Override
	public List<String> getSubjectsByExamRound(String examTypeCode, String examRound) {
		Map<String, String> map = new HashMap<>();
		map.put("examTypeCode", examTypeCode);
		map.put("examRound", examRound);
		
		return dao.getSubjectsByExamRound(map);
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
		
		// 폴더 id 저장
		if(examInfo.getFolderId() == null) {
			examInfo.setFolderId(1);
		}
		
		// 시험 정보 저장
		dao.saveParsedExamInfo(examInfo); // 시험 정보 저장 후 examId으로 설정됨
		Integer examId = (Integer)examInfo.getExamId(); // DB 삽입 후 주입된 examId 가져옴
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
	public Set<ExamPageDto.ExamCommonpassageDto> getCommonPassageInfo(String examType, String examRound, String examSubject) {
		Map<String, Object> map = new HashMap<>();
		map.put("examType", examType);
		map.put("examRound", examRound);
		map.put("examSubject", examSubject);
		List<ExamQuestionDto> questionDto = dao.getCommonPassageInfo(map);
		
//		ExamCommonpassageDto commonpassageDto; 
//		List<ExamCommonpassageDto> distinctPassageList = new ArrayList<>();
		Set<ExamPageDto.ExamCommonpassageDto> distinctPassageSet = new HashSet<>();
		
		for(ExamQuestionDto dto: questionDto) {
			ExamPageDto.ExamCommonpassageDto commonpassageDto = new ExamPageDto.ExamCommonpassageDto();
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

	@Override
	public int findTypeIdByCode(String type) {
		return dao.findTypeIdByCode(type);
	}

	@Override
	public boolean saveExamByForm(ExamCreateRequestDto request) {
		ParsedExamData data = buildParsedExamData(request);
		
		return saveParsedExamData(data.getExamInfo(), data.getNewList());
	}

	/**
	 * ExamCreateRequestDto(요청 데이터)를 DB 저장용 구조로 변환하고
	 * ExamInfoDto와 문제 리스트(Map 포함)를 조립하여 ParsedExamData로 반환.
	 * 
	 * 관리자가 PDF 업로드 또는 직접 작성한 시험지를 저장할 때
	 * saveParsedExamData() 호출 전에 사용됨.
	 */
	@Override
	public ParsedExamData buildParsedExamData(ExamCreateRequestDto request) {
		// 시험 코드로 부터 시험 종류 조회(middle-geomjeong → 중졸 검정고시)
		
		ExamInfoDto examInfo = new ExamInfoDto();
		String subject = request.getExamInfo().getSubject();
		String round = request.getExamInfo().getRound();
		int examTypeId = findTypeIdByCode(request.getExamInfo().getType());
		// 시험지 정보
		examInfo.setExamRound(round);
		examInfo.setExamSubject(subject);
		examInfo.setExamTypeId(examTypeId);
		examInfo.setFolderId(request.getExamInfo().getFolderId());
		
		List<Questions> questions = request.getQuestions();
		List<Map<String, Object>> newList = new ArrayList<>();
		for(Questions q : questions) {
			Map<String, Object> map = new HashMap<>();
			// 문제번호
			map.put("questionNum", q.getQuestionNum());
			
			// 문제
			map.put("questionText", q.getQuestionText());
			
			// 개별지문
			map.put("useIndividualPassage", q.getUseIndividualPassage());
			if(q.getUseIndividualPassage() == 'Y') {
				if(q.getIndividualPassage().getType().equals("image")) {
					// 폴더이름 생성
					String typename = getExamtypename(request.getExamInfo().getType());
					String folderPath = typename + File.separator +
							round + File.separator +
							subject;
					
					// 파일이름 생성
					String filename = q.getIndividualPassage().getContent().trim().replace(" ", "_");
					String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
				
					// 파일 저장
	                String fileKey = q.getIndividualPassage().getFileKey();
	                MultipartFile file = request.getFileMap().get(fileKey);
					ensureImageFolderExists(folderPath, uniqueFilename, file);
					map.put("individualPassage", uniqueFilename);
				} else if(q.getIndividualPassage().getType().equals("text")) {
					map.put("individualPassage", q.getIndividualPassage().getContent());
				}
			} 
			
			// 공통지문
			map.put("useCommonPassage", q.getUseCommonPassage());
			if(q.getUseCommonPassage() == 'Y') {
				if(q.getCommonPassage().getType().equals("image")) {
					// 폴더이름 생성
					String typename = getExamtypename(request.getExamInfo().getType());
					String folderPath = typename + File.separator +
							round + File.separator +
							subject;
					
					// 파일이름 생성
					String filename = q.getCommonPassage().getContent().trim().replace(" ", "_");
					String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
					map.put("commonPassage", uniqueFilename);
					map.put("passageScope", q.getCommonPassage().getRangeText());
					
					// 파일 저장
	                String fileKey = q.getCommonPassage().getFileKey();
	                MultipartFile file = request.getFileMap().get(fileKey);
					ensureImageFolderExists(folderPath, uniqueFilename, file);
				} else if(q.getCommonPassage().getType().equals("text")) {
					map.put("commonPassage", q.getCommonPassage().getContent());
					map.put("passageScope", q.getCommonPassage().getRangeText());	
				}
				
			}
			
			// 선택지
			List<QuestionChoices> choices = q.getQuestionChoices();
			List<ExamChoiceDto> newChoiceList = new ArrayList<>();
			for(QuestionChoices c : choices) {
				newChoiceList.add(new ExamChoiceDto(c.getChoiceLabel(), c.getChoiceText(), c.getChoiceNum()));
			}
			map.put("options", newChoiceList);
			newList.add(map);
		}
		
		return new ParsedExamData(examInfo, newList);
	}

	@Override
	public void ensureImageFolderExists(String folderPath, String filename, MultipartFile file) {
		if(config.getImageStorageType().equals("local")) {
			Path fullPath = Paths.get(config.getImageUploadPath(), folderPath);
			
			// 폴더 생성
			try {
				if(Files.notExists(fullPath)) {
					Files.createDirectories(fullPath);
				}
				
				// 파일 저장
				Path filePath = fullPath.resolve(filename);
				InputStream is = file.getInputStream();
				Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(config.getImageStorageType().equals("gcs")) {
			// TODO: GCS API 사용해서 폴더 확인 및 생성
		} else {
			throw new IllegalArgumentException("알 수 없는 이미지 저장소 타입: " + config.getImageStorageType());
		}
	}

	/**
	 * 관리자가 업로드한 PDF를 읽음
	 */
	@Override
	public List<Map<String, Object>> parsePdfToQuestions(MultipartFile file) throws Exception {
		// 1. PDF 텍스트 추출
		String text = extractor.extract(file);
		
		// 2. 한글 spacing 정리 및 좌/우 단 섞임 복구
		text = normalizer.normalize(text);
//		text = PdfTextNormalizer.normalize(text);
		
		if(text == null || text.trim().isEmpty()) {
			throw new Exception("PDF에서 텍스트를 읽을 수 없습니다. PDF 파일인지 확인해주세요.");
		}
		
		// 3. 파서를 이용하여 텍스트를 List<Map> 구조로 변환
		return gedExamParser.parse(text);
	}

	/**
	 * 중복된 시험지를 등록하려는 경우 어느 폴더에 등록되었는지 알려주기 위해 folderId를 조회
	 */
	@Override
	public String findExistingExamFolderId(ExamInfoDto examInfoDto) {
		return dao.findExistingExamFolderId(examInfoDto);
	}

}
