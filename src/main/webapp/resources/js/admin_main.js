// folderId 전역변수
let activeFolderId = null
let activeFolderName = null

document.addEventListener('DOMContentLoaded', () => {
    // exam_page.jsp에서 '목록으로' 버튼을 눌러서 다시 돌아온 경우 '시험지 목록' 화면으로 보여주기
    const params = new URLSearchParams(window.location.search) // 현재 URL에서 쿼리스트링만 가져옴
    if(params.get("folderId") && params.get("folderName")){
        // URL에 폴더 정보가 있으면 시험지 목록 화면으로
        activeFolderId = params.get("folderId")
        activeFolderName = params.get("folderName")
        loadExamList(params.get("folderId"), params.get("folderName"))
    } else {
        // URL에 폴더 정보가 없으면 폴더 목록 화면으로
        loadFolderView()
    }

    // 시험지 목록 - 폴더 보기 / 폴더 삭제 버튼 리스너
    document.querySelector(".folder-list-area").addEventListener('click', (e) => {
        // 폴더 보기 버튼
        const folderViewBtn = e.target.closest('.btn-view')
        if(folderViewBtn){
            activeFolderId = folderViewBtn.getAttribute('data-id')
            activeFolderName = folderViewBtn.getAttribute('data-name')

            loadExamList(activeFolderId, activeFolderName)
            return // btn-delete까지 타지 않도록 return
        }

        // 폴더 삭제 버튼
        const folderDeleteBtn = e.target.closest('.btn-delete')
        if(folderDeleteBtn){
            const folderId = folderDeleteBtn.getAttribute('data-id')
            deleteFolder(folderId)
        }
    })

    // 새 폴더 만들기 버튼 클릭 리스너
    document.querySelector(".btn-create-folder").addEventListener('click', openCreateFolderModal)

    // 폴더 목록으로 돌아가기 버튼 리스너
    document.querySelector(".btn-back-to-folders").addEventListener('click', loadFolderView)

    // 시험지 검색 버튼 리스너
    document.querySelector(".btn-search-go").addEventListener('click', searchExams)

    // 새 폴더 모달 닫기 버튼 리스너
    document.querySelector(".modal-close-btn").addEventListener('click', closeCreateFolderModal)

    // 새 폴더 모달 취소 버튼 리스너
    document.querySelector(".modal-footer .btn-cancel").addEventListener('click', closeCreateFolderModal)

    // 새 폴더 모달 생성 버튼 리스너
    document.querySelector(".modal-footer .btn-create-modal").addEventListener('click', saveFolder)

    // 시험지 목록 - 시험지 보기 / 시험지 삭제 버튼 리스너
    document.querySelector(".exam-card-grid").addEventListener('click', (e) => {
        // 시험지 보기 버튼
        const examViewBtn = e.target.closest('.btn-view')
        if(examViewBtn){
            const examId = examViewBtn.getAttribute('data-examId')
            const examTypeCode = examViewBtn.getAttribute('data-examTypeCode')
            const examTypeName = examViewBtn.getAttribute('data-examTypeName')
            const examRound = examViewBtn.getAttribute('data-examRound')
            const examSubject = examViewBtn.getAttribute('data-examSubject')

            viewExam(examId, examTypeCode, examTypeName, examRound, examSubject)
        }

        // 시험지 삭제 버튼
        const examDeleteBtn = e.target.closest('.btn-delete')
        if(examDeleteBtn){
            const confirmed = confirm('시험지를 삭제하시겠습니까?')
            if(!confirmed) {return}

            fetchExamDelete([examDeleteBtn.getAttribute('data-examId')])
        }
    })

    // 시험지 이동 버튼 리스너
    document.querySelector(".exam-list-actions").addEventListener('click', (e) => {
        const examMoveBtn = e.target.closest('#btn-toggle-move-mode') // 시험지 이동 버튼
        if(examMoveBtn){
            toggleMoveMode() // 체크 박스 활성화
        }
    })

    // 시험지 이동 모드에서 체크박스 선택 감지 리스너
    document.querySelector(".exam-card-grid").addEventListener('change', (e) => {
        if(e.target.classList.contains('exam-select-checkbox')){
            updateBulkActionBar()
        }
    })

    // 하단 바(선택항목이동) - 모달 열기 버튼 리스너
    document.querySelector("#btn-move-selected").addEventListener('click', showFolderSelectionModal)

    // 하단 바(선택항목이동) - 시험지 삭제 버튼 리스너
    document.querySelector("#btn-delete-selected").addEventListener('click', deleteSelectedExams)

    // 하단 바(선택항목이동) - 닫기 버튼 리스너
    document.querySelector("#folderSelectionModal .modal-close-btn").addEventListener('click', closeFolderSelectionModal)

    // 하단 바(선택항목이동) - 취소 버튼 리스너
    document.querySelector("#folderSelectionModal .btn-cancel").addEventListener('click', closeFolderSelectionModal)

    // 하단 바(선택항목이동) - 폴더 리스트 중 옵션을 선택했을 때 리스너
    document.querySelector("#folderSelectionModal").addEventListener('click', (e) => {
        const option = e.target.closest('.folder-option')
        if(option){
            const folderId = option.getAttribute("data-folder-id")
            selectTargetFolder(folderId)
        }
    })

    // '새 시험지 등록' 모달 열기 리스너
    document.querySelector(".btn-create-exam").addEventListener('click', openCreateExamModal)

    // '새 시험지 등록' 모달 닫기 리스너
    document.querySelector("#createExamModal .btn-cancel").addEventListener('click', closeCreateExamModal)
    document.querySelector("#createExamModal .modal-close-btn").addEventListener('click', closeCreateExamModal)

    // PDF 파일 선택 버튼 클릭 리스너
    const pdfFileInput = document.querySelector("#pdfFileInput")
    document.querySelector(".btn-upload-trigger").addEventListener('click', () => {
        pdfFileInput.click()
    })

    // PDF 파일 업로드 감지 리스너
    const pdfFileNameSpan = document.querySelector("#pdfFileName")
    pdfFileInput.addEventListener('change', (e) => {
        const fileName = e.target.files.length > 0 ? e.target.files[0].name : '선택된 파일 없음'
        pdfFileNameSpan.textContent = fileName
    })

    // 직접 등록하기 버튼 클릭 리스너
    document.querySelector(".btn-manual-register").addEventListener('click', () => {
        // 직접 등록 페이지로 이동 로직
        closeCreateExamModal()
        location.href = `/admin/createExamPage?folderId=${activeFolderId}`
    })

})


