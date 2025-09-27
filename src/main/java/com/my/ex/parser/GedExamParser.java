package com.my.ex.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GedExamParser implements IExamParser {

	// ①, ②, ③, ④ 유니코드 패턴
    private static final String CHOICE_PATTERN_STR = "(\u2460|\u2461|\u2462|\u2463)(.*?)";
    private static final Pattern CHOICE_PATTERN = Pattern.compile(CHOICE_PATTERN_STR, Pattern.DOTALL);
	
    
    // 1. 페이지 상단 헤더 제거 패턴 (초졸/중졸/고졸 모두 대응)
    private static final Pattern PAGE_HEADER_PATTERN =
    	Pattern.compile("(초졸|중졸|고졸)\\s*\\([가-힣\\s]+\\)\\s*\\d+－\\d+", Pattern.DOTALL);
    
    // 2. 시험 기본 정보 추출 패턴 (회차, 교시, 과목)
    private static final Pattern INFO_BLOCK_PATTERN =
    	Pattern.compile(
			"(\\d{4}년도 제\\d+회 (중학교|고등학교|초등학교) 졸업학력 검정고시)\\s*" + // 그룹 1: 연도/회차 전체
            "(초졸|중졸|고졸)\\s*" +
            "(제\\d+교시)\\s*" + // 그룹 4: 교시
            "([가-힣\\s]+)", // 그룹 5: 과목명 (공백 포함)
    		Pattern.DOTALL
		);
    
    private ExamInfo examInfo;
    
    @Override
	public ExamInfo getExamInfo() {
		return examInfo;
	}
    
	@Override
	public List<Map<String, Object>> parse(String fullText) {
		// 1. 기본 정보 추출 및 텍스트 정제
		String cleanedText = cleanAndExtractInfo(fullText);
		
		// 2. 문제 단위 분리 및 파싱
		return parseQuestions(cleanedText);
	}

	// 시험 문제 번호와 문제+지문+선택지 추출
	private List<Map<String, Object>> parseQuestions(String cleanedText) {
		List<Map<String, Object>> questions = new ArrayList<>();
		
		String tempText = cleanedText.replaceAll("(?<=[^\\s])\\s*(\\d+\\.\\s*+)(?![\\d\\.])", "\n$1");
		
		Matcher firstQuestionMatcher = Pattern.compile("^.*?(?=\\s*1\\.\\s*)", Pattern.DOTALL).matcher(tempText);
		String startText = tempText;
	    if (firstQuestionMatcher.find()) {
	        startText = tempText.substring(firstQuestionMatcher.end()).trim();
	    }
		
		// 문제 단위 분리
	    String questionDelimiter = "(?=\\n\\s*\\d+\\.\\s*)"; // 문제 번호 패턴 (1. 2. 3.)
	    // 공통 지문 관련 변수 초기화
 		String currentCommonPassage = ""; // [11~13]에 해당하는 긴 지문 텍스트
 		String currentPassageScope = "";   // [11~13] 범위 텍스트
	    
		String[] questionBlocks = startText.split(questionDelimiter);
		
		for(String rawBlock : questionBlocks) {
			String trimmedBlock = rawBlock.trim();
			
	        if (trimmedBlock.isEmpty()) continue;
	        
	        // 블록 유형 확인 (지문 또는 문제)
	        
	        Matcher scopeMatcher = Pattern.compile("^\\[(\\d+)\\s*~\\s*(\\d+)\\]([\\s\\S]*)", Pattern.DOTALL).matcher(trimmedBlock);
	        if(scopeMatcher.find()) {
	        	// **[A] 지문 블록인 경우**
	        	currentPassageScope = scopeMatcher.group(1) + "~" + scopeMatcher.group(2); // 예: 11~13
	        	
	        	// 지문 본문 텍스트 (범위 패턴 뒤의 모든 내용)
	        	String passageContent = scopeMatcher.group(3).trim(); 
	        	
	        	// 안내 문구 (예: '다음 관을 읽고 물음에 답하시오.') 제거 정규식
	            passageContent = passageContent.replaceFirst("^[\\s\\S]*?[\\?.:]\\s*", "").trim(); 
	            
	            currentCommonPassage = passageContent;
	            
	            System.out.println(">>> 지문 블록 (Scope: " + currentPassageScope + ")");
	        } else {
	        	// **[B] 문제 블록인 경우**
	        	Matcher numMatcher = Pattern.compile("^(\\d+)").matcher(trimmedBlock);
	        	
	        	if(numMatcher.find()) {
	        		int questionNum = Integer.parseInt(numMatcher.group(1));
		        	
		        	// 문제 번호와 마침표를 제외한 나머지 텍스트 (문제 본문 + 선택지)
		        	String contentBlock = trimmedBlock;
		        	System.out.println(">>> 문제 블록 " + questionNum + "번");
		            System.out.println("contentBlock: " + contentBlock);
		            
		            Map<String, Object> questionData = parseQuestionBlock(
		            	questionNum, 
		            	contentBlock,
		            	currentCommonPassage, // 공통 지문 텍스트 전달
		            	currentPassageScope   // 지문 범위 전달
	            	);
		            questions.add(questionData);
	        	}
	        	
	        }
	        
		}
		
		return null;
	}
	
	// 시험 문제와 선택지 추출
	private Map<String, Object> parseQuestionBlock(
		int questionNum, 
		String contentBlock,
		String commonPassageText,
		String passageScope) {
		Map<String, Object> questionData = new HashMap<>();
		
		// 1. 선택지 분리 정규식: ①, ②, ③, ④ 등 동그라미 숫자로 시작하는 패턴
		String choiceDelimiter = "([①②③④⑤])";
		
		// 2. 시험 문제/지문 덩어리 추출 (선택지가 시작하는 위치를 찾아서 분리)
		// 'contentBlock'의 텍스트 전체에서 [①]가 나오는 위치를 찾기만 함
		Pattern choiceStartPattern = Pattern.compile("(?=[①②③④⑤])");
	    Matcher choiceMatcher = choiceStartPattern.matcher(contentBlock);
	    
	    String questionText; 		// 시험 문제(질문) 텍스트
	    String questionPassageText; // 지문/보기 텍스트
	    String preChoiceText; 		// 선택지(①, ②, ③...)가 시작되기 전까지의 모든 내용 (질문 + 지문)
	    String choicesText;  		// 선택지
	    
	    if(choiceMatcher.find()) {
	    	// 첫 번째 선택지 기호 직전까지를 '문제 본문'으로 간주
	    	int startIndex = choiceMatcher.start(); // 첫 번째 선택지 기호(①)가 처음 나타나는 위치(인덱스)를 저장
	    	preChoiceText = contentBlock.substring(0, startIndex).trim(); // (질문 + 지문) 추출
	    	choicesText = contentBlock.substring(startIndex).trim(); // 첫 번째 선택지 기호부터 끝까지를 '선택지 덩어리'로 간주
	    } else {
	    	// 선택지 기호가 없는 경우 (선택지 덩어리는 비우고, 모든 텍스트를 지문/질문 텍스트로 간주)
	    	preChoiceText  = contentBlock.trim();
	        choicesText = "";
	    }
	    
	    // 3. 'preChoiceText' (질문 + 지문) 덩어리를 분리
	    int splitIndex = -1;
	    // 물음표(?)가 있다면, 물음표 다음의 첫 번째 줄바꿈(\n)을 분리점으로 찾습니다.
	    int questionMarkIndex = preChoiceText.indexOf('?');

	    if (questionMarkIndex != -1) {
	        splitIndex = preChoiceText.indexOf('\n', questionMarkIndex + 1);
	    }

	    if (splitIndex != -1) {
	        // 분리점을 찾은 경우: [질문] + [지문/보기] 구조로 간주
	        questionText = preChoiceText.substring(0, splitIndex).trim();
	        questionPassageText = preChoiceText.substring(splitIndex).trim();

	    } else {
	        // 분리점을 찾지 못한 경우 (지문이 없는 순수 질문이거나 문장이 붙어 있는 경우)
	        questionText = preChoiceText.trim();
	        questionPassageText = "";
	    }
	    // questionText가 최종 결정된 후, 시작 부분의 문제 번호 잔재를 무조건 제거
	    questionText = questionText.replaceFirst("^\\d+\\.\\s*", "").trim();
	    
	    // 4. 선택지 덩어리를 개별 선택지로 분리
	    List<Map<String, String>> optionsList = new LinkedList<>();
	    if(!choicesText.isEmpty()) {
	    	// 선택지 분리
	    	String[] options = choicesText.split(choiceDelimiter);
	    	
	    	for(String option: options) {
	    		String trimmedOption = option.trim();
	    		if(!trimmedOption.isEmpty()) {
	    			// 라벨(①)과 나머지(선택지 텍스트)를 분리
	    			String choiceLabel = trimmedOption.substring(0, 1);
	    			String choiceText = trimmedOption.substring(1).trim();
	    			
	    			Map<String, String> choice = new HashMap<>();
	    			choice.put("choiceLabel", choiceLabel);
	    			choice.put("choiceText", choiceText);
	    			optionsList.add(choice);
	    		}
	    	}
	    }
	    
	    // 5. 추출 결과를 Map에 담아 반환
	    questionData.put("questionNum", questionNum);
	    questionData.put("questionText", questionText);
	    
	    // 지문이 있으면 그것을, 없으면 상위에서 받은 공통 지문을 사용
	    String finalPassage = questionPassageText.isEmpty() ? commonPassageText : questionPassageText;
	    
	    questionData.put("questionPassage", finalPassage); // 지문/보기 텍스트
	    questionData.put("passageScope", passageScope); // 공통 지문 범위
	    questionData.put("options", optionsList);
	    
	    // 디버깅 출력
	    System.out.println("--- 파싱 결과 ---");
	    System.out.println("문제 번호: " + questionNum);
	    System.out.println("질문 텍스트 (Question Text): " + questionText);
	    System.out.println("지문/보기 텍스트 (Passage): " + questionPassageText);
	    System.out.println("선택지 수 (Options Count): " + optionsList.size());
	    System.out.println("-----------------");
	    
		return null;
//		return questionData;
	}

	// 시험 기본 정보 추출 (회차, 교시, 과목)
	private String cleanAndExtractInfo(String fullText) {
		// 1. 페이지 헤더 제거
		String cleanedText = PAGE_HEADER_PATTERN.matcher(fullText).replaceAll("");
		
		// 2. 시험 기본 정보 추출 및 설정
		Matcher infoMatcher = INFO_BLOCK_PATTERN.matcher(cleanedText);
		if(infoMatcher.find()) {
			// 그룹 1: "2025년도 제1회 중학교 졸업학력 검정고시"
			String fullRoundText = infoMatcher.group(1).trim();
			// EXAM_ROUND 설정: "2025년도 제1회" 부분만 추출
			Matcher roundMatcher = Pattern.compile("^(\\d{4}년도 제\\d+회)").matcher(fullRoundText);
			String round = roundMatcher.find() ? roundMatcher.group(1).trim() : fullRoundText;
			
			// 그룹 4: "제1교시"
			String sessionNo = infoMatcher.group(4).trim();
			
			// 그룹 5: 과목명 공백 제거 ("국 어" -> "국어")
			String subject = infoMatcher.group(5).replaceAll("\\s+", "").trim();
			
			// ExamInfo 객체 생성
			ExamInfo examInfo = new ExamInfo(round, subject, sessionNo);
			
			// 추출된 기본 정보 블록을 텍스트에서 제거
			cleanedText = infoMatcher.replaceAll("");
		} else {
			System.err.println("Warning: Could not extract GED Exam Info block. Setting to null.");
			ExamInfo examInfo = new ExamInfo(null, null, null);
		}
		
		// 3. 과도한 공백 및 줄바꿈 정리
		cleanedText = cleanedText.replaceAll("(\r?\n){3,}", "\n\n");
		cleanedText = cleanedText.replaceAll("\\s{2,}", " ");
		
		return cleanedText.trim();
	}
	
}
