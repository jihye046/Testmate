package com.my.ex.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.my.ex.config.EnvironmentConfig;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamCommonpassageDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamInfoGroup;
import com.my.ex.dto.ExamPageDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.parser.GedExamParser;
import com.my.ex.service.ExamSelectionService;

@Controller
@RequestMapping("/exam")
public class ExamSelectionController {
	
	@Autowired
	private ExamSelectionService service;
	
	@Autowired
	private EnvironmentConfig config;
	
	@GetMapping("/main")
	public String mainPage(Model model) {
		List<ExamTypeDto> list = service.getExamTypes();
		model.addAttribute("examtypes", list);
		return "/exam/main";
	}
	
	@GetMapping("/getExamRounds")
	@ResponseBody
	public ExamInfoGroup getExamInfo(@RequestParam String examTypeCode) {
		List<String> examRounds = service.getExamRounds(examTypeCode);
		
		ExamInfoGroup group = new ExamInfoGroup();
		group.setExamRounds(examRounds);
		
		return group;
	}
	
	@GetMapping("/getExamSubjects")
	@ResponseBody
	public ExamInfoGroup getExamSubjects(
		@RequestParam String examTypeCode,
		@RequestParam String examRound) 
	{
		List<String> examSubjects = service.getExamSubjects(examTypeCode, examRound);
		
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
		
		GedExamParser parse = new GedExamParser();
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
	 * 시험 조건에 해당하는 시험 정보를 조회
	 * @param examType 시험 종류 (예: "middle-geomjeong")
	 * @param examRound 시험 회차 정보 (예: "2025년도 제1회")
	 * @param examSubject 과목명 (예: "국어")
	 * @return
	 */
	@GetMapping("/showExamPage")
//	@ResponseBody
	public String showExamPage (
//	public Map<String, Object> showExamPage (
		@RequestParam String examType,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		Model model) 
	{
		
		String examTypename = service.getExamtypename(examType);
		List<ExamQuestionDto> questions = service.getExamQuestions(examType, examRound, examSubject);
		List<ExamChoiceDto> choices = service.getExamChoices();
		Set<ExamCommonpassageDto> distinctPassageDto = service.getCommonPassageInfo(examType, examRound, examSubject); // 공통지문 시작번호 추출
		ExamPageDto response = new ExamPageDto(questions, examTypename, examRound, examSubject, choices, distinctPassageDto);
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
				examType + "\\" +
				examRound + "\\" +
				examSubject + "\\" +
				filename;
		// C:\server_program\project\testmate\images\2025년도 제1회\국어
		File file = new File(path);
		if (file.exists()) {
			return new FileSystemResource(file); // file자체를 보내는 것은 X, HTTP 본문 응답으로 자동 변환해주는 API(FileSystemResource())를 이용해서 보내야 함
		} else {
			throw new RuntimeException("File not found: " + file.getAbsolutePath());
		}
	}
	
}
