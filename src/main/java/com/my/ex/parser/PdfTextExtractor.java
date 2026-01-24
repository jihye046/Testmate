package com.my.ex.parser;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * PDF에서 원본 텍스트 추출
 * @author jihye
 *
 */
@Component
public class PdfTextExtractor {
	public String extract(MultipartFile file) throws IOException {
		try (InputStream is = file.getInputStream();
	             PDDocument document = PDDocument.load(is)) {

	            PDFTextStripperByArea areaStripper = new PDFTextStripperByArea();
	            areaStripper.setSortByPosition(true);

	            StringBuilder fullText = new StringBuilder();

	            for (int i = 0; i < document.getNumberOfPages(); i++) {
	                PDPage page = document.getPage(i);
	                PDRectangle mediaBox = page.getMediaBox();

	                float width = mediaBox.getWidth();
	                float height = mediaBox.getHeight();

	                // 좌/우 영역 등록
	                areaStripper.addRegion("left", new Rectangle2D.Float(0, 0, width / 2, height));
	                areaStripper.addRegion("right", new Rectangle2D.Float(width / 2, 0, width / 2, height));

	                // 페이지에서 텍스트 추출
	                areaStripper.extractRegions(page);
	                String leftText = areaStripper.getTextForRegion("left");
	                String rightText = areaStripper.getTextForRegion("right");

	                // 문제 번호 단위로 나누기
	                List<String> leftQuestions = splitByQuestionNumber(leftText);
	                List<String> rightQuestions = splitByQuestionNumber(rightText);

	                // 문제 번호 기준으로 정렬
	                Map<Integer, String> questionMap = new TreeMap<>();
	                for (String q : leftQuestions) {
	                    int num = extractQuestionNumber(q);
	                    questionMap.put(num, q);
	                }
	                for (String q : rightQuestions) {
	                    int num = extractQuestionNumber(q);
	                    questionMap.put(num, q);
	                }

	                // 정렬된 문제 순서로 append
	                for (String q : questionMap.values()) {
	                    fullText.append(q).append("\n\n");
	                }

	                // 다음 페이지를 위해 영역 초기화
	                areaStripper.getRegions().clear();
	            }

	            return fullText.toString().trim();
	        }
	}
	
	private List<String> splitByQuestionNumber(String text) {
		List<String> questions = new ArrayList<>();
        Matcher matcher = Pattern.compile("(\\d+)\\.").matcher(text);

        int lastIndex = 0;
        int lastNum = -1;

        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            int start = matcher.start();

            if (lastNum != -1) {
                questions.add(text.substring(lastIndex, start).trim());
            }

            lastIndex = start;
            lastNum = num;
        }

        // 마지막 문제 추가
        if (lastIndex < text.length()) {
            questions.add(text.substring(lastIndex).trim());
        }

        return questions;
    }
	
	private int extractQuestionNumber(String question) {
        Matcher m = Pattern.compile("(\\d+)\\.").matcher(question);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return Integer.MAX_VALUE; // 번호 못찾으면 끝으로
    }
}
