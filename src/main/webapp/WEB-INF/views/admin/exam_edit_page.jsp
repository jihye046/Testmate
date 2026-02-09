<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>시험지 수정 페이지</title>
<link href="<c:url value="/resources/css/admin_exam_create_page.css"/>" rel="stylesheet">
<link href="<c:url value="/resources/css/admin_exam_edit_page.css"/>" rel="stylesheet">

<!-- axios -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!-- font-awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">

<!-- Quill 에디터 -->
<link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">
</head>
<body>
	<div class="exam_edit_page">
		<div id="exam-creation-form">
			<div class="exam-info-header edit" style="display: flex;">
				<h2>시험지 수정</h2>
				<button class="btn btn-back" 
					onclick="location.href='/admin/showExamPage?examId=${map.examId}&examTypeEng=${map.examTypeEng}&examTypeKor=${map.examTypeKor}&examRound=${map.examRound}&examSubject=${map.examSubject}'"
				>
					시험지로 돌아가기
				</button>
			</div>
			
			<!-- 문제 컨테이너 -->
			<div id="question-list-container"> 
				<c:set var="q" value="${examPageDto.examQuestion}" />

				<div class="question-item card" data-question-num="${q.questionNum}">
					<div class="question-header">
						<h4>제 ${examPageDto.examQuestion.questionNum} 문항</h4>
	<%-- 	                <button class="btn btn-sm btn-danger btn-remove-question" data-question-num="${q.questionNum}"> --%>
	<!-- 	                    <i class="fas fa-trash-alt"></i> 삭제 -->
	<!-- 	                </button> -->
					</div>
					
					<div class="question-body">
						<div class="form-group passage-group">
		
							<div class="passage-controls common-passage-check-commonContentContainer">
								<label class="form-check-label" for="common-passage-toggle-${q.questionNum}">
									공통 지문
								</label>
									<input class="form-check-input common-passage-toggle" 
										type="checkbox" 
										id="common-passage-toggle-${q.questionNum}" 
										data-q-num="${q.questionNum}"
										<c:if test="${not empty q.commonPassage}">checked</c:if> 
									>		
									<button <c:if test="${empty q.commonPassage}">disabled</c:if> id="commonPassageViewBtn">
										<i class="fas fa-search"></i> 공통 지문 설정
									</button>
							</div>
		
							<div class="passage-controls passage-container" id="passage-controls-${q.questionNum}">
								<label class="form-check-label">
									개별 지문
								</label>
								<button type="button" class="btn btn-sm btn-outline-secondary btn-passage-type" data-type="text" data-q-num="${q.questionNum}">
									<i class="fas fa-file-alt"></i> 텍스트 지문
								</button>
								<button type="button" class="btn btn-sm btn-outline-secondary btn-passage-type" data-type="image" data-q-num="${q.questionNum}">
									<i class="fas fa-image"></i> 이미지 지문
								</button>
							</div>
							<div class="passage-content" id="passage-content-${q.questionNum}">
								<p class="placeholder-text">지문 유형을 선택해주세요.</p>
	<!-- 	                    <textarea class="form-control no-resize passage-text" rows="6" data-q-num="1" maxlength="1000" placeholder="문항에 필요한 지문 내용을 입력하세요."></textarea> -->
							</div>
						</div>
		
						<div class="form-group">
							<label>문항 내용</label>
							<textarea 
								class="form-control no-resize question-text" 
								rows="4" 
								placeholder="문항 내용을 입력하세요."
							>${q.questionText}</textarea>
						</div>
		
						<div class="form-group options-group">
							<label>선택지</label>
							<div class="option-inputs">
								<c:forEach items="${examPageDto.examChoices}" var="choices">
									<div class="option-item-1">
										<input type="text" 
											class="form-control option-input" 
											data-choice-num="${choices.choiceNum}" 
											placeholder="보기 ${choices.choiceNum}"
											value="${choices.choiceText}"
										>
										<button type="button" class="btn btn-danger btn-sm btn-remove-option">
											<i class="fas fa-times"></i>
										</button>
									</div>	
								</c:forEach>
							</div>
	<!-- 	                    <button type="button" class="btn btn-sm btn-outline-primary btn-add-option"> -->
	<!-- 	                        + 보기 추가 -->
	<!-- 	                    </button> -->
						</div>
		
						<div class="form-group answer-group">
							<label for="answer-${examPageDto.examAnswer.correctAnswer}">정답</label>
							<input type="number" 
								id="answer-${examPageDto.examAnswer.correctAnswer}" 
								class="form-control question-answer" 
								min="1" 
								max="5" 
								placeholder="정답 번호 (예: 3)"
								value="${examPageDto.examAnswer.correctAnswer}"
							>
						</div>
					</div>
				</div>
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
						<div class="passage-content" id="modal-passage-input" style="margin-top: 10px;">
							<p class="placeholder-text">지문 유형을 선택해주세요.</p>
						</div>
					</div>
		
					<div class="form-group">
						<label for="common-passage-range">적용할 문항 범위</label>
						<input type="text" id="common-passage-range" class="form-control" 
							value="${q.passageScope}"
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
			
			<button id="btnSaveExam" class="btn btn-success btn-lg btn-block" style="margin-top: 10px;">
				<i class="fas fa-save"></i> 수정
			</button>
		</div>
		
		<!-- 토스트 메시지 -->
		<div id="toastMessage" class="toast-message"></div>
	</div>
    
    <script src="<c:url value='/resources/js/admin_exam_create_page.js'/>"></script>
    <script src="<c:url value='/resources/js/admin_exam_edit_page.js'/>"></script>
	
	<!-- Quill 에디터 -->
    <script src="https://cdn.quilljs.com/1.3.6/quill.min.js"></script> 
    <script src="https://cdn.jsdelivr.net/npm/quill-image-resize-module@3.0.0/image-resize.min.js"></script>

	<div class="hidden-data" id="examPageDtoJson" data-exam-page-dto-json="${fn:escapeXml(examPageDtoJson)}"></div>
</body>
</html>