<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>시험지 등록 페이지</title>
<link href="<c:url value="/resources/css/admin_exam_create_page.css"/>" rel="stylesheet">

<!-- axios -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!-- font-awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">

<!-- Quill 에디터 -->
<link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
</head>
<body>
	<div class="exam_create_page">
		<div id="exam-creation-form">
			<div class="exam-info-header">
				<h2>새 시험지 문항 직접 등록</h2>

				<!-- <div class="form-group">
					<label for="examTitle">시험지 제목</label>
					<input type="text" id="examTitle" class="form-control" placeholder="예: 2024년 1회차 고졸 검정고시 국어">
				</div> -->
				
				<div class="exam-info-controls">
					<div class="form-group">
						<label for="examType">시험 유형</label>
						<select id="examType" class="form-control">
							<option value="" selected disabled>유형 선택</option>
							<c:forEach items="${examtypes}" var="type">
								<option value="${type.examTypeCode}">${type.examTypeName}</option>
							</c:forEach>
						</select>
					</div>
					
					<div class="form-group">
						<label for="examRound">시험 회차</label>
						<input type="text" id="examRound" class="form-control" placeholder="예: 2023년도 제1회">
						<!-- <select id="examRound" class="form-control">
							<option value="" selected disabled>회차 선택</option>
							<option value="1">2023년도 제1회</option>
							<option value="2">2025년도 제1회</option>
						</select> -->
					</div>
					
					<div class="form-group">
						<label for="examSubject">과목</label>
						<select id="examSubject" class="form-control">
							<option value="" selected disabled>과목 선택</option>
	<!-- 						<option value="국어">국어</option> -->
	<!-- 						<option value="사회">사회</option> -->
						</select>
					</div>
				</div>

			</div>
			
			<div id="question-list-container"> 
				<!-- 동적으로 추가 -->
			</div>

			<!-- 공통지문 설정 모달창 -->
			<div id="commonPassageModal" class="modal" style="display:none;">
				<div class="modal-content">
					<span class="close-button">&times;</span>
					<h3>공통 지문 설정</h3>

					<div class="usage-tip-box">
						<p class="usage-tip-text">
							<i class="fas fa-lightbulb"></i> <strong>공통 지문 작성 Tip</strong>
							<br>
							문항 범위가 같은 지문은 <strong>최초 1회만</strong> 작성 후 <strong>임시 저장</strong>하세요. 
							다른 문항에서는 <strong>[등록된 공통 지문 보기]</strong>에서 바로 가져와 적용할 수 있습니다.
						</p>
						<p class="usage-steps-text">
							<strong>사용 순서:</strong> 1. 지문 입력 → 2. 임시 저장 → 3. 작성 완료 (또는 등록된 지문 목록에서 적용 후 완료)
						</p>
					</div>
					
					<div class="form-group">
						<label>지문 내용</label>
						<div class="passage-controls" id="modal-passage-controls">
							<button type="button" class="btn btn-sm btn-outline-secondary modal-btn-passage-type" data-type="text">
								<i class="fas fa-file-alt"></i> 텍스트 지문
							</button>
							<button type="button" class="btn btn-sm btn-outline-secondary modal-btn-passage-type" data-type="image">
								<i class="fas fa-image"></i> 이미지 지문
							</button>
						</div>
						<div id="modal-passage-input" class="passage-content" style="margin-top: 10px;">
							<p class="placeholder-text">지문 유형을 선택해주세요.</p>
						</div>
					</div>

					<div class="form-group">
						<label for="common-passage-range">적용할 문항 범위</label>
						<input type="text" id="common-passage-range" class="form-control" 
							placeholder="적용할 문항 번호를 입력하세요 (예: 1~3)">
					</div>

					<div class="modal-footer">
						<div class="left-group">
							<button type="button" class="btn btn-primary" id="btnSaveCommonPassageModal">
								<i class="fas fa-save"></i> 임시 저장
							</button>
							<button type="button" class="btn btn-outline-info" id="btnShowCommonPassageList">
								<i class="fas fa-list-alt"></i> 등록된 공통 지문 보기
							</button>
						</div>

						<button type="button" class="btn btn-success" id="btnCompleteCommonPassage">
							<i class="fas fa-check-circle"></i> 작성 완료
						</button>
					</div>

					<!-- 등록된 공통 지문 목록 컨테이너 -->
					<div id="commonPassageListContainer"></div>

				</div>
			</div>
		
			<button id="btnAddQuestion" class="btn btn-primary btn-lg btn-block" style="margin-top: 20px;">
				<i class="fas fa-plus-circle"></i> 문항 추가
			</button>
			
			<button id="btnSaveExam" class="btn btn-success btn-lg btn-block" style="margin-top: 10px;">
				<i class="fas fa-save"></i> 시험지 최종 등록
			</button>
		</div>
		
		<!-- 토스트 메시지 -->
		<div id="toastMessage" class="toast-message"></div>
	</div>

	<script src="<c:url value='/resources/js/admin_exam_edit_page.js'/>"></script>
	<script src="<c:url value='/resources/js/admin_exam_create_page.js'/>"></script>

	<!-- Quill 에디터 -->
    <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script> 
    <script src="https://cdn.jsdelivr.net/npm/quill-image-resize-module@3.0.0/image-resize.min.js"></script>
</body>
</html>