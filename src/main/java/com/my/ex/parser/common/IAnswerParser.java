package com.my.ex.parser.common;

import java.util.Map;

import com.my.ex.dto.ExamInfoDto;

public interface IAnswerParser {
	ExamInfoDto getExamInfo();
	Map<Integer, String> parse(String fullText);
}
