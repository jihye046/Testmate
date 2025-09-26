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
	<div class="login-container signup-container">
        
        <a href="<c:url value='/'/>" class="home-link">
            <h1 class="login-title">자동 채점 서비스</h1>
        </a>
        
        <p class="login-subtitle">새로운 계정을 만들고 학습 관리 서비스를 이용해 보세요.</p>
        
        <form id="signup-form" action="/user/signup" method="post" class="login-form">
            
            <div class="input-group">
                <label for="userId" class="input-label">아이디</label>
                <div class="input-with-button">
                    <input type="text" id="userId" name="userId" class="input-field" placeholder="아이디 (4~12자)" required>
                    <button type="checkIdButton" id="checkIdButton" class="check-button">중복 확인</button>
                </div>
                <span id="idRequirement" class="validation-message">아이디는 4~12자의 영문 소문자, 숫자만 사용할 수 있습니다.</span>
            </div>
            
            <div class="input-group">
                <label for="password" class="input-label">비밀번호</label>
                <input 
                    type="password" 
                    id="password" 
                    name="userPw" 
                    class="input-field" 
                    placeholder="비밀번호 입력" 
                    required
                >
                <span id="passwordRequirement" class="validation-message">8자 이상 16자 이하, 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다.</span>
            </div>
            
            <div class="input-group">
                <label for="passwordCheck" class="input-label">비밀번호 확인</label>
                <input 
                    type="password" 
                    id="passwordCheck" 
                    name="passwordCheck" 
                    class="input-field" 
                    placeholder="비밀번호 재확인" 
                    required
                >
                <span id="passwordMismatchMessage" class="validation-message"></span>
            </div>
            
            <div class="input-group">
                <label for="email" class="input-label">이메일</label>
                <input 
                    type="email" 
                    id="email" 
                    name="uemail" 
                    class="input-field" 
                    placeholder="이메일 주소" 
                    required
                >
            </div>
            
            <button id="signupBtn" type="submit" class="login-button signup-button" disabled>가입 완료</button>
        </form>
        
        <div class="link-area">
            이미 계정이 있으신가요? <a href="<c:url value='/user/login'/>" class="signup-link">로그인</a>
        </div>
    </div>

	<script src="<c:url value="/resources/js/signup.js"/>"></script>
</body>
</html>