<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 검토 페이지</title>
<link href="<c:url value="/resources/css/admin_exam_page.css"/>" rel="stylesheet">
<!-- axios -->
<script type="module" src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
    <div class="review-container">
        
        <header class="review-header">
            <h1 class="exam-title">
            	<a href="/admin/main">${examPageDto.examType} ${examPageDto.examRound} 검토 모드</a>
           	</h1>
            <div class="exam-info-meta">
                <span><i class="fas fa-book"></i> 과목: ${examPageDto.examSubject}</span>
                <span><i class="fas fa-tag"></i> 유형/회차: ${examPageDto.examRound}</span>
                <button class="btn btn-back" 
                	onclick="location.href='/admin/main?folderId=${folderDto.folderId}&folderName=${folderDto.folderName}'"
               	>
                	<i class="fas fa-list-ul"></i> 목록으로
               	</button>
            </div>
        </header>

        <div class="question-list-area">
        	<!-- 문제들 -->
            <c:forEach items="${examPageDto.examQuestions}" var="questionDto" varStatus="status">
                <div class="question-review-item">
                	
                	<!-- 각 문항 수정 버튼 -->
                	<div class="question-actions">
		                <button class="btn btn-edit-question" 
		                        onclick="location.href='/admin/editQuestion?questionId=${questionDto.questionId}'">
		                    <i class="fas fa-edit"></i> 문항 수정
		                </button>
		            </div>
                	
                    <div class="question-content-box">
                    
                        <!-- 공통 지문 -->
                        <c:forEach items="${examPageDto.distinctPassageDto}" var="commonPassageDto">
                            <c:if test="${questionDto.questionNum == commonPassageDto.commonPassageStartNum}">
                                <div class="common-text-box">
                                    <p class="text-range">
                                        [${commonPassageDto.commonPassageStartNum}~${commonPassageDto.commonPassageEndNum}] 다음 글을 읽고 물음에 답하시오.
                                    </p>
                                    <div class="text-content">
                                    	<c:choose>
                                            <c:when test="${fn:endsWith(commonPassageDto.commonPassageText, '.png')
                                                            or fn:endsWith(commonPassageDto.commonPassageText, '.jpg')
                                                            or fn:endsWith(commonPassageDto.commonPassageText, '.jpeg')}">
                                                <div class="question-media-box">
                                                    <img src="/admin/getExamImagePath?examType=${examPageDto.examType}&examRound=${examPageDto.examRound}&examSubject=${examPageDto.examSubject}&filename=${commonPassageDto.commonPassageText}" 
                                                        alt="문제 이미지" 
                                                        class="question-image"
                                                    >
                                                </div>
                                            </c:when>
                                            <c:when test="${not empty commonPassageDto.commonPassageText}">
                                                <div class="question-media-box">
                                                    <div class="text-content single-passage">${commonPassageDto.commonPassageText}</div>
                                                </div>                            	
                                            </c:when>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:if>
                        </c:forEach>
						
						<!-- 문제 번호 및 텍스트 -->                        
                        <p class="question-number">
                            <span class="number-circle">${questionDto.questionNum}</span>
                            ${questionDto.questionText}
                        </p>
                   		
						<!-- 개별 지문 -->
                        <c:choose>
                        	<c:when test="${not empty questionDto.individualPassage &&
                        					( 
	                        					fn:endsWith(questionDto.individualPassage, '.png') 
	                        					or fn:endsWith(questionDto.individualPassage, '.jpg')
	                        					or fn:endsWith(questionDto.individualPassage, '.jpeg')
                        					)}">
           						<div class="question-media-box">
                                    <img src="/admin/getExamImagePath?examType=${examPageDto.examType}&examRound=${examPageDto.examRound}&examSubject=${examPageDto.examSubject}&filename=${questionDto.individualPassage}" 
                                         alt="문제 이미지" 
                                         class="question-image">
                                </div>
           					</c:when>
                            <c:when test="${not empty questionDto.individualPassage}">
                                <div class="question-media-box">
                                    <div class="text-content single-passage">${questionDto.individualPassage}</div>
                                </div>
                            </c:when>
                        </c:choose>
						
						<!-- 선택지들 -->
                        <div class="options-group">
                            <c:forEach items="${examPageDto.examChoices}" var="choiceDto">
                                <c:if test="${choiceDto.questionId == questionDto.questionId}">
                                    
                                    <div class="option-item">
                                    	<!-- 선택지 번호 -->
                                    	<c:if test="${not empty choiceDto.choiceText}">
                                    		<span class="choice-label">${choiceDto.choiceLabel}</span>
                                    	</c:if>
                                    	
                                    	<!-- 선택지 내용 -->
                                        <c:if test="${not empty choiceDto.choiceText}">
                                        	<span class="choice-text">${choiceDto.choiceText}</span>
                                        </c:if>
                                        
                                        <!-- 선택지 이미지 -->
                                        <c:if test="${not empty choiceDto.choiceImage}">
                                            <img src="${choiceDto.choiceImage}" alt="선택지 이미지" class="choice-image">
                                        </c:if>
                                    </div>
                                    
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                    
                    <div class="answer-explanation-box">
                        <h4 class="box-title"><i class="fas fa-check-circle"></i> 정답 및 해설 (관리자 검토용)</h4>
                        
                        <!-- 정답지 -->
                        <div class="correct-answer">
                            정답: <span class="answer-label">
                                <c:set var="currentCorrectAnswer" value="" />

                                <c:forEach items="${examPageDto.examAnswers}" var="answerDto">
                                    <c:if test="${answerDto.questionId == questionDto.questionId}">
                                        <c:set var="currentCorrectAnswer" value="${answerDto.correctAnswer}"/>
                                    </c:if>
                                </c:forEach>

                                <c:choose>
                                    <c:when test="${not empty currentCorrectAnswer}">
                                        ${currentCorrectAnswer}
                                    </c:when>
                                    <c:otherwise>
                                        [데이터 연결 필요]
                                    </c:otherwise>
                                </c:choose>
                                
                            </span>
                        </div>
                        
                        <div class="explanation-content">[임시 해설] 데이터 연결 필요</div>
                        
                        <div class="question-meta-info">
<!--                             <p>난이도: <span class="difficulty">[추후 확장시 사용]</span></p> -->
                        </div>
                    </div>
                    
                </div>
            </c:forEach>
        </div>
        
    </div>

    <script src="<c:url value='/resources/js/admin_exam_page.js'/>"></script>
</body>
</html>