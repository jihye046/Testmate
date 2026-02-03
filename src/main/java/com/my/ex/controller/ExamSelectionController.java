package com.my.ex.controller;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.ex.config.EnvironmentConfig;
import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.request.ExamCreateRequestDto;
import com.my.ex.dto.request.ExamCreateRequestDto.CreateExamInfo;
import com.my.ex.dto.request.MoveExamsToFolderDto;
import com.my.ex.dto.response.ExamInfoGroup;
import com.my.ex.dto.response.ExamPageDto;
import com.my.ex.dto.response.ExamPdfPreview;
import com.my.ex.parser.geomjeong.parse.exam.GeomjeongExamParser;
import com.my.ex.service.ExamAnswerService;
import com.my.ex.service.ExamSelectionService;
import com.my.ex.service.IExamAnswerService;
import com.my.ex.service.IExamSelectionService;

// 시험지 선택/조회 등 관리 영역 
@Controller
@RequestMapping("/exam")
public class ExamSelectionController {
	
	@Autowired
//	private ExamSelectionService service;
	private IExamSelectionService service;
	
	@Autowired
//	private ExamAnswerService answerService;
	private IExamAnswerService answerService;
	
	@Autowired
	private EnvironmentConfig config;
	
	@GetMapping("/main")
	public String mainPage(Model model) {
		List<ExamTypeDto> list = service.getExamTypes();
		model.addAttribute("examtypes", list);
		return "/exam/main";
	}
	
	@GetMapping("/getExamTypes")
	@ResponseBody
	public List<ExamTypeDto> getExamTypes(){
		return service.getExamTypes();
	}
	
	@GetMapping("/getExamRounds")
	@ResponseBody
	public ExamInfoGroup getExamInfo(@RequestParam String examTypeCode) {
		List<String> examRounds = service.getExamRounds(examTypeCode);
		
		ExamInfoGroup group = new ExamInfoGroup();
		group.setExamRounds(examRounds);
		
		return group;
	}
	
	@GetMapping("/getSubjectsByExamRound")
	@ResponseBody
	public ExamInfoGroup getExamSubjects(
		@RequestParam String examTypeCode,
		@RequestParam String examRound) 
	{
		List<String> examSubjects = service.getSubjectsByExamRound(examTypeCode, examRound);
		
		ExamInfoGroup group = new ExamInfoGroup();
		group.setExamSubjects(examSubjects);
		
		return group;
	}
	
	@PostMapping("/uploadPdfText")
	@ResponseBody
	public String uploadPdfText(@RequestBody Map<String, String> payload) {
		String text = payload.get("text");
		
		if(text == null || text.trim().isEmpty()) {
			return "error: PDF 텍스트 내용이 비어있습니다.";
		}
		
		GeomjeongExamParser parse = new GeomjeongExamParser();
		List<Map<String, Object>> questions = parse.parse(text);
		// 파싱된 시험 정보 및 문제 리스트 추출
		ExamInfoDto examInfo = parse.getExamInfo();
		examInfo.setExamTypeId(1); // 시험 종류 코드: 중졸 1, 고졸 2
		
		boolean result = service.saveParsedExamData(examInfo, questions);
		if(result) {
			return "✅" + result + ": 총 " + questions.size() + "개의 문제가 저장되었습니다.";
		} else {
			return "❌ 이미 저장된 문제지입니다.";
		}
		
	}

