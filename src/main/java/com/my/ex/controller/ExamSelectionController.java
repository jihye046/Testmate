package com.my.ex.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.ex.config.EnvironmentConfig;
import com.my.ex.dto.*;
import com.my.ex.dto.request.ExamCreateRequestDto;
import com.my.ex.dto.request.ExamCreateRequestDto.CreateExamInfo;
import com.my.ex.dto.request.ExamCreateRequestDto.Question;
import com.my.ex.dto.request.MoveExamsToFolderDto;
import com.my.ex.dto.response.ExamInfoGroup;
import com.my.ex.dto.response.ExamPageDto;
import com.my.ex.dto.response.ExamPdfPreview;
import com.my.ex.parser.geomjeong.parse.exam.GeomjeongExamParser;
import com.my.ex.service.IExamAnswerService;
import com.my.ex.service.IExamSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 시험지 선택/조회 등 관리 영역 
@Controller
@RequestMapping("/exam")
public class ExamSelectionController {
	
	@Autowired
	private IExamSelectionService service;
	
	@Autowired
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
	 * @param examTypeEng 시험 종류 (예: "middle-geomjeong")
	 * @param examRound 시험 회차 정보 (예: "2025년도 제1회")
	 * @param examSubject 과목명 (예: "국어")
	 * 
	 * @return ExamPageDto 시험정보 Dto
	 * 		(시험 문제들, 시험지 한글 타입, 시험지 회차, 시험 과목, 선택지, 중복제거된 공통지문 Dto)
	 */
	@GetMapping("/showExamPage")
	public String showExamPage (
		@RequestParam String examTypeEng,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		Model model) 
	{
		/* 데이터 추출 */
		// 1. 시험지 문제
		String examTypeKor = service.getExamtypename(examTypeEng);
		List<ExamQuestionDto> questions = service.getExamQuestions(examTypeEng, examRound, examSubject);
		if(questions == null || questions.isEmpty()) {
			throw new IllegalStateException("해당 시험에 대한 문제가 존재하지 않습니다.");
		}

		// 2. 시험지 정보
		int examId = service.getExamIdByExamTypeId(examTypeEng, examRound, examSubject);
		int examTypeId = service.getExamTypeIdByExamTypeCode(examTypeEng);
		
		// 3. 선택지
		List<ExamChoiceDto> choices = service.getExamChoicesByExamId(questions.get(0).getExamId());
		
		// 4. 공통 지문
		Set<ExamPageDto.ExamCommonpassageDto> distinctPassageDto =
				service.getCommonPassageInfo(examTypeEng, examRound, examSubject); // 공통지문 시작번호 추출
		
		/* 데이터 담기 */
		ExamPageDto response = 
				new ExamPageDto(
						questions, 
						examId, 
						examTypeId, 
						examTypeKor, 
						examRound, 
						examSubject, 
						choices, 
						distinctPassageDto
				);
		model.addAttribute("examPageDto", response);
		
		return "/exam/exam_page";
	}
	
	@GetMapping("/getExamImagePath")
	@ResponseBody
	public Resource getExamImagePath(
		@RequestParam(required = false, defaultValue = "") String examType,
		@RequestParam(required = false, defaultValue = "") String examRound,
		@RequestParam(required = false, defaultValue = "") String examSubject,
		@RequestParam String filename)
	{
		Path filePath;
		String baseDir = config.getImageUploadPath();
		
		if(examType.isEmpty() && examRound.isEmpty() && examSubject.isEmpty()) {
			filePath = Paths.get(baseDir, "temp", filename);
		} else {
			filePath = Paths.get(baseDir, examType, examRound, examSubject, filename);
		}
		
		// C:\server_program\project\testmate\images\2025년도 제1회\국어
		File file = filePath.toFile();
		if (file.exists()) {
			return new FileSystemResource(file); // file자체를 보내는 것은 X, HTTP 본문 응답으로 자동 변환해주는 API(FileSystemResource())를 이용해서 보내야 함
		} else {
			throw new RuntimeException("File not found: " + file.getAbsolutePath());
		}
	}
	
	/**
	 * 에디터 이미지 임시 저장 처리
	 * @param image 에디터에 업로드한 이미지 파일
	 * @return fileName(저장된 파일명), status(성공여부)를 포함한 Map
	 */
	@PostMapping("/uploadEditorImage")
	@ResponseBody
	public Map<String, Object> uploadEditorImage(@RequestParam MultipartFile image) {
		Map<String, Object> response = new HashMap<>();
		try {
			String savedFileName = service.saveEditorImage(image);
			
			response.put("fileName", savedFileName);
			response.put("status", "success");
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", e.getMessage());
		}
		
		return response;
	}
	
