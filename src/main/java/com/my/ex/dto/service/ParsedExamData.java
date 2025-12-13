package com.my.ex.dto.service;

import java.util.List;
import java.util.Map;

import com.my.ex.dto.ExamInfoDto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 서비스단의 buildParsedExamData() 메서드에서 요청 데이터를 DB 저장용 구조로 변환한 결과를 담는 DTO.
 * 관리자가 PDF 업로드 또는 직접 시험지 작성 시 사용되며, 
 * 서비스단의 saveParsedExamData() 호출을 위해 ExamInfoDto와 문제 리스트를 보관
 * 
 * 외부 API 요청/응답 DTO가 아니며, DB 테이블 매핑 DTO도 아님
 * 
 * @author jihye
 */
@Data
@AllArgsConstructor
public class ParsedExamData {
	private ExamInfoDto examInfo;
	private List<Map<String, Object>> newList;
}