	/**
	 * 사용자 시험 응시 페이지
	 * 
	 * @param examType 시험 종류 (예: "middle-geomjeong")
	 * @param examRound 시험 회차 정보 (예: "2025년도 제1회")
	 * @param examSubject 과목명 (예: "국어")
	 * 
	 * @return ExamPageDto 시험정보 Dto
	 * 		(시험 문제들, 시험지 한글 타입, 시험지 회차, 시험 과목, 선택지, 중복제거된 공통지문 Dto)
	 */
	@GetMapping("/showExamPage")
//	@ResponseBody
	public String showExamPage (
//	public Map<String, Object> showExamPage (
		@RequestParam String examTypeEng,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		Model model) 
	{
		// 시험지인 경우
		String examTypeKor = service.getExamtypename(examTypeEng);
		List<ExamQuestionDto> questions = service.getExamQuestions(examTypeEng, examRound, examSubject);
		if(questions == null || questions.isEmpty()) {
			throw new IllegalStateException("해당 시험에 대한 문제가 존재하지 않습니다.");
		}
		
		// 데이터 추출
		List<ExamChoiceDto> choices = service.getExamChoices(questions.get(0).getExamId());
		Set<ExamPageDto.ExamCommonpassageDto> distinctPassageDto =
				service.getCommonPassageInfo(examTypeEng, examRound, examSubject); // 공통지문 시작번호 추출
		
		// 데이터 담기
		ExamPageDto response = 
				new ExamPageDto(questions, examTypeKor, examRound, examSubject, choices, distinctPassageDto);
		model.addAttribute("examPageDto", response);
		
		return "/exam/exam_page";
			
		
//		Map<String, Object> map = new HashMap<>();
//		String examTypename = service.getExamtypename(examType);
//		List<ExamQuestionDto> questions = service.getExamQuestions(examType, examRound, examSubject);
//		List<ExamChoiceDto> choices = service.getExamChoices();
//		Set<ExamCommonpassageDto> distinctPassageDto = service.getCommonPassageInfo(examType, examRound, examSubject); // 공통지문 시작번호 추출
//		ExamPageDto response = new ExamPageDto(questions, examTypename, examRound, examSubject, choices, distinctPassageDto);
//		map.put("examPageDto", response);
//	
//		return map;
		
	}
	
	@GetMapping("/getExamImagePath")
	@ResponseBody
	public Resource getExamImagePath(
		@RequestParam String examType,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		@RequestParam String filename)
	{
		String path = 
				config.getImageUploadPath() +
				examType + File.separator +
				examRound + File.separator +
				examSubject + File.separator +
				filename;
		// C:\server_program\project\testmate\images\2025년도 제1회\국어
		File file = new File(path);
		if (file.exists()) {
			return new FileSystemResource(file); // file자체를 보내는 것은 X, HTTP 본문 응답으로 자동 변환해주는 API(FileSystemResource())를 이용해서 보내야 함
		} else {
			throw new RuntimeException("File not found: " + file.getAbsolutePath());
		}
	}
	
	/**
	 * 시험지 삭제 요청 처리
	 * - 단일 삭제와 일괄 삭제 요청을 동일한 엔드포인트로 처리하기 위해 
	 * 	 단일 삭제 요청도 List<Integer>로 받음
	 * 
	 * @param MoveExamToFolderDto 삭제할 시험지 ID 리스트를 담은 DTO 
	 * 
	 * @return true/false
	 */
	@PatchMapping("/deleteExams")
	@ResponseBody
	public boolean deleteExams(@RequestBody MoveExamsToFolderDto dto) {
		return service.deleteExams(dto.getExamIds());
	}
	
	/**
	 * 선택한 시험 유형의 전체 과목들을 조회
	 * 
	 * @param examTypeCode
	 * @return List<String> 과목들
	 */
	@GetMapping("/getSubjectsForExamType")
	@ResponseBody
	public List<String> getSubjectsForExamType(@RequestParam String examTypeCode) {
		return service.getSubjectsForExamType(examTypeCode);
	}
	
