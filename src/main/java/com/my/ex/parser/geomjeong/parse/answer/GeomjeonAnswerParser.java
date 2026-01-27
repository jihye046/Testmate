package com.my.ex.parser.geomjeong.parse.answer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.my.ex.dto.ExamInfoDto;
import com.my.ex.parser.common.IAnswerParser;

public class GeomjeonAnswerParser implements IAnswerParser {
	
	@Autowired
	private ExamInfoDto examInfoDto;
	
	@Override
	public ExamInfoDto getExamInfo() {
		return examInfoDto;
	}

	@Override
	public Map<String, String> parse(String fullText) {
		// TODO Auto-generated method stub
		return null;
	}

}
