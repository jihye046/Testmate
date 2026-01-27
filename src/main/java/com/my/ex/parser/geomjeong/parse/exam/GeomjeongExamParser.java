package com.my.ex.parser.geomjeong.parse.exam;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.parser.common.IExamParser;

@Component
public class GeomjeongExamParser implements IExamParser {

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
    
    // 자주 오인식되는 단어를 올바른 단어로 매핑하는 Map
    private static final Map<String, String> CORRECTION_MAP = createCorrectionMap();
    private static Map<String, String> createCorrectionMap(){
    	return Collections.unmodifiableMap(Stream.of(
			new AbstractMap.SimpleImmutableEntry<>("윗관","윗글"),
			new AbstractMap.SimpleImmutableEntry<>("윁윈","적절"),
			new AbstractMap.SimpleImmutableEntry<>("아윀씨","아저씨"),
			new AbstractMap.SimpleImmutableEntry<>("윐수","점수"),
			new AbstractMap.SimpleImmutableEntry<>("만윐","만점"),
			new AbstractMap.SimpleImmutableEntry<>("묑줄","밑줄"),
			new AbstractMap.SimpleImmutableEntry<>("한관","한글"),
			new AbstractMap.SimpleImmutableEntry<>("김치쬌게","김치찌개"),
			new AbstractMap.SimpleImmutableEntry<>("공통윐","공통점"),
			new AbstractMap.SimpleImmutableEntry<>("문법윁","문법적"),
			new AbstractMap.SimpleImmutableEntry<>("윀것","저것"),
			new AbstractMap.SimpleImmutableEntry<>("어괋나는","어긋나는"),
			new AbstractMap.SimpleImmutableEntry<>("중괈속","중금속"),
			new AbstractMap.SimpleImmutableEntry<>("먼윀","먼저"),
			new AbstractMap.SimpleImmutableEntry<>("미각윁","미각적"),
			new AbstractMap.SimpleImmutableEntry<>("씁변","영변"),
			new AbstractMap.SimpleImmutableEntry<>("너물","눈물"),
			new AbstractMap.SimpleImmutableEntry<>("물질주의윁","물질주의적"),
			new AbstractMap.SimpleImmutableEntry<>("위통윁인","전통적인"),
			new AbstractMap.SimpleImmutableEntry<>("계승하씀기에","계승하였기에"),
			new AbstractMap.SimpleImmutableEntry<>("위날","전날"),
			new AbstractMap.SimpleImmutableEntry<>("딐","또"),
			new AbstractMap.SimpleImmutableEntry<>("괈시에","금시에"),
			new AbstractMap.SimpleImmutableEntry<>("지위을", "지전을"),
			new AbstractMap.SimpleImmutableEntry<>("지위", "지전"),
			new AbstractMap.SimpleImmutableEntry<>("윑은", "접은"),
			new AbstractMap.SimpleImmutableEntry<>("잡픀", "잡혀"),
			new AbstractMap.SimpleImmutableEntry<>("관을", "글을"),
			new AbstractMap.SimpleImmutableEntry<>("관이", "글이"),
			new AbstractMap.SimpleImmutableEntry<>("윑어서", "접어서"),
			new AbstractMap.SimpleImmutableEntry<>("계윈", "계절"),
			new AbstractMap.SimpleImmutableEntry<>("구체윁인", "구체적인"),
			new AbstractMap.SimpleImmutableEntry<>("위개", "전개"),
			new AbstractMap.SimpleImmutableEntry<>("표프", "표현"),
			new AbstractMap.SimpleImmutableEntry<>("위달", "전달"),
			new AbstractMap.SimpleImmutableEntry<>("사딐", "사또"),
			new AbstractMap.SimpleImmutableEntry<>("하씀", "하였"),
			new AbstractMap.SimpleImmutableEntry<>("일뜀다", "일렀다"),
			new AbstractMap.SimpleImmutableEntry<>("그뜁그뜁", "그렁그렁"),
			new AbstractMap.SimpleImmutableEntry<>("윈벽", "절벽"),
			new AbstractMap.SimpleImmutableEntry<>("너이", "눈이"),
			new AbstractMap.SimpleImmutableEntry<>("빙괋", "빙긋"),
			new AbstractMap.SimpleImmutableEntry<>("윀녁", "저녁"),
			new AbstractMap.SimpleImmutableEntry<>("윁시며", "적시며"),
			new AbstractMap.SimpleImmutableEntry<>("세계윁", "세계적"),
			new AbstractMap.SimpleImmutableEntry<>("강제윁", "강제적"),
			new AbstractMap.SimpleImmutableEntry<>("프황", "현황"),
			new AbstractMap.SimpleImmutableEntry<>("씁향", "영향"),
			new AbstractMap.SimpleImmutableEntry<>("픑력", "협력"),
			new AbstractMap.SimpleImmutableEntry<>("남종씁", "남종영"),
			new AbstractMap.SimpleImmutableEntry<>("부정윁인", "부정적인"),
			new AbstractMap.SimpleImmutableEntry<>("일반윁", "일반적"),
			new AbstractMap.SimpleImmutableEntry<>("그뜇지", "그렇지"),
			new AbstractMap.SimpleImmutableEntry<>("윁게", "적게"),
			new AbstractMap.SimpleImmutableEntry<>("너코", "눈코"),
			new AbstractMap.SimpleImmutableEntry<>("윀렴한", "저렴한"),
			new AbstractMap.SimpleImmutableEntry<>("오위", "오후"),
			new AbstractMap.SimpleImmutableEntry<>("위픑", "위협")
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
    
    private ExamInfoDto examInfo;
    
    @Override
	public ExamInfoDto getExamInfo() {
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

	    Matcher firstQuestionMatcher = Pattern.compile("^.*?(?=\\s*1\\.\\s*|\\s*\\[\\d+[~～]\\d+\\])", Pattern.DOTALL).matcher(tempText);
	    String startText = tempText;
	    String currentCommonPassage = ""; // [11~13]에 해당하는 긴 지문 텍스트
    	String currentPassageScope = "";  // [11~13] 범위 텍스트
	
    	if (firstQuestionMatcher.find()) {
            String preamble = firstQuestionMatcher.group(0).trim();
            startText = tempText.substring(firstQuestionMatcher.end()).trim();

            // Preamble(앞부분)에서 지문 범위와 내용을 추출
            Matcher scopeMatcher = Pattern.compile("\\[(\\d+)\\s*[~～]\\s*(\\d+)\\]([\\s\\S]*)", Pattern.DOTALL).matcher(preamble);
            if (scopeMatcher.find()) {
                currentPassageScope = scopeMatcher.group(1) + "~" + scopeMatcher.group(2); // 예: 23~25
                String passageContent = scopeMatcher.group(3).trim();

                // 안내 문구 제거
                passageContent = passageContent.replaceAll("다음 (글|자료)을 읽고 물음에 답하시오[\\.:]?", "").trim();
                passageContent = passageContent.replaceAll("([가-힣\\s]+)을 읽고 물음에 답하시오[\\.:]?", "").trim();

                currentCommonPassage = passageContent;
//                System.out.println(">>> 초기 지문 블록 추출 (Scope: " + currentPassageScope + ")");
            } else {
                // 첫 문제 전에 지문 범위가 없지만 텍스트가 있는 경우, 공통 지문으로 간주
                if (!preamble.isEmpty()) {
                     currentCommonPassage = preamble;
                }
            }
    	}
    	
	    // 문제 단위 분리
	    String questionDelimiter = "(?=\\n\\s*\\d+\\.\\s*)"; // 문제 번호 패턴 (1. 2. 3.)
	    // 공통 지문 관련 변수 초기화
	    String[] questionBlocks = startText.split(questionDelimiter);

	    // 다음 지문 블록에 대한 정보를 임시로 저장할 변수
	    String nextCommonPassage = "";
	    String nextPassageScope = "";

	    for(String rawBlock : questionBlocks) {
	        String trimmedBlock = rawBlock.trim();

	        if (trimmedBlock.isEmpty()) continue;
	        
	        // **[2. 문제 블록 처리]**
	        Matcher numMatcher = Pattern.compile("^(\\d+)\\.\\s*([\\s\\S]*)", Pattern.DOTALL).matcher(trimmedBlock);
	        
	        if(numMatcher.find()) {
	            if (!nextCommonPassage.isEmpty()) {
	                currentCommonPassage = nextCommonPassage;
	                currentPassageScope = nextPassageScope;
	                nextCommonPassage = "";
	                nextPassageScope = "";
	            }
	            
	            int questionNum = Integer.parseInt(numMatcher.group(1));
	            String contentPart = numMatcher.group(2).trim();

	            // 3. 문제 블록의 꼬리에 다음 공통 지문이 붙어있는지 확인 (예: 10번 문제 끝에 [11~13]이 붙은 경우)
	            // *해당 로직은 다음 문제의 지문을 찾아서 `nextCommonPassage`에 저장하는 역할만 함*
	            Matcher scopeSeparatorMatcher = Pattern.compile("(\\s*\\[\\d+[~～]\\d+\\][\\s\\S]*)$", Pattern.DOTALL).matcher(contentPart);
	            if(scopeSeparatorMatcher.find()) {
	            	String passageBlock = scopeSeparatorMatcher.group(1).trim();
	            	Matcher scopeOnlyMatcher = Pattern.compile("\\[(\\d+)\\s*[~～]\\s*(\\d+)\\]", Pattern.DOTALL).matcher(passageBlock);
	            	if (scopeOnlyMatcher.find()) {

	            		// **다음 문제에 사용될 변수에 저장**
						nextPassageScope = scopeOnlyMatcher.group(1) + "~" + scopeOnlyMatcher.group(2);
						int scopeEndIndex = scopeOnlyMatcher.end();
						String contentAfterScope = passageBlock.substring(scopeEndIndex).trim();
						
                        // 안내 문구 제거 로직 단순화
						String finalPassageContent = contentAfterScope.replaceAll("다음 (글|자료)을 읽고 물음에 답하시오[\\.:]?", "").trim();
						finalPassageContent = finalPassageContent.replaceAll("([가-힣\\s]+)을 읽고 물음에 답하시오[\\.:]?", "").trim();
						nextCommonPassage = finalPassageContent; // **다음 문제에 사용될 지문 저장**
                        
                        // 공통 지문이 바뀌었으므로 현재 지문 정보 초기화
//                        currentCommonPassage = "";
//                        currentPassageScope = "";
	            	}
	            	
	            	// 4. 공통 지문 부분을 제거하여 contentPart를 정리 (현재 문제 내용만 남김)
	            	contentPart = contentPart.substring(0, scopeSeparatorMatcher.start()).trim();

//	            	System.out.println(">>> 지문 블록 (Scope: " + nextPassageScope + ") - 문제 " + questionNum + "번 끝에서 분리, 다음 문제에 적용 예정");
	            }
	            
	            // --- 문제 파싱 로직 ---
	            String contentBlock = contentPart; 

//	            System.out.println(">>> 문제 블록 " + questionNum + "번");

	            Map<String, Object> questionData = parseQuestionBlock(
	                questionNum,
	                contentBlock,
	                currentCommonPassage, // 현재 저장된 공통 지문 텍스트 전달
	                currentPassageScope	  // 현재 저장된 지문 범위 전달
	            );
	            questions.add(questionData);
	            
	            // 해당 문제가 지문의 마지막 문제였는지 확인하고, 맞다면 공통 지문 정보를 초기화
	            /*
                if (!currentPassageScope.isEmpty()) {
                    try {
                        String[] range = currentPassageScope.split("~");
                        int end = Integer.parseInt(range[1]);
                        
                        if (questionNum == end) {
//                            System.out.println(">>> 지문 범위 마지막 문제(" + end + "번) 완료. 공통 지문 초기화.");
                            currentCommonPassage = "";
                            currentPassageScope = "";
                        }
                    } catch (NumberFormatException e) {
                        // 숫자로 파싱 실패 시, 무시하고 다음 문제로 진행
                    }
                }
                */
	        }
	    }

	    // 파싱된 전체 문제 리스트를 반환
	    return questions;
	}
	
	// 시험 문제와 선택지 추출
	private Map<String, Object> parseQuestionBlock(
	    int questionNum,
	    String contentBlock,
	    String commonPassageText,
	    String passageScope
	) {
		Map<String, Object> questionData = new HashMap<>();

	    // 1. 선택지 분리 정규식: ①, ②, ③, ④ 등 동그라미 숫자로 시작하는 패턴
	    String choiceDelimiter = "([①②③④⑤])";

	    // 2. 시험 문제/지문 덩어리 추출 (선택지가 시작하는 위치를 찾아서 분리)
	    Pattern choiceStartPattern = Pattern.compile("(?=[①②③④⑤])");
	    Matcher choiceMatcher = choiceStartPattern.matcher(contentBlock);

	    String questionText; 		// 시험 문제(질문) 텍스트
	    String questionPassageText; // 지문/보기 텍스트 (개별 지문)
	    String preChoiceText; 		// 선택지(①, ②, ③...)가 시작되기 전까지의 모든 내용 (질문 + 지문)
	    String choicesText;			// 선택지

	    if(choiceMatcher.find()) {
	        int startIndex = choiceMatcher.start();
	        preChoiceText = contentBlock.substring(0, startIndex).trim();
	        choicesText = contentBlock.substring(startIndex).trim();
	    } else {
	        preChoiceText = contentBlock.trim();
	        choicesText = "";
	    }

	    // 3. 'preChoiceText' (질문 + 지문) 덩어리를 분리
	    int splitIndex = -1;
	    int questionMarkIndex = preChoiceText.indexOf('?');

	    if (questionMarkIndex != -1) {
	        splitIndex = preChoiceText.indexOf('\n', questionMarkIndex + 1);
	    }
	    
	    // ?가 없는 경우를 대비하여 마침표(.) 또는 콜론(:) 다음 줄바꿈을 확인
	    if (splitIndex == -1 && questionMarkIndex == -1) {
	    	int lastPunctuationIndex = Math.max(preChoiceText.lastIndexOf('.'), preChoiceText.lastIndexOf(':'));
	    	
	    	if (lastPunctuationIndex != -1) {
	    		// 마지막 구두점 이후의 첫 번째 줄바꿈을 찾음
	    		int potentialSplitIndex = preChoiceText.indexOf('\n', lastPunctuationIndex + 1);
	    		if (potentialSplitIndex != -1) {
	    			splitIndex = potentialSplitIndex;
	    		}
	    	}
	    	
	    }

	    if (splitIndex != -1) {
	        questionText = preChoiceText.substring(0, splitIndex).trim();
	        questionPassageText = preChoiceText.substring(splitIndex).trim();
	    } else {
	        questionText = preChoiceText.trim();
	        questionPassageText = "";
	    }
	    
	    questionText = questionText.replaceFirst("^\\d+\\.\\s*", "").trim();
	    
	    // 4. 선택지 덩어리를 개별 선택지로 분리
//	    List<Map<String, Object>> optionsList = new LinkedList<>();
	    List<ExamChoiceDto> optionsList = new LinkedList<>();
	    if(!choicesText.isEmpty()) {
	        String[] options = choicesText.split(choiceDelimiter);

	        String[] labels = new String[]{"①", "②", "③", "④", "⑤"};
	        int[] nums = new int[] {1, 2, 3, 4, 5};
	        
	        int index = 0;

	        for(String option: options) {
	            String trimmedOption = option.trim();

	            if(trimmedOption.isEmpty()) continue;
	            if (index >= labels.length) break;

	            String choiceLabel = labels[index];
	            int choiceNum = nums[index]; 
	            
	            // 선택지 내용에서 불필요한 줄바꿈/구분자(예: //) 제거 및 공백 정규화
	            String choiceText = trimmedOption
	                                .replaceAll("(\r?\n)", " ") 
	                                .replaceAll("//\\s*", "")   
	                                .replaceAll("\\s{2,}", " ") 
	                                .trim();
	            /*
	            Map<String, Object> choice = new HashMap<>();
	            choice.put("choiceLabel", choiceLabel);
	            choice.put("choiceText", choiceText);
	            */
	            optionsList.add(new ExamChoiceDto(choiceLabel, choiceText, choiceNum));

	            index++;
	        }
	    }

	    // 5. 추출 결과를 Map에 담아 반환
	    questionData.put("questionNum", questionNum);
	    questionData.put("questionText", questionText);
	    if(questionPassageText == null || questionPassageText.isEmpty()) {
	    	questionData.put("useIndividualPassage", "N");
	    	questionData.put("individualPassage", null);
	    } else {
	    	questionData.put("useIndividualPassage", "Y");
	    	questionData.put("individualPassage", questionPassageText);
	    }
	    
	    if(commonPassageText == null || commonPassageText.isEmpty()) {
	    	questionData.put("useCommonPassage", "N");
	    	questionData.put("commonPassage", null);
	    } else {
	    	questionData.put("useCommonPassage", "Y");
	    	questionData.put("commonPassage", commonPassageText);
	    }
//	    questionData.put("questionPassage", finalPassage);
	    questionData.put("passageScope", passageScope);
	    questionData.put("options", optionsList);
	    

	    // 디버깅 출력
	    /*
	    System.out.println("--- 파싱 결과 ---");
	    System.out.println("문제 번호: " + questionNum);
	    System.out.println("질문 텍스트 (Question Text): " + questionText);
	    System.out.println("지문/보기 텍스트 (Passage): " + finalPassage); 
	    System.out.println("선택지 수 (Options Count): " + optionsList.size());
	    System.out.println("-----------------");
	     */

	    return questionData;
	}

	// 시험 기본 정보 추출 (회차, 교시, 과목)
	private String cleanAndExtractInfo(String fullText) {
		// 오타 치환 먼저 수행
		String cleanedText = replaceMistypedWords(fullText);
		
		// 1. 페이지 헤더 제거
		cleanedText = PAGE_HEADER_PATTERN.matcher(cleanedText).replaceAll("");
		
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
//			this.examInfo = new ExamInfoDto(round, subject, sessionNo);
			this.examInfo = new ExamInfoDto(round, subject);
			
			// 추출된 기본 정보 블록을 텍스트에서 제거
			cleanedText = infoMatcher.replaceAll("");
		} else {
			System.err.println("Warning: GED 시험 정보를 추출하지 못해 null로 설정");
			this.examInfo = new ExamInfoDto(null, null, null);
		}
		
		// 3. 과도한 공백 및 줄바꿈 정리
		cleanedText = cleanedText.replaceAll("(\r?\n){3,}", "\n\n");
		cleanedText = cleanedText.replaceAll("\\s{2,}", " ");
		
		return cleanedText.trim();
	}
	
	private String replaceMistypedWords(String text) {
	    String correctedText = text;
	    // Map에 정의된 모든 키-값 쌍에 대해 반복하여 치환
	    for (Map.Entry<String, String> entry : CORRECTION_MAP.entrySet()) {
	        String regex = Pattern.quote(entry.getKey());
	        correctedText = correctedText.replaceAll(regex, entry.getValue());
	    }
	    return correctedText;
	}
	
}