	/**
	 * 시험지 삭제 요청 처리
	 * - 단일 삭제와 일괄 삭제 요청을 동일한 엔드포인트로 처리하기 위해 
	 * 	 단일 삭제 요청도 List<Integer>로 받음
	 * 
	 * @param dto 삭제할 시험지 ID 리스트를 담은 DTO
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
	 * @param examTypeCode "middle-geomjeong"
	 * @return List<String> 과목들
	 */
	@GetMapping("/getSubjectsForExamType")
	@ResponseBody
	public List<String> getSubjectsForExamType(@RequestParam String examTypeCode) {
		return service.getSubjectsForExamType(examTypeCode);
	}
	
	/**
	 * 관리자가 시험지를 직접 작성하여 등록하는 경우
	 */
	@PostMapping("/saveExamByForm")
	@ResponseBody
	public boolean saveExamByForm(
			@RequestParam("examInfo") String examInfoStr,
			@RequestParam("questions") String questionsStr,
			@RequestParam(required = false) Map<String, MultipartFile> fileMap) throws IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		ExamCreateRequestDto requestDto = new ExamCreateRequestDto();
		
		requestDto.setExamInfo(mapper.readValue(examInfoStr, CreateExamInfo.class));
		requestDto.setQuestions(
				mapper.readValue(
						questionsStr,
						new TypeReference<List<ExamCreateRequestDto.Question>>() {}
				)
		);
		requestDto.setFileMap(fileMap);
		
		return service.saveExamByForm(requestDto);
	}
	
	/**
	 * 관리자가 시험지 PDF를 업로드하여 등록하는 경우
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
					String message = 
							"PDF에서 시험지를 추출할 수 없습니다. \n" +
							"정답지 PDF를 업로드하려면 시험 유형을 '정답지'로 선택해주세요.";
					return new ExamPdfPreview(false, message, null);
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
	
	@GetMapping("/editQuestion/{questionId}/{examId}")
	public String editQuestionPage(
			@PathVariable int questionId,
			@PathVariable int examId,
			Model model) 
	{
		ExamInfoDto examInfo = service.getExamInfoByExamId(examId);
		String examTypeEng = examInfo.getExamTypeEng();
		String examTypeKor = examInfo.getExamTypeKor();
		String examRound = examInfo.getExamRound();
		String examSubject = examInfo.getExamSubject();
		
		// 1. 시험지 문제 
		ExamQuestionDto question = service.getExamQuestionByQuestionId(questionId);
		if(question == null) {
			throw new IllegalStateException("해당 시험에 대한 문제가 존재하지 않습니다.");
		}
		question.setExamId(examId);
		question.setQuestionId(questionId);
		
		// 2. 선택지
		List<ExamChoiceDto> choices = service.getExamChoicesByQuestionId(questionId);
		
		// 3. 정답지
		ExamAnswerDto answer = answerService.getAnswerByQuestionId(questionId);
		if(answer != null) {
			answer.setQuestionNum(question.getQuestionNum());
		}
		
		// 4. 폴더id
		int folderId = service.findFolderIdByExamId(examId);
		
		/* 데이터 담기 */
		ExamPageDto response = 
				new ExamPageDto(question, examTypeEng, examTypeKor, examRound, examSubject, choices, answer, folderId);
		model.addAttribute("examPageDto", response); // jsp용
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String examPageDtoJson = mapper.writeValueAsString(response);
			model.addAttribute("examPageDtoJson", examPageDtoJson); // js용
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("JSON 변환 실패", e);
		}
		
		return "/admin/exam_edit_page";
	}
	
	@PostMapping("/updateExamByForm")
	@ResponseBody
	public String updateExamByForm(
			@RequestParam(value="examInfo", required = false) String examInfoStr,
			@RequestParam(value="question", required = false) String questionStr,
			@RequestParam(required = false) Map<String, MultipartFile> fileMap) throws IOException 
	{
		
		ObjectMapper mapper = new ObjectMapper();
		CreateExamInfo createExamInfo = mapper.readValue(examInfoStr, CreateExamInfo.class);
		Question question = mapper.readValue(questionStr, Question.class);
		
		ExamCreateRequestDto requestDto = new ExamCreateRequestDto();
		requestDto.setExamInfo(createExamInfo);
		requestDto.setQuestion(question);
		requestDto.setFileMap(fileMap);
		
		service.updateExamByForm(requestDto);
		
		Integer examId = requestDto.getExamInfo().getExamId();
		String examTypeEng = requestDto.getExamInfo().getType();
		String examTypeKor = requestDto.getExamInfo().getTypeKor();
		String examRound = requestDto.getExamInfo().getRound();
		String examSubject = requestDto.getExamInfo().getSubject();
		return "/admin/showExamPage?"
				+ "examId=" + examId + "&"
				+ "examTypeEng=" + examTypeEng + "&"
				+ "examTypeKor=" + examTypeKor + "&"
				+ "examRound=" + examRound + "&"
				+ "examSubject=" + examSubject;
	}
	
	@PostMapping("/checkAnswers")
	@ResponseBody
	public Map<String, Object> checkAnswers(@RequestParam Map<String, Object> map) {
		return answerService.checkAnswers(map);
	}
}
