package com.my.ex.parser.geomjeong.parse.answer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.my.ex.dto.ExamInfoDto;
import com.my.ex.parser.common.IAnswerParser;

@Component
public class GeomjeongAnswerParser implements IAnswerParser {
	
	private ExamInfoDto examInfoDto;
	
	@Override
	public ExamInfoDto getExamInfo() {
		return examInfoDto;
	}

	@Override
	public Map<Integer, String> parse(String fullText) {
		Pattern pattern = Pattern.compile("(\\d+)\\s*([①②③④⑤])");
		Matcher matcher = pattern.matcher(fullText);
		
		Map<Integer, String> answers = new HashMap<>();
		while(matcher.find()) {
			int questionNum = Integer.parseInt(matcher.group(1));
			String answer = matcher.group(2);
			answers.put(questionNum, answer);
		}
		return answers;
	}
	
	
	
}