/* 폴더 목록 뷰와 시험지 목록 뷰 전환 관련 함수들
================================================== */

// 폴더 목록 뷰와 시험지 목록 뷰 DOM 요소
const folderView = document.getElementById('folder-view')
const examListView = document.getElementById('exam-list-view')
const mainTitle = document.getElementById('main-dashboard-title')
const btnCreateExam = document.getElementById('btn-create-exam')

// 폴더 목록으로 전환하는 함수
const loadFolderView = () => {
    const cleanUrl = window.location.pathname       // 현재 URL에서 쿼리스트링 '?' 앞의 경로만 가져옴
    window.history.replaceState({}, '', cleanUrl)   // URL 초기화

    mainTitle.innerText = '시험 카테고리 관리'         // 제목 변경
    examListView.style.display = 'none'             // 시험지 뷰 숨김
    folderView.style.display = 'block'              // 폴더 뷰 표시
    btnCreateExam.style.display = 'none'            // 새 시험지 등록 버튼 숨김

    clearSelections()                               // 초기화
}

// 시험지 목록으로 전환하고 데이터를 로드하는 함수
const loadExamList = (folderId, folderName) => {
    mainTitle.innerText = folderName                // 제목 변경
    folderView.style.display = 'none'               // 폴더 뷰 숨김
    examListView.style.display = 'block'            // 시험지 뷰 표시
    btnCreateExam.style.display = 'inline-flex'     // 새 시험지 등록 버튼 표시
    
    loadExamListData(folderId)                      // 해당 폴더의 시험지 목록 데이터 로드
}


/* 모달 관련 함수들
================================================== */

// 모달 DOM 요소
const createFolderModal = document.querySelector('#createFolderModal')
const newFolderName = document.querySelector('#newFolderName')

// 새 폴더 만들기 모달 열기 함수
const openCreateFolderModal = () => {
    createFolderModal.style.display = 'flex'
    newFolderName.value = ''
    newFolderName.focus()
}

// 새 폴더 만들기 모달 닫기 함수
const closeCreateFolderModal = () => {
    createFolderModal.style.display = 'none'
}

