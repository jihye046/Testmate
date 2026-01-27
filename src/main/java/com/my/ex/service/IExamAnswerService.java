package com.my.ex.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface IExamAnswerService {
	Map<String, String> parsePdfToAnswers(MultipartFile file);
}
