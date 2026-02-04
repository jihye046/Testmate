package com.my.ex.parser.common;

import java.util.List;
import java.util.Map;

import com.my.ex.dto.ExamInfoDto;

public interface IExamParser {
	/**
	 * 파싱된 시험 기본 정보를 반환
	 * @return ExamInfo 객체
	 * */
	ExamInfoDto getExamInfo();
	
	/**
	 * 전체 텍스트를 파싱하여 문제 데이터 리스트를 반환
	 * @param fullText 전체 시험지 텍스트
	 * @return 파싱된 문제 및 선택지 데이터 맵 리스트 
	 */
	List<Map<String, Object>> parse(String fullText);
}
