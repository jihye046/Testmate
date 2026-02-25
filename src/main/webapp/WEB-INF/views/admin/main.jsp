<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 메인 페이지</title>
<link href="<c:url value="/resources/css/admin_main.css"/>" rel="stylesheet">

<!-- axios -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!-- font-awesome -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
	<div class="admin-container">
		<div>
			<a href="/exam/main">사용자 메인으로 이동</a> 
		</div>

		<header class="admin-header">
			<h1 class="header-title" id="main-dashboard-title">시험 카테고리 관리</h1> 
			<button class="btn btn-create-exam" onclick="location.href='#'" id="btn-create-exam">
				<i class="fas fa-file-circle-plus"></i> 새 시험지 등록
			</button>
		</header>

		<!-- 폴더 view -->
		<div id="folder-view" class="dashboard-content-area">

			<div class="actions-header">
				<button class="btn btn-create-folder">
					<i class="fas fa-folder-plus"></i> 새 폴더 만들기
				</button>
			</div>

            <div class="exam-card-list folder-list-area">
				<!-- 등록된 폴더가 있을 때 -->
                <c:forEach items="${folderList}" var="folderDto">
                    <div class="exam-card folder-card">
                        <div class="folder-info">
                            <i class="fas fa-folder folder-icon"></i>
                            <h2 class="folder-title">${folderDto.folderName}</h2>
                            
                            <!-- 문항 수 태그 -->
                            <div class="folder-meta">
								<span class="tag tag-count">
									<i class="fas fa-hashtag"></i> 
									${folderDto.examCount}개
								</span>
							</div>
                        </div>
                        
                        <div class="folder-actions">
                            <button class="btn btn-view" data-id="${folderDto.folderId}" data-name="${folderDto.folderName}">
                                보기
                            </button>
							<c:if test="${folderDto.folderId != 1}">
								<button class="btn btn-delete" data-id="${folderDto.folderId}">
									삭제
								</button>
							</c:if>
                        </div>
                    </div>
                </c:forEach>
                
                <!-- 등록된 폴더가 없을 때 -->
                <c:if test="${empty folderList}">
                    <div class="no-content">
                        <i class="fas fa-box-open"></i>
                        <p>등록된 카테고리(폴더)가 없습니다. 새로운 폴더를 만들어 관리하세요.</p>
                    </div>
                </c:if>
                
            </div>
        </div>
        
        <!-- 시험지 view -->
        <div id="exam-list-view" class="dashboard-content-area" style="display:none;">
            
            <div class="actions-header exam-list-header">
				<button class="btn btn-back-to-folders">
                    <i class="fas fa-arrow-left"></i> 폴더 목록으로
                </button>
            </div>
            
			<!-- 검색 필터 섹션 -->
            <div class="filter-box">
				<form id="searchForm" class="filter-controls">
					<div class="input-group main-search">
						<i class="fas fa-search search-icon"></i>
						<input type="text" id="searchKeyword" name="keyword" placeholder="시험지 제목, 과목, 유형 등으로 검색하세요" class="form-control">
						<button type="submit" class="btn btn-search-go">검색</button>
					</div>
			
					<div class="select-group">
						<select id="selectExamType" name="type" class="form-control select-filter ">
							<option value="" disabled selected>유형 선택</option>
							<!-- 서버로부터 받은 데이터로 동적으로 설정 -->
						</select>

						<select id="selectSubject" name="subject" class="form-control select-filter">
							<option value="" disabled selected>시험 유형을 선택해주세요</option>
							<!-- 시험 유형에 따라 동적으로 설정 -->
						</select>

						<select id="selectYear" name="year" class="form-control select-filter">
							<option value="" disabled selected>시험 유형을 선택해주세요</option>
							<!-- 현재 연도부터 과거 10년치를 동적으로 설정 -->
						</select>

						<select id="selectRound" name="round" class="form-control select-filter">
							<option value="" disabled selected>시험 유형을 선택해주세요</option>
							<!-- 시험 유형에 따라 동적으로 설정 -->
						</select>
					</div>
				</form>
			</div>
            
			<!-- 시험지 이동 -->
			<div class="exam-list-actions"><!-- 폴더 클릭 시 동적으로 생성 --></div>
			
			<!-- 시험지 목록 섹션 -->
			<div class="exam-card-grid"><!-- 폴더 클릭 시 동적으로 생성 --></div>
			
			<!-- 페이징 -->
			<!-- <div class="pagination-area">
				<button class="page-btn">&laquo;</button>
				<button class="page-btn active">1</button>
				<button class="page-btn">2</button>
				<button class="page-btn">3</button>
				<button class="page-btn">&raquo;</button>
			</div> -->
		</div>

		<!-- 하단 액션 바 -->
		<div id="bulk-action-bar" class="bulk-action-bar" style="display: none;">
			<div class="action-summary">
				<span id="selected-count">0</span>개 항목 선택됨
			</div>
			<div class="action-buttons">
				<button id="btn-move-selected" class="btn btn-primary">
					<i class="fas fa-folder-open"></i> 선택 항목 이동
				</button>
				<button id="btn-delete-selected" class="btn btn-danger">
					<i class="fas fa-trash-alt"></i> 선택 항목 삭제
				</button>
			</div>
		</div>

	</div>
	
    <!-- 새폴더 만들기 모달창 -->
    <div id="createFolderModal" class="modal-overlay">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title">새 폴더 만들기</h4>
				<button type="button" class="modal-close-btn">
					<i class="fas fa-times"></i>
				</button>
			</div>
			<div class="modal-body">
				<label for="newFolderName">폴더 이름</label>
				<input type="text" id="newFolderName" class="form-control" placeholder="새로운 폴더 이름을 입력하세요" maxlength="50">
				<p class="input-description">폴더 이름은 시험의 카테고리를 분류하는 데 사용됩니다.</p>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-cancel">취소</button>
				<button type="button" class="btn btn-create-modal" id="btn-save-folder">
					<i class="fas fa-save"></i> 생성
				</button>
			</div>
		</div>
	</div>

	<!-- 선택 항목 이동 시 보여줄 폴더 목록 모달창 -->
	<div id="folderSelectionModal" class="modal-overlay">
		<div class="modal-content" style="max-width: 540px;">
			<div class="modal-header">
				<h4 class="modal-title">시험지를 이동할 폴더를 선택하세요</h4>
				<button type="button" class="modal-close-btn">
					<i class="fas fa-times"></i>
				</button>
			</div>
			
			<div class="modal-body">
				<div id="folderListForMove" class="folder-selection-list">
					<p style="text-align: center;"><i class="fas fa-spinner fa-spin"></i> 폴더 목록 로딩 중...</p>
				</div>
				<p class="selection-info">총 <strong id="move-exam-count">0</strong>개 항목 이동 예정</p>
			</div>
			
			<div class="modal-footer">
				<button class="btn btn-cancel">취소</button>
				<button class="btn btn-primary" id="btn-confirm-move" disabled onclick="moveExamsToFolder()">
					<i class="fas fa-paper-plane"></i> 이동 실행
				</button>
			</div>
		</div>
	</div>

	<!-- 새 시험지 등록  모달창 -->
	<div id="createExamModal" class="modal-overlay">
		<div class="modal-content" style="max-width: 600px;"> 
			<div class="modal-header">
				<h4 class="modal-title">새 시험지 등록 방법 선택</h4>
				<button type="button" class="modal-close-btn" data-modal-id="createExamModal">
					<i class="fas fa-times"></i>
				</button>
			</div>
			
			<div class="modal-body">
				<!-- PDF 업로드하여 등록 -->
				<div class="registration-option pdf-upload-option">
					<h5 class="option-title"><i class="fas fa-file-pdf"></i> PDF 파일로 등록</h5>
					<p class="option-description">PDF 파일 업로드를 통해 자동으로 문항을 분석하여 등록합니다.</p>
					
					<div class="file-upload-area">
						<input type="file" id="pdfFileInput" accept=".pdf" style="display: none;">
						<button class="btn btn-secondary btn-upload-trigger">
							<i class="fas fa-upload"></i> PDF 파일 선택
						</button>
						<span id="pdfFileName" class="file-name-display">선택된 파일 없음</span>
					</div>

					<!-- PDF 업로드 시 나타날 화면 -->
					<div id="pdf-analysis-section" class="analysis-options-section" style="display:none; margin-top: 20px;">
						<!-- OCR 옵션 -->
