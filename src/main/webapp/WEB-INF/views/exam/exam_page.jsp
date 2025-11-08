<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="<c:url value="/resources/css/exam_page.css"/>" rel="stylesheet">
<!-- axios -->
<script type="module" src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
	<div class="exam-container">
        
        <header class="exam-header">
            <h1 class="exam-title">
            	<a href="/">${examPageDto.examType} (${examPageDto.examRound}) ${examPageDto.examSubject}</a>
           	</h1>
            <div class="timer-box">
                <span id="timer">00:00</span>
            </div>
            <button type="submit" form="examForm" class="submit-button">시험 종료 및 제출</button>
        </header>

        <form id="examForm" action="/exam/submitExam" method="post">
            <div class="exam-content-wrapper no-omr">
                <div class="question-area full-width">

                    <!-- 문제들 -->
                    <c:forEach items="${examPageDto.examQuestions}" var="questionDto" varStatus="status" >
                        <div class="question-item">
                            <!-- 공통 지문 -->
                            <c:forEach items="${examPageDto.distinctPassageDto}" var="commonPassageDto">
                            	<c:if test="${questionDto.questionNum == commonPassageDto.commonPassageStartNum}">
                            		<div class="common-text-box">
                                        <p class="text-range">
                                            [${commonPassageDto.commonPassageStartNum}~${commonPassageDto.commonPassageEndNum}] 다음 글을 읽고 물음에 답하시오.
                                        </p>
                                        <div class="text-content">
                                            ${commonPassageDto.commonPassageText}
                                        </div>
                                    </div>
                            	</c:if>
                            </c:forEach>
                            
                            <!-- 문제 번호 및 텍스트 -->
                            <p class="question-number" id="qnum_${questionDto.questionNum}">${questionDto.questionNum}. ${questionDto.questionText}</p>

                            <!-- 단독 지문 -->
                            <c:choose>
                            	<c:when test="${not empty questionDto.questionImage}">
                            		<div class="question-media-box">
                                        <img src="/exam/getExamImagePath?examType=${examPageDto.examType}&examRound=${examPageDto.examRound}&examSubject=${examPageDto.examSubject}&filename=${questionDto.questionImage}" 
											alt="문제 이미지" 
											class="question-image"
										>
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
<%--                     				<c:choose> --%>
                    					<c:if test="${choiceDto.questionId == questionDto.questionId}">
                    						<label class="option-label full-click">
                    						
                    							<!-- 선택지 번호 -->
                    							<c:if test="${not empty choiceDto.choiceLabel}">
                    								<input type="radio" name="question_${questionDto.questionId}" value="${choiceDto.choiceId}">
                                                	<span class="omr-bubble">${choiceDto.choiceLabel}</span>
                    							</c:if>
                                                
                                                
                                                <!-- 선택지 내용 -->
                                                <c:if test="${not empty choiceDto.choiceText}">
                                                	<span class="option-text">${choiceDto.choiceText}</span>
                                                </c:if>
                                                
                                                <!-- 선택지 이미지 -->
                                                <c:if test="${not empty choiceDto.choiceImage}">
                                                	<img src="${choiceDto.choiceImage}" alt="Choice Image" class="option-image">
                                                </c:if>
                                                
                                            </label>
                    					</c:if>				
<%--                     				</c:choose>			 --%>
                    			</c:forEach>
                    			
                    		</div>        
                        </div>
                    </c:forEach>
                    
                </div>
            </div>
        </form>
    </div>

	<script src="<c:url value="/resources/js/exam_page.js"/>"></script>
</body>
</html>