<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 검토 페이지</title>
<link href="<c:url value="/resources/css/admin_answer_page.css"/>" rel="stylesheet">
<!-- 아이콘 및 폰트 -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700&display=swap" rel="stylesheet">
<!-- 차트 라이브러리 -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<!-- axios -->
<script type="module" src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
	<div class="answer-container">
	    <div class="header">
	        <h2><i class="fas fa-key"></i> 정답지 등록/검토</h2>
	        <button class="btn btn-secondary back-to-folders" 
				onclick="location.href='/admin/main?folderId=${folderDto.folderId}&folderName=${folderDto.folderName}'"
			>목록으로
			</button>
	    </div>
	
	    <div class="summary-bar">
	        <span>총 문항: <span id="totalCount">0</span>개</span>
	        <span>입력 완료: <span id="doneCount" style="color: var(--success-color);">0</span>개</span>
	        <span>미입력: <span id="waitCount" style="color: #ef4444;">0</span>개</span>
	    </div>
	
	    <table class="answer-table">
	        <thead>
	            <tr>
	                <th width="15%">번호</th>
	                <th width="35%">정답</th>
	                <th width="50%">상태 / 동작</th>
	            </tr>
	        </thead>
	        <tbody id="answerTableBody">
	            <!-- JS에서 동적 생성 예정 -->
				<c:forEach items="${answers}" var="answerDto">
					<tr>
						<td class="q-num" data-question-id="${answerDto.questionId}">${answerDto.questionNum}</td>
						<td>
							<input type="number" class="answer-input" 
								value="${answerDto.correctAnswer}" min="1" max="5" 
								oninput="updateStatus(this, '${answerDto.questionNum}')"
								placeholder="?">
						</td>
						<td>
							<div style="display: flex; align-items: center; justify-content: center; gap: 15px;">
								<span id="status-${answerDto.questionNum}">
									<!-- 동적으로 추가 -->
								</span>
								<button class="btn-reset" onclick="resetInput(this, '${answerDto.questionNum}')" title="초기화">
									<i class="fas fa-redo-alt"></i>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>

	        </tbody>
	    </table>
	
	    <div class="footer-actions">
	        <button class="btn btn-primary" id="btnSaveAnswers">
	            <i class="fas fa-save"></i> 정답지 저장하기
	        </button>
	    </div>
	</div>

	<script src="<c:url value='/resources/js/admin_answer_page.js'/>"></script>
</body>
</html>