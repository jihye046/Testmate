<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link href="<c:url value="/resources/css/login.css"/>" rel="stylesheet">
<!-- axios -->
<script type="module" src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
	<div class="login-container">
		<a href="<c:url value='/'/>" class="home-link">
	        <h1 class="login-title">통합 자동 채점 서비스</h1>
		</a>
		
        <p class="login-subtitle">로그인 후 오답노트, 학습 관리 등<br>추가 기능을 이용해 보세요.</p>
        
        <form action="login" method="post" class="login-form">
            <div class="input-group">
                <label for="userId" class="input-label">아이디</label>
                <input type="text" id="userId" name="userId" class="input-field" placeholder="아이디 입력" required>
            </div>
            
            <div class="input-group">
                <label for="password" class="input-label">비밀번호</label>
                <input type="password" id="password" name="password" class="input-field" placeholder="비밀번호 입력" required>
            </div>
            
            <button type="submit" class="login-button">로그인</button>
        </form>
        
        <div class="link-area">
            <a href="/user/signupPage" class="signup-link">회원가입</a> |
            <a href="#" class="find-link">아이디/비밀번호 찾기</a>
        </div>
    </div>
    
    <script src="<c:url value="/resources/js/login.js"/>"></script>
</body>
</html>