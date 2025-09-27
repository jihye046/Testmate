package com.my.ex.controller;

import java.util.HashMap;
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

import com.my.ex.dto.ExamInfoGroup;
import com.my.ex.dto.ExamTypeDto;
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
//		System.out.println("text: " + text);
		GedExamParser parse = new GedExamParser();
		parse.parse(text);
		
		return "success";
	}
	
}