<!-- 						<div class="option-card-mini"> -->
<!-- 							<label class="checkbox-container"> -->
<!-- 								<input type="checkbox" id="ocrOption" checked> -->
<!-- 								<span class="checkmark"></span> -->
<!-- 								<span class="option-text">이미지 텍스트 인식(OCR) 포함</span> -->
<!-- 							</label> -->
<!-- 						</div> -->
						
						<!-- PDF 분석 성공 시 미리보기 -->
						<div id="previewContainer" style="display: none; margin-top: 20px;">
							<h5 class="fw-bold"><i class="fas fa-file-alt"></i> 추출 결과 미리보기</h5>
							<div id="textPreview" style="background: #f8f9fa; border: 1px solid #ddd; padding: 15px; 
								max-height: 400px; overflow-y: auto; white-space: pre-wrap; border-radius: 8px; font-size: 0.9rem;">
							</div>
							<button type="button" class="btn-convert-start" id="btnFinishConversion">
								<i class="fas fa-magic"></i> 시험지 등록
							</button>
						</div>

						<!-- 시험 정보 설정 -->
						<h4 style="margin-bottom: 20px; font-weight: 800; color: #3a3b45;">시험 정보 설정</h4>
    
						<div class="analysis-grid">
							<div class="form-group">
								<label><i class="fas fa-layer-group"></i> 시험 유형</label>
								<select id="selectExamType" class="form-select">
									<option value="" disabled selected>유형 선택</option>
									<!-- 서버로부터 받은 데이터로 동적으로 설정 -->
								</select>
							</div>

							<div class="form-group">
								<label><i class="fas fa-book"></i> 시험 과목</label>
								<select id="selectSubject" class="form-select">
									<option value="" disabled selected>시험 유형을 선택해주세요</option>
									<!-- 시험 유형에 따라 동적으로 설정 -->
								</select>
							</div>

							<div class="form-group">
								<label><i class="fas fa-calendar-check"></i> 시행 연도</label>
								<select id="selectYear" class="form-select">
									<option value="" disabled selected>시험 유형을 선택해주세요</option>
									<!-- 현재 연도부터 과거 10년치를 동적으로 설정 -->
								</select>
							</div>

							<div class="form-group">
								<label><i class="fas fa-redo"></i> 시행 회차</label>
								<select id="selectRound" class="form-select">
									<option value="" disabled selected>시험 유형을 선택해주세요</option>
									<!-- 시험 유형에 따라 동적으로 설정 -->
								</select>
							</div>
						</div>

						<!-- 분석 시작 및 시험지 등록 버튼 -->
						<div class="upload-actions" style="margin-top: 15px;">
							<button type="button" class="btn-convert-start" id="btnStartConversion">
								<i class="fas fa-magic"></i> 분석 및 변환 시작
							</button>
						</div>

						<!-- 분석 진행 상태바 (변환 시작 시 노출) -->
						<div id="loadingOverlay">
						    <div class="custom-spinner"></div>
						    <div style="color: white; margin-top: 20px; font-weight: bold; font-size: 1.2rem;">
						        PDF 시험지를 분석하고 있습니다...
						    </div>
						    <div style="color: #ccc; margin-top: 5px;">잠시만 기다려주세요.</div>
						</div>

					</div>

				</div>
				
				<hr>
				
				<!-- 직접 등록 -->
				<div class="registration-option manual-input-option">
					<h5 class="option-title"><i class="fas fa-keyboard"></i> 직접 문항 등록하기</h5>
					<p class="option-description">문항 내용을 직접 입력하여 시험지를 만듭니다.</p>
					<button class="btn btn-primary btn-manual-register">
						직접 등록하기 <i class="fas fa-arrow-right"></i>
					</button>
				</div>
			</div>
			
			<div class="modal-footer">
				<button type="button" class="btn btn-cancel" data-modal-id="createExamModal">취소</button>
			</div>
		</div>
	</div>

	<script src="<c:url value="/resources/js/admin_main.js"/>"></script>
</body>
</html>