// 새 폴더 저장 함수
const saveFolder = () => {
    const folderName = newFolderName.value.trim()

    if(folderName === '') {
        alert('폴더명을 입력해주세요.')
        newFolderName.focus()
        return
    } else {
        const data = {
            folderName: folderName,
        }
        
        axios.post('/admin/saveFolder', data)
            .then(response => {
                // 폴더 추가 성공 시 모달창 닫기
                closeCreateFolderModal()

                // 폴더 추가 성공 시 폴더 리스트 재요청
                if(response.data){
                    axios.get('/admin/folders')
                        .then(response => {
                            const folderList = response.data
                            const output = renderFolderList(folderList)

                            folderListArea.innerHTML = output
                        })
                        .catch(error => {
                            console.error('error: ', error)
                        })
                } else {
                    alert('이미 생성된 폴더입니다.')
                }

            })
            .catch(error => {
                console.error('error: ', error)
            })
    }
}


/* 폴더 목록 관리 페이지 관련 함수들
================================================== */

// DOM 요소
const folderListArea = document.querySelector(".folder-list-area") // 목록 부분

// 폴더 리스트 UI 업데이트
const renderFolderList = (folderList) => {
    let output = ''

    if(folderList.length == 0){
        output += 
        `
            <div class="no-content">
                <i class="fas fa-box-open"></i>
                <p>등록된 카테고리(폴더)가 없습니다. 새로운 폴더를 만들어 관리하세요.</p>
            </div>
        `
    } else {
        folderList.forEach((folderDto) => {
            
            output += 
            `
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
            `
            if(folderDto.folderId == 1){
                // '폴더 미지정'은 삭제 버튼 없음
                output += 
                `
                        </div>
                    </div>
                `
            } else {
                // '폴더 미지정'이 아닌 경우에는 삭제 버튼 보여줌
                output +=
                `
                            <button class="btn btn-delete" data-id="${folderDto.folderId}">
                                삭제
                            </button>
                        </div>
                    </div>
                `
            }
        })
    }

    return output
}

// 폴더 리스트 요청
const fetchFolderList = () => {
    axios.get('/admin/folders')
        .then(response => {
            const folderList = response.data
            const output = renderFolderList(folderList)

            folderListArea.innerHTML = output
        })
        .catch(error => {
            console.error('error: ', error)
        })
}

// 폴더 삭제 함수
const deleteFolder = (folderId) =>{
    axios.delete(`/admin/deleteFolder/${folderId}`)
        .then(response => {
            // 폴더 삭제 성공 시 폴더 리스트 재요청
            if(response.data){
                axios.get('/admin/folders')
                    .then(response => {
                        const folderList = response.data
                        const output = renderFolderList(folderList)

                        folderListArea.innerHTML = output
                    })
                    .catch(error => {
                        console.error('error: ', error)
                    })
            } else {
                alert('알 수 없는 오류가 발생했습니다. 새로고침 후 다시 시도해주세요.')
            }
        })
        .catch(error => {
            console.error('error: ', error)
        })
}

// 폴더명 편집 함수
const editFolder = () =>{}


/* 시험지 관리 페이지 관련 함수들
================================================== */
const createExamModal = document.querySelector("#createExamModal")

// 시험지 등록 모달 열기
const openCreateExamModal = () => {
    createExamModal.style.display = 'flex'
}

// 시험지 등록 모달 닫기
const closeCreateExamModal = () => {
    createExamModal.style.display = 'none'
}

// 시험지 PDF 업로드 등록 함수


// 시험지 직접 등록 함수
const createExam = () => {}

// 일괄 선택 버튼 로드 함수
const loadBulkActionBtn = () => {
    const listAction = document.querySelector(".exam-list-actions")
    let output = ''

    output += 
    `
        <button id="btn-toggle-move-mode" class="btn btn-secondary">
            <i class="far fa-check-square"></i> 편집 모드
        </button>
    `

    listAction.innerHTML = output
}

