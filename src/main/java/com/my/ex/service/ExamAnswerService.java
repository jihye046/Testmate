package com.my.ex.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.my.ex.parser.geomjeong.upload.answer.UploadedGeomjeongAnswerPdfTextExtractor;

@Service
public class ExamAnswerService implements IExamAnswerService {
	
	@Autowired
	private UploadedGeomjeongAnswerPdfTextExtractor extractor;
	
	@Override
	public Map<String, String> parsePdfToAnswers(MultipartFile file) {
		// 1. PDF 텍스트 추출
		
		return null;
	}
	
}
