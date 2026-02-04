package com.my.ex.parser.geomjeong.upload.exam;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.my.ex.dto.ExamInfoDto;

/**
 * UploadedGeomjeongPdfTextExtractor 클래스에서 추출된 텍스트 깨끗하게 다듬기, 공백/줄바꿈/순서 정리
 *
 */
@Component
public class GeomjeongPdfTextNormalizer {
	public String normalize(String text) {
		if (text == null || text.isEmpty()) return "";

        // 1. 줄바꿈 통일 (\r\n, \r → \n)
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");

        // 2. 연속 공백 최소화
        text = text.replaceAll("[ \t]{2,}", " ");

        // 3. 앞뒤 공백 제거
        text = text.trim();

        // 4. 선택적 교시 표기 복구만 수행
        text = text.replaceAll("(제)\\s*(\\d+)\\s*(교)\\s*(시)", "$1$2$3$4");

        return text;
	}
	
	// 헌글 띄어쓰기/자간 복구
	private static String normalizeKoreanSpacing(String text) {
		// 1. 교육과정/과목 잘린 경우 복구 (2~3글자)
	    text = text.replaceAll("([가-힣]{1,3})\\s+([가-힣]{1,3})(?=[\\s,.!?\\d]|$)", "$1$2");
	    
	    // 2. 제 1 교시, 제 2 교시 등 숫자 포함
	    text = text.replaceAll("(제)\\s*(\\d+)\\s*(교)\\s*(시)", "$1$2$3$4");

	    return text;
	}
	
	// 문제 순서 재정렬
	private static String reorderQuestionsByNumber(String text) {
		Map<Integer, String> map = new TreeMap<>();
        Matcher m = Pattern.compile("(\\d+)\\.").matcher(text);
        int lastIndex = 0;
        int lastNum = -1;

        while (m.find()) {
            int num = Integer.parseInt(m.group(1));
            int start = m.start();

            if (lastNum != -1) {
                map.put(lastNum, text.substring(lastIndex, start).trim());
            }

            lastNum = num;
            lastIndex = start;
        }

        if (lastNum != -1) {
            map.put(lastNum, text.substring(lastIndex).trim());
        }

        return String.join("\n\n", map.values());
	}

}