// 시험지 목록 로드 함수
const loadExamListData = (folderId) => {

    // 시험지 이동 버튼 컨테이너
    const listAction = document.querySelector(".exam-list-actions")
    listAction.innerHTML = '' // 기존 내용 초기화

    // 시험지 목록 컨테이너
    const examCard = document.querySelector(".exam-card-grid")
    examCard.innerHTML = ''  // 기존 내용 초기화


    // 비동기 요청으로 시험지 목록 데이터 가져오기
    const params = {
        folderId: folderId
    }
    
    axios.get('/admin/examList', { params })
        .then(response => {
            const list = response.data
            let output = ''

            if(list.length == 0){
                output += 
                `
                    <div class="no-exams-message">
                        <i class="fas fa-box-open"></i>
                        <p>등록된 시험지 목록이 없습니다. 새로운 시험지를 등록해주세요.</p>
                    </div>
                `
            } else {
                list.forEach((examTitleDto) => {

                    output += 
                    `
                        <div class="exam-card" data-id="${examTitleDto.examId}">
                            <input type="checkbox" class="exam-select-checkbox" data-exam-id="${examTitleDto.examId}">

                            <div class="card-header">
                                <span class="tag tag-${examTitleDto.examSubject}">${examTitleDto.examSubject}</span>
                                <span class="tag tag-round">${examTitleDto.examRound}</span>
                            </div>
                            <h3 class="card-title">${examTitleDto.displayTitle}</h3>
                            <div class="card-meta">
                                <p><i class="fas fa-question-circle"></i> 문항 수: <strong>${examTitleDto.totalCount}개</strong></p>
                                <p><i class="fas fa-calendar-alt"></i> 등록일: 2025-01-01</p>
                            </div>
                            <div class="card-actions">
                                <button class="btn btn-action btn-view" 
                                    data-examId="${examTitleDto.examId}"
                                    data-examTypeCode="${examTitleDto.examTypeCode}"
                                    data-examTypeName="${examTitleDto.examTypeName}"
                                    data-examRound="${examTitleDto.examRound}"
                                    data-examSubject="${examTitleDto.examSubject}"
                                >
                                    보기
                                </button>
                                <button class="btn btn-action btn-delete" data-examId="${examTitleDto.examId}">
                                    삭제
                                </button>
                            </div>
                        </div>
                    `
                })

                loadBulkActionBtn()
            }
            examCard.innerHTML = output

        })
        .catch(error => {
            console.error('error: ', error)
        })
}

// 시험지 상세보기 페이지로 이동하는 함수
const viewExam = (examId, examTypeCode, examTypeName, examRound, examSubject) => {
    window.location.href = 
        `/admin/showExamPage?examId=${examId}` +
        `&examTypeEng=${examTypeCode}` +
        `&examTypeKor=${examTypeName}` + 
        `&examRound=${examRound}` + 
        `&examSubject=${examSubject}`
}

// 시험지 삭제 함수
const fetchExamDelete = (examIds) => {
    const data = {
        examIds: examIds
    }
    
    axios.patch('/exam/deleteExams', data)
        .then(response => {
            if(response.data){
                // 1. 시험지 목록 UI 업데이트
                loadExamListData(activeFolderId)

                // 2. 폴더 목록 UI 업데이트
                fetchFolderList()
            } else {
                alert("일시적인 문제가 발생하였습니다. 새로고침 후 다시 이용해주세요.")
            }
        })
        .catch(error => {
            console.error('error: ', error)
        })
}

// 시험지 검색 함수 (AJAX로 구현 예정)
const searchExams = () => {}

// 시험지 목록 페이징 처리 함수 (AJAX로 구현 예정)
// const pagination = () => {}


/* 시험지 이동 버튼 관련 함수
================================================== */
// DOM 요소
const folderSelectionModal = document.querySelector("#folderSelectionModal") // 모달 자체
const bulkActionBar = document.querySelector("#bulk-action-bar")
let moveModeActive = false

// 폴더 목록 모달 열기 함수
const openFolderSelectionModal = () => {
    folderSelectionModal.style.display = 'flex'
}

// 폴더 목록 모달 닫기 함수
const closeFolderSelectionModal = () => {
    folderSelectionModal.style.display = 'none'
    document.querySelector("#btn-confirm-move").disabled = true
}

// 하단 액션 바 열기
const openBulkActionBar = () => {
    bulkActionBar.style.display = 'flex'
}

// 하단 액션 바 숨기기
const closeBulkActionBar = () => {
    bulkActionBar.style.display = 'none'
}

// 체크박스 표시/숨김 함수
const toggleMoveMode = () => {
    moveModeActive = !moveModeActive 

    // 체크 해제 및 숨김
    const checkboxesInput = document.querySelectorAll(".exam-select-checkbox")

    checkboxesInput.forEach((box) => {
        box.style.display = moveModeActive ? 'block' : 'none'
        box.checked = false // 모드 전환 시 초기화
    })

    updateBulkActionBar() // 모드 전환 후 상태 업데이트
}

// 하단 일괄 처리 바 표시/숨김 함수
const updateBulkActionBar = () => {
    const selectedCheckboxes = document.querySelectorAll(".exam-select-checkbox:checked")

    if(moveModeActive && selectedCheckboxes.length > 0){
        // 1개 이상 체크되었다면
        openBulkActionBar()
        document.querySelector("#selected-count").textContent = selectedCheckboxes.length
    } else {
        closeBulkActionBar()
    }
}

