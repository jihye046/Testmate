<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link href="<c:url value="/resources/css/main.css"/>" rel="stylesheet">
<!-- axios -->
<script type="module" src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
	<!-- URL: 로그인페이지 또는 로그아웃 -->
	<c:set var="loginUrl" value="${pageContext.request.contextPath}${empty sessionScope.userId ? '/user/loginPage' : '/user/logout'}"></c:set>
	<!-- TEXT: 로그인 또는 로그아웃 텍스트 -->
	<c:set var="loginText" value="${empty sessionScope.userId ? '로그인' : '로그아웃'}"></c:set>
	
	<div class="container">
		<div class="top-utility">
			<a href="${loginUrl}" class="login-link">
				${loginText}
			</a>
		</div>
        <h1 class="main-title">자동 채점 서비스</h1>
		
        <p class="subtitle">응시할 시험 정보(종류, 회차, 과목)를 선택하세요.</p>
        <div class="selection-form">
            <div class="select-wrapper">
                <label for="examType" class="select-label">시험 종류</label>
                <select id="examType" class="styled-select">
                    <option value="" selected disabled>시험을 선택하세요</option>
                    <c:forEach items="${examtypes}" var="type">
                        <option value="${type.examTypeCode}">${type.examTypeName}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="select-wrapper">
                <label for="examRound" class="select-label">시험 회차</label>
                <select id="examRound" class="styled-select" disabled>
                    <option value="" selected disabled>시험 종류를 먼저 선택하세요</option>
                </select>
            </div>
            <div class="select-wrapper">
                <label for="examSubject" class="select-label">시험 과목</label>
                <select id="examSubject" class="styled-select" disabled>
                    <option value="" selected disabled>시험 종류를 먼저 선택하세요</option>
                </select>
            </div>
        </div>
        <button id="nextButton" class="next-button" disabled>다음</button>
    </div>
    
    <script src="<c:url value="/resources/js/examInfo.js"/>"></script>
</body>
</html>