	/**
	 * 관리자가 시험지를 직접 작성하여 등록하는 경우
	 * @param examInfoStr
	 * @param questionsStr
	 * @param fileMap
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/saveExamByForm")
	@ResponseBody
	public boolean saveExamByForm(
			@RequestParam("examInfo") String examInfoStr,
			@RequestParam("questions") String questionsStr,
			@RequestParam Map<String, MultipartFile> fileMap) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		ExamCreateRequestDto requestDto = new ExamCreateRequestDto();
		
		requestDto.setExamInfo(mapper.readValue(examInfoStr, CreateExamInfo.class));
		requestDto.setQuestions(
				mapper.readValue(
						questionsStr,
						new TypeReference<List<ExamCreateRequestDto.Questions>>() {}
				)
		);
		requestDto.setFileMap(fileMap);
		
		return service.saveExamByForm(requestDto);
	}
	
	/**
	 * 관리자가 시험지 PDF를 업로드하여 등록하는 경우
	 * @param examInfo
	 * @param pdfFile
	 * @param folderId
	 * @return
	 */
	@PostMapping("/loadPdfFile")
	@ResponseBody
	public ExamPdfPreview loadPdfFile(
			@RequestParam String examInfo,
			@RequestParam MultipartFile pdfFile,
			@RequestParam Integer folderId)
	{
		
		try {
			// 시험지 정보 매핑
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> examInfoMap = mapper.readValue(examInfo, new TypeReference<Map<String, String>>() {});
			String examTypeCode = examInfoMap.get("examTypeCode");
			String examRound = examInfoMap.get("examRound");
			String examSubject = examInfoMap.get("examSubject");
			
			ExamInfoDto examInfoDto = new ExamInfoDto(examRound, examSubject);
			examInfoDto.setExamTypeId(service.findTypeIdByCode(examTypeCode));
			examInfoDto.setFolderId(folderId);
			
			boolean result = false;
			List<Map<String, Object>> questions = null;
			
			// 검정고시 시험지인 경우
			if(examTypeCode.endsWith("geomjeong")) {
				// 1. 시험지 PDF 텍스트 추출 후 파싱된 전체 문제 리스트를 반환
				questions = service.parsePdfToQuestions(pdfFile);
				if(questions == null || questions.isEmpty()) {
					return new ExamPdfPreview(false, "PDF에서 시험지를 추출할 수 없습니다.", null);
				}
				
				// 2. 시험지 등록 
				result = service.saveParsedExamData(examInfoDto, questions);
			} 
			
			// 검정고시 답안지인 경우
			else if(examTypeCode.endsWith("geomjeongAnswer")) { // 문자열 비교하는 부분을 enum이나, 상수로 빼는것을 고려
				// 1. 정답지 PDF 텍스트 추출
				Map<Integer, String> answerMap = answerService.parsePdfToAnswers(pdfFile);
				
				// 2. 정답지와 연결될 시험지의 examTypeCode를 이용해 examTypeId 조회 및 설정
				String examTypeCodeWithoutAnswer = answerService.examTypeCodeWithoutAnswer(examTypeCode);
				int mappedExamTypeId = service.findTypeIdByCode(examTypeCodeWithoutAnswer);
				examInfoDto.setExamTypeId(mappedExamTypeId);
				
				// 3. 정답지의 examTypeCode를 이용해 examTypeId 조회
				int originalExamTypeId  = service.findTypeIdByCode(examTypeCode);
				
				// 4. db저장용 구조로 변환
				List<ExamAnswerDto> answersList = answerService.buildParsedAnswerData(answerMap, examInfoDto);
				if(answersList == null || answersList.isEmpty()) {
					return new ExamPdfPreview(false, "시험지를 먼저 등록하신 후 정답지를 등록해주세요", null);
				}
				
				// 5. 정답지 등록
				/* 문제지 examTypeId로 저장되어있던 것을 정답지 examTypeId로 다시 설정하여 정답지 등록 */
				examInfoDto.setExamTypeId(originalExamTypeId);
				result = answerService.saveParsedAnswerData(examInfoDto, answersList);
			}
			
			// 시험지/정답지 등록 결과에 따른 응답처리
			if(!result) {
				String folderName = service.findExistingExamFolderId(examInfoDto);
				return new ExamPdfPreview(false, '"' + folderName + '"' + " 폴더에 이미 등록된 문서입니다.", null);
			}
			
			return new ExamPdfPreview(true, "파일이 등록되었습니다.", questions); 
		} catch (Exception e) {
			e.printStackTrace();
			return new ExamPdfPreview(false, "잘못된 입력값입니다.", null);
		}
	}
	
	@PatchMapping("/updateAnswers")
	@ResponseBody
	public boolean updateAnswers(@RequestBody Map<String, List<ExamAnswerDto>> answers) {
		return answerService.updateAnswers(answers.get("answers"));
	}
	
}