// 목록 모달창에서 선택한 폴더의 UI 업데이트
let selectedFolderId = null
const selectTargetFolder = () => {
    const folderModal = document.querySelector("#folderSelectionModal")
    const confirmMoveBtn = document.querySelector("#btn-confirm-move")

    folderModal.addEventListener('click', (e) => {
        const clicked = e.target.closest('.folder-option')
        if (!clicked) return
        
        // 모든 .folder-option에서 selected 클래스 제거
        document.querySelectorAll(".folder-option").forEach((option) => {
            option.classList.remove('selected')
        })  

        // 클릭한 요소에만 selected 클래스 추가
        clicked.classList.add('selected')
        selectedFolderId = clicked.getAttribute('data-folder-id')
        confirmMoveBtn.disabled = false
    })
}

// 모달창의 폴더 목록 UI 업데이트
const renderFolderListExcluding = (folders) => {
    let output = ''

    if(folders.length == 0) {
        output +=
        `
            <p style="text-align: center;">현재 저장된 폴더가 없습니다.</p>
        `
    } else {
        folders.forEach((folder) => {
            output += 
            `
                <div class="folder-option" data-folder-id="${folder.folderId}">
                    <i class="fas fa-folder"></i> 
                    ${folder.folderName} 
                    <span style="color: #888; margin-left: auto;">(${folder.examCount}개)</span>
                </div>
            `
        })
    }

    return output
}

// '선택 항목 이동' 클릭 시 모달창 설정
const showFolderSelectionModal = () => {
    // 1. 모달창 열기
    openFolderSelectionModal()

    // 2. 현재 폴더를 제외한 나머지 폴더 목록 조회 (비동기 요청)
    const params = {
        excludeFolderId: activeFolderId
    }
    
    axios.get('/admin/getFolderListExcluding', { params })
        .then(response => {
            // 모달창의 폴더 목록 UI 업데이트
            const output = renderFolderListExcluding(response.data)
            document.querySelector("#folderListForMove").innerHTML = output

            // 총 ''개 항목 이동 예정< 부분 UI 업데이트
            const selectedCheckboxes = document.querySelectorAll(".exam-select-checkbox:checked")
            const count = selectedCheckboxes.length
            document.querySelector("#move-exam-count").textContent = count
        })
        .catch(error => {
            console.error('error: ', error)
        })

}

// 선택된 시험지를 지정된 폴더로 이동
const moveExamsToFolder = () => {
    // 이동시킬 시험지의 examId
    const selectedCheckboxes = document.querySelectorAll(".exam-select-checkbox:checked")
    let selectedExamIds = []

    selectedCheckboxes.forEach((box) => {
        selectedExamIds.push(box.getAttribute('data-exam-id'))
    })

    const data = {
        folderId: selectedFolderId,
        examIds: selectedExamIds
    }
    
    axios.patch('/admin/moveExamsToFolder', data)
        .then(response => {
            if(response.data){
                // 시험지 목록 UI 업데이트
                loadExamListData(activeFolderId)

                // 모달창 닫기
                closeFolderSelectionModal()

                // 폴더 목록 UI 업데이트
                axios.get('/admin/folders')
                    .then(response => {
                        const folderList = response.data
                        const output = renderFolderList(folderList)

                        folderListArea.innerHTML = output
                    })
                    .catch(error => {
                        console.error('error: ', error)
                    })
                
                // 하단 액션 바 숨김
                closeBulkActionBar()
            }
        })
        .catch(error => {
            console.error('error: ', error)
        })

}

// 선택 항목 삭제
const deleteSelectedExams = () => {
    // confirm 창
    const confirmed = confirm('시험지를 삭제하시겠습니까?')
    if(!confirmed) {return}

    // 삭제할 시험지ID를 배열에 담기
    let selectedExamIds = []
    const checkboxes = document.querySelectorAll('.exam-select-checkbox:checked')

    checkboxes.forEach((box) => {
        selectedExamIds.push(box.getAttribute('data-exam-id'))
    })

    fetchExamDelete(selectedExamIds)
}


/* 초기화 함수
================================================== */
const clearSelections = () => {
    // 이동 모드 비활성화
    moveModeActive = false

    // 체크박스 모두 선택 해제 및 숨김
    const checkboxesInput = document.querySelectorAll(".exam-select-checkbox")
    checkboxesInput.forEach((box) => {
        box.style.display = 'none'
        box.checked = false
    })

    // 하단 액션바 숨기기
    closeBulkActionBar()

    // 폴더 id 초기화
    activeFolderId = null
}
