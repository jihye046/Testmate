package com.my.ex.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamInfoGroup;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.parser.ExamInfo;
import com.my.ex.parser.GedExamParser;
import com.my.ex.service.ExamSelectionService;

@Controller
@RequestMapping("/exam")
public class ExamSelectionController {
	
	@Autowired
	private ExamSelectionService service;
	
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
			@RequestParam String examRound) {
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
	
	@GetMapping("/showExamPage")
	public String showExamPage(ExamInfo params) {
		System.out.println(params);
		
		return "/exam/exam_page";
	}
	
}
