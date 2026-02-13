// 전역 변수
const examinfoContainer = document.querySelector(".exam-info-controls")
const questionContainer = document.querySelector("#question-list-container")
const commonPassageModal = document.querySelector("#commonPassageModal")
const urlParam = new URLSearchParams(window.location.search)
const folderId = urlParam.get("folderId")
let commonPassageCounter = 1
window.passages = [] // 공통 지문 데이터를 저장할 배열


/* 시험 정보 컨테이너 핸들러
================================================== */
const ExamInfoContainerHandler = {
    init(){
        const examTypeSelect = document.querySelector('#examType')
        if(examTypeSelect){
            examTypeSelect.addEventListener('change', (e) => this.handleExamTypeChange(e))
        }
    },

    handleExamTypeChange(e){
        const examTypeSelectedValue = e.target.value
        if(!examTypeSelectedValue) return

        const params = { examTypeCode: examTypeSelectedValue }
        
        axios.get('/exam/getSubjectsForExamType', { params })
            .then(response => {
                this._updateExamSubjects(response.data)
            })
            .catch(error => {
                console.error('error: ', error)
            })
    },

    // 시험 과목 UI 초기화
    _updateExamSubjects(examSubjects){
        const examSubject = document.querySelector("#examSubject")

        let options = '<option value="" selected disabled>과목 선택</option>'
        examSubjects.forEach((subject) => {
            options += `<option value="${subject}">${subject}</option>`
        })

        examSubject.innerHTML = options
    }
}

/* 모달 컨테이너 핸들러
================================================== */
const CommonPassageHandler = {
    handleModalClick(e, {
            commonPassageModal,
            renderCommonPassageList,
            createPassageTextHtml,
            createPassageImageHtml,
            closeCommonPassageModal,
            showToastMessage,
            questionContainer
    }){
        // 공통 지문 유형 버튼
        const commonContentContainer = commonPassageModal.querySelector('#modal-passage-input')
        const questionModalNum = commonPassageModal.getAttribute("data-q-modal-num")
        const commonControls = commonPassageModal.querySelector(".passage-controls")

        // 1. 닫기 버튼 처리
        const closeBtn = e.target.closest('.close-button')
        if(closeBtn){
            const card = questionContainer.querySelector(`.question-item[data-question-num="${questionModalNum}"]`)
            // 최종 '작성 완료'를 하지 않고 닫는 경우, 활성화 상태를 취소하고 초기화함
            if(card && !card.getAttribute("data-selected-passage-id")){
                const checkbox = card.querySelector(`#common-passage-toggle-${questionModalNum}`)
                const cardViewBtn = card.querySelector(`#commonPassageViewBtn-${questionModalNum}`)
                checkbox.checked = false    // 체크박스 해제
                cardViewBtn.disabled = true // '공통 지문 설정' 버튼 비활성화
            } 

            closeCommonPassageModal()
            return
        }

        // 2. 지문 유형 버튼 (Text / Image)
        const commonPassageTypeBtn = e.target.closest('.modal-btn-passage-type')
        if(commonPassageTypeBtn){
            const dataType = commonPassageTypeBtn.getAttribute("data-type")

            commonControls.querySelectorAll('.modal-btn-passage-type').forEach((btn) => resetButton(btn))
            activeButton(commonPassageTypeBtn)

            if(dataType == 'text'){
                commonContentContainer.innerHTML = createPassageTextHtml(questionModalNum)
                window.edit_common.renderPassageInput('modal', 'text', '')
            } else if(dataType == 'image'){
                commonContentContainer.innerHTML = createPassageImageHtml(questionModalNum)

                const commonContentFile = commonContentContainer.querySelector('.passage-image-file')
                if(commonContentFile){
                    commonContentFile.addEventListener('change', (e) => {
                        const file = e.target.files[0]
                        if(!file) return

                        // 파일 타입 체크
                        if(!file.type.trim().startsWith('image/')){
                            alert('이미지 파일만 업로드 가능합니다.')
                            commonContentFile.value = ''
                            return
                        }

                        // 파일 크기 체크(5MB 제한)
                        if(file.size > 5 * 1024 * 1024){
                            alert('5MB 이하의 파일만 업로드 가능합니다.')
                            commonContentFile.value = ''
                            return
                        }
                    })
                }
            }
        }

        // 3. 임시 저장 버튼
        const commonPassageSaveBtn = e.target.closest('#btnSaveCommonPassageModal')
        const controls = commonPassageModal.querySelector(".passage-controls")
        
        if(commonPassageSaveBtn) {
            // 1. 지문 유형 저장
            const activeBtn = controls.querySelector(".modal-btn-passage-type.active")
            if(!activeBtn){
                alert('지문 유형을 선택해주세요.')
                return
            }

            const type = activeBtn.dataset.type
            let content = null
            let passageData = { type: type }

            // 2. 지문 적용 범위 저장
            const rangeInput = commonPassageModal.querySelector("#common-passage-range").value.trim()
            if(!rangeInput || !rangeInput.includes("~")){
                alert('적용할 문항 범위를 올바르게 입력해주세요. (예: 1~3)')
                return
            }
            const rangeArray = parseQuestionRange(rangeInput)
            // if(rangeArray.length == 0){
            //     alert('적용할 문항 범위를 올바르게 입력해주세요. (예: 1~3)')
            //     return
            // }

            // 3. 지문 내용 저장
            if(type == 'text'){
                content = commonContentContainer.querySelector("textarea").value.trim()
                if(!content){
                    alert('내용을 입력해주세요.')
                    return
                }
            } else if(type == 'image'){
                const fileInput = commonContentContainer.querySelector("input[type='file']")
                if(fileInput.files.length == 0){
                    alert('이미지 파일을 선택해주세요.')
                    return
                } 

                // 파일 타입 체크
                if(!fileInput.files[0].type.trim().startsWith('image/')){
                    alert('이미지 파일만 업로드 가능합니다.')
                    fileInput.value = ''
                    return
                }

                // 파일 크기 체크(5MB 제한)
                if(fileInput.files[0].size > 5 * 1024 * 1024){
                    alert('5MB 이하의 파일만 업로드 가능합니다.')
                    fileInput.value = ''
                    return
                }

                // 파일키, 파일 저장
                passageData.fileKey = `question_${rangeInput}_common_image`
                passageData.file = commonContentContainer.querySelector("input[type='file']").files[0]
                content = fileInput.files[0].name
            }
            
            // 중복 체크 (내용, 범위 일치 여부로 확인)
            let existingPassage = passages.find(p => p.content == content && p.rangeText == rangeInput)
            if(existingPassage){
                alert("이미 등록된 지문입니다.")
                window.edit_common.renderPassageInput('modal', 'text', existingPassage.content)
                commonPassageModal.querySelector("#common-passage-range").value = existingPassage.rangeText
                return
            }

            // 4. 추출한 데이터를 객체에 저장
            passageData.content = content
            passageData.rangeText = rangeInput
            passageData.rangeArray = rangeArray
            passageData.id = commonPassageCounter++

            // 5. 임시 저장 버튼 클릭 시 공통 지문 id를 selectedPassageId 값에 저장
            commonPassageModal.setAttribute("data-selected-passage-id", passageData.id)
            commonPassageModal.setAttribute("data-selected-passage-type", passageData.type)

            // 6. 공통 지문 관리 배열에 passageData 객체 저장
            passages.push(passageData)
            renderCommonPassageList()
            showToastMessage('임시 저장되었습니다.')
        }

        // 4. 목록 보기 버튼
        const commonPassageShowListBtn = e.target.closest('#btnShowCommonPassageList')
        if(commonPassageShowListBtn){
            const listContainer = document.querySelector("#commonPassageListContainer")
            const isShowing = listContainer.classList.toggle('show')
            if(isShowing) renderCommonPassageList()
            commonPassageShowListBtn.innerHTML = isShowing  // true인 상태? 토글 후에 show 클래스가 존재하는 경우
                ? `<i class="fas fa-chevron-up"></i> 목록 닫기`
                : `<i class="fas fa-list-alt"></i> 등록된 공통 지문 보기`
        }

        // 5. 삭제 버튼
        const commonPassageDeleteListBtn = e.target.closest('.btn-delete-common-passage')
        if(commonPassageDeleteListBtn){
            const passageId = commonPassageDeleteListBtn.getAttribute("data-passage-id") 

            /**
             * @note 전역 변수 passages를 매개변수로 전달받아 사용할 경우:
             * .push()나 요소 수정처럼 '참조'를 통한 원본 객체 변경은 가능하지만,
             * filter() 결과를 다시 대입하는 '재할당'은 해당 함수 내의 지역 변수 값만 바꿀 뿐 전역 변수 자체를 교체하지 못함.
             * 따라서 전역 배열을 완전히 새 배열로 교체(삭제 처리)하기 위해 window.passages를 직접 참조하여 재할당함.
             */
            window.passages = passages.filter(passage => passage.id != passageId) // 삭제 버튼을 누르지 않은 passage만 모아서 배열 업데이트
            renderCommonPassageList()
        }

        // 6. 공통 지문 목록 적용 버튼
        const commonPassageApplyBtn = e.target.closest('.btn-apply-common-passage')

        if(commonPassageApplyBtn){
            const isConfirmed = window.confirm('해당 지문을 적용하시겠습니까?')
            if(isConfirmed){
                // 모달 데이터 속성 갱신
                const passageId = commonPassageApplyBtn.getAttribute('data-passage-id')
                const passageType = commonPassageApplyBtn.getAttribute('data-passage-type')
                commonPassageModal.setAttribute("data-selected-passage-id", passageId)
                commonPassageModal.setAttribute("data-selected-passage-type", passageType)

                // 에디터에 내용 렌더링
                const passageObj = passages.find(passage => passage.id == passageId) 
                window.edit_common.renderPassageInput('modal', passageType, passageObj.content)

                // 범위 입력창 값 set
                commonPassageModal.querySelector("#common-passage-range").value = passageObj.rangeText
            }
        }

        // 7. 작성 완료 버튼
        const commonPassageCompleteBtn = e.target.closest("#btnCompleteCommonPassage")
        
        if(commonPassageCompleteBtn){
            const selectedPassageId = commonPassageModal.getAttribute("data-selected-passage-id")
            const selectedPassageType = commonPassageModal.getAttribute("data-selected-passage-type")
            const activeType = controls.querySelector(".modal-btn-passage-type.active").getAttribute("data-type")
            
            // 1. 타입 불일치 체크
            if(selectedPassageType != activeType){
                alert('임시 저장 후 다시 이용해주세요.')
                return
            }
            
            // 2. 공통지문 ID 확인
            if(!selectedPassageId){
                alert('선택된 공통 지문이 없습니다. 임시 저장 후 다시 시도해주세요.')
                return
            }

            // 3. 지문 내용 유효성 검사
            let content = null
            const savePassage = passages.find(p => p.id == selectedPassageId)

            if(selectedPassageType == 'text'){
                content = commonPassageModal.querySelector("textarea.passage-text").value.trim()
                if(!content){
                    alert('지문 내용을 입력해주세요.')
                    return
                } else if(content != savePassage.content.trim()){
                    alert('지문 내용이 수정되었습니다. 임시 저장 후 다시 시도해주세요.')
                    return
                }
            } else if(selectedPassageType == 'image'){
                content = commonPassageModal.querySelector("input[type='file']").files // fileList 객체
                if(content.length == 0){
                    alert('이미지 지문 파일을 업로드해주세요.')
                    return
                }
            }

            // 4. 정상 처리
            const card = questionContainer.querySelector(`.question-item[data-question-num="${questionModalNum}"]`)
            card.setAttribute("data-selected-passage-id", selectedPassageId)
            closeCommonPassageModal()
            showToastMessage('공통 지문이 저장되었습니다.')
        }
    }
    
}

/* 시험지 컨테이너 핸들러
================================================== */
const QuestionContainerHandler = {
    /**
     * 수정 시 기존 공통 지문 데이터를 passages[]에 추가하고, .card에 추가함
     */
    setupCommonPassages(data, passages){
        if(data.common){
            const card = document.querySelector(`.question-item.card[data-question-num="${data.qNum}"]`)

            // passages[]에 추가
            const content = data.common
            const rangeText = data.passageScope
            const rangeArray = parseQuestionRange(rangeText)
            const passageId = commonPassageCounter++
            let type = 'text'
            if (content && (
                content.endsWith(".jpg") || 
                content.endsWith(".jpeg") || 
                content.endsWith(".png")
            )) {
                type = 'image'
            } 

            const passageData = {
                type: type,
                content: content,
                rangeText: rangeText,
                rangeArray: rangeArray,
                id: passageId
            }
            passages.push(passageData)
            // .card에 id추가
            card.setAttribute('data-selected-passage-id', passageId)

            // 공통지문모달 - 임시저장 리스트에 추가
            // window.edit_common.initSection('common', 'modal', content)
        }
    },

    handleQuestionContainerClick(e, passages) {
        // '문항 삭제' 버튼 리스너
        const removeBtn = e.target.closest('.btn-remove-question')
        if(removeBtn){
            const isConfirmed = confirm('해당 문항를 삭제하시겠습니까?')
            if(isConfirmed){
                const questionCard = removeBtn.closest('.question-item.card')
                removeQuestion(questionCard)
            }
            return
        }

        // '보기 추가' 버튼 리스너
        const addOptionBtn = e.target.closest('.btn-add-option')
        if(addOptionBtn){
            const questionItem = addOptionBtn.closest(".question-item")
            addOption(questionItem)
            return
        }

        // '보기 삭제' 버튼 리스너
        const removeOptionBtn = e.target.closest(".btn-remove-option")
        if(removeOptionBtn){
            // 등록 페이지인 경우, 선택지 삭제
            const isCreatePage = document.querySelector(".exam_create_page") ? true : false
            if(isCreatePage){
                const optionDiv = removeOptionBtn.closest(".option-inputs").querySelectorAll("[class^='option-item']") // 각각의 보기 div
                
                if(optionDiv.length > 2){
                    removeOption(removeOptionBtn)
                } else {
                    alert('보기는 최소 2개 이상이어야 합니다.')
                }
            } else {
                // 수정 페이지인 경우, 선택지 내용 비우기
                const optionDiv = removeOptionBtn.closest("[class^='option-item']")
                const targetInput = optionDiv.querySelector(".option-input")
                if(targetInput){
                    targetInput.value = ''
                    targetInput.focus()
                }
            }
            return
        }

        // 공통 지문 모달창 열기
        const toggle = e.target.closest('.common-passage-toggle') // 체크박스
        const card = e.target.closest('.question-item.card')
        const questionNum = card.getAttribute('data-question-num')
        const passageBtns = questionContainer.querySelectorAll(`.btn-passage-type[data-q-num="${questionNum}"]`)
        const passageContent = questionContainer.querySelector(`#passage-content-${questionNum}`)
        // 체크박스 클릭 시 '공통 지문 설정' 버튼 상태 변경
        if(toggle){
            const cardViewBtn = card.querySelector(`#commonPassageViewBtn-${questionNum}`)
            cardViewBtn.disabled = !(toggle.checked)
            return
        }

        // '공통 지문 설정' 버튼 클릭 시 모달창 열기
        const clickViewBtn = e.target.closest('.btn-common-passage-view')
        if(clickViewBtn){
            const card = clickViewBtn.closest('.question-item.card')
            const questionNum = card.dataset.questionNum

            if(!clickViewBtn.disabled){
                openCommonPassageModal(questionNum)
                const selectedPassageId = card.getAttribute("data-selected-passage-id")
                if(card && selectedPassageId){
                    // 기존에 설정된 공통 지문이 있는 경우
                    const savedPassage = passages.find(p => p.id == selectedPassageId)
                    if(savedPassage){
                        window.edit_common.renderPassageInput('modal', savedPassage.type, savedPassage.content)
                        const rangeInput = commonPassageModal.querySelector("#common-passage-range")
                        if(rangeInput){
                            rangeInput.value = savedPassage.rangeText
                        } 
                    } 
                } else {
                    /**
                     * @note 기존에 설정된 공통 지문이 없는 경우에는 Quill 에디터 렌더링을 하지 않음
                     * 사용자가 [지문 유형 버튼]을 클릭하기 전까지 "유형 선택 안내" UI를 유지하기 위함
                     * 실제 에디터 초기화는 [지문 유형 버튼] 클릭 시 발생하는 리스너에서 처리
                     * @see #CommonPassageHandler.handleModalClick
                     */
                }
            }
            return
        }

        /* 개별 지문 리스너
        ================================================== */
        const passageBtn = e.target.closest('.btn-passage-type')
        if(passageBtn) {
            const dataType = passageBtn.getAttribute("data-type")
            const contentContainer = document.querySelector(`#passage-content-${questionNum}`)
            const controls = passageBtn.closest(".passage-controls")
            // const previewDiv = document.querySelector(`#image-preview-${questionNum}`)
            
            // 현재 선택된 버튼 스타일 초기화 및 적용
            controls.querySelectorAll('.btn-passage-type').forEach((btn) => {
                resetButton(btn)
            })
            activeButton(passageBtn)

            // 내용 컨테이너 업데이트
            contentContainer.innerHTML = ''

            if(dataType == 'text'){
                // 텍스트 지문
                contentContainer.innerHTML = createPassageTextHtml(questionNum)
                window.edit_common.renderPassageInput(questionNum, 'text', '')
            } else if(dataType == 'image'){
                // 이미지 지문
                contentContainer.innerHTML = createPassageImageHtml(questionNum)
                // input에 파일 이름이 있으면
                const fileInput = contentContainer.querySelector('.passage-image-file')
                if(fileInput){
                    fileInput.addEventListener('change', (e) => {
                        const file = e.target.files[0]
                        if(!file) return

                        // 파일 타입 체크
                        if(!file.type.trim().startsWith('image/')){
                            alert('이미지 파일만 업로드 가능합니다.')
                            e.target.value = ''
                            return
                        }

                        // 파일 크기 체크(5MB 제한)
                        if(file.size > 5 * 1024 * 1024){
                            alert('5MB 이하의 파일만 업로드 가능합니다.')
                            e.target.value = ''
                            return
                        }
                    })
                } 
            } else {
                alert('일시적인 오류가 발생했습니다. 잠시후 다시 시도해주세요.')
            }
            return
        }
    }
}


/* 시험지 동작(문항 추가, 시험지 최종 등록) 핸들러
================================================== */
const ExamActionHandler = {
    init(){
        const btnAddQuestion = document.querySelector("#btnAddQuestion")
        if(btnAddQuestion){
            btnAddQuestion.addEventListener('click', addQuestion)
        }

        const btnSaveExam = document.querySelector("#btnSaveExam")
        if(btnSaveExam){
            btnSaveExam.addEventListener('click', saveExam)
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if(!document.querySelector(".exam_create_page")) return
    
    ExamInfoContainerHandler.init() // 시험 정보 설정 컨테이너
    ExamActionHandler.init()        // 문항 추가 및 시험지 최종 등록 컨테이너
    
    // 시험지 컨테이너 리스너
    questionContainer.addEventListener('click', (e) => {
        QuestionContainerHandler.handleQuestionContainerClick(e, window.passages)
    })

    // 공통 지문 모달창 리스너
    commonPassageModal.addEventListener('click', (e) => {
        const context = {
            commonPassageModal,
            renderCommonPassageList,
            createPassageTextHtml,
            createPassageImageHtml,
            closeCommonPassageModal,
            showToastMessage,
            questionContainer
        } 

        CommonPassageHandler.handleModalClick(e, context)
    })

    // 에디터 등록
    // window.edit_common.bindPassageEvents()
        
})


/* 문항 추가, 시험지 최종 등록
================================================== */

// 전역 변수
let questionCounter = 0 // 현재 문항 번호
const maxOptions = 5 // 선택지 개수 제한

// 문항 추가 시 UI
const createQuestionHml = (number) => {
    return `
        <div class="question-item card" data-question-num="${number}">
            <div class="question-header">
                <h4>제 ${number} 문항</h4>
                <button class="btn btn-sm btn-danger btn-remove-question" data-question-num="${number}">
                    <i class="fas fa-trash-alt"></i> 삭제
                </button>
            </div>
            
            <div class="question-body">
                <div class="form-group passage-group">

                    <div class="passage-controls common-passage-check-commonContentContainer">
                        <label class="form-check-label" for="common-passage-toggle-${number}">
                            공통 지문
                        </label>
                        <input class="form-check-input common-passage-toggle" type="checkbox" 
                            id="common-passage-toggle-${number}" data-q-num="${number}">
                        <button id="commonPassageViewBtn-${number}" class="btn-common-passage-view" disabled>
                            <i class="fas fa-search"></i> 공통 지문 설정
                        </button>
                    </div>

                    <div class="passage-controls passage-container" id="passage-controls-${number}">
                        <label class="form-check-label">
                            개별 지문
                        </label>
                        <button type="button" class="btn btn-sm btn-outline-secondary btn-passage-type" data-type="text" data-q-num="${number}">
                            <i class="fas fa-file-alt"></i> 텍스트 지문
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-secondary btn-passage-type" data-type="image" data-q-num="${number}">
                            <i class="fas fa-image"></i> 이미지 지문
                        </button>
                    </div>
                    
                    <div class="passage-content" id="passage-content-${number}">
                        <p class="placeholder-text">지문 유형을 선택해주세요.</p>
                    </div>
                </div>

                <div class="form-group question-group">
                    <label>문항 내용</label>
                    <textarea class="form-control no-resize question-text" rows="4" placeholder="문항 내용을 입력하세요."></textarea>
                </div>

                <div class="form-group options-group">
                    <label>선택지</label>
                    <div class="option-inputs">
                        <div class="option-item-1">
                            <input type="text" class="form-control option-input" data-choice-num="1" placeholder="보기 1">
                            <button type="button" class="btn btn-danger btn-sm btn-remove-option">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                        <div class="option-item-2">
                            <input type="text" class="form-control option-input" data-choice-num="2" placeholder="보기 2">
                            <button type="button" class="btn btn-danger btn-sm btn-remove-option">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-primary btn-add-option">
                        + 보기 추가
                    </button>
                </div>

                <div class="form-group answer-group">
                    <label for="answer-${number}">정답</label>
                    <input type="number" id="answer-${number}" class="form-control question-answer" min="1" max="5" placeholder="정답 번호 (예: 3)">
                </div>
            </div>
        </div>
    `
}

// 문항 추가 함수
const addQuestion = () => {
    questionCounter++
    const newQuestionHtml = createQuestionHml(questionCounter)

    questionContainer.insertAdjacentHTML('beforeend', newQuestionHtml)
}

// 문항 삭제 함수
const removeQuestion = (questionCard) => {
    questionCard.remove()

    const cards = document.querySelectorAll('.question-item.card')
    if(cards.length == 0) {
        questionCounter = 0
        return
    }

    cards.forEach((card, index) => {
        const qNum = index + 1

        card.setAttribute('data-question-num', qNum)
        card.querySelector("h4").textContent = `제 ${qNum} 문항`
        card.querySelector(".btn-remove-question").setAttribute('data-question-num', qNum)
        card.querySelectorAll(".btn-passage-type").forEach((btn) => {
            btn.setAttribute('data-q-num', qNum)
        })
        card.querySelector(".passage-content").id = `passage-content-${qNum}`
        card.querySelector(".answer-group > label").htmlFor = `answer-${qNum}`
        card.querySelector(".answer-group > input").id = `answer-${qNum}`

        questionCounter = cards.length
    })
}



// 공통지문 설정 모달창 열기
const openCommonPassageModal = (questionNum) => {
    commonPassageModal.style.display = 'flex'
    commonPassageModal.setAttribute('data-q-modal-num', questionNum)

    /**
     * 공통지문 값이 있던 문항인 경우 값 복원
     */

}

// 공통지문 설정 모달창 닫기
const closeCommonPassageModal = () => {
    /**
     * 값 초기화
     */
    // 1. 텍스트 지문 입력칸 + 파일 업로드 부분 없애고 p태그 보여주기
    const modalPassageInput = commonPassageModal.querySelector("#modal-passage-input")
    modalPassageInput.innerHTML = createPassagePlaceholderHtml()
    // 2. 버튼 스타일 초기화
    commonPassageModal.querySelectorAll('.modal-btn-passage-type').forEach((btn) => {
        resetButton(btn)
    })

    // 3. 목록 컨테이너 숨기기
    document.querySelector("#commonPassageListContainer").innerHTML = ''

    // 4. 공통지문 범위 초기화
    commonPassageModal.querySelector("#common-passage-range").value = ''

    // 5. 모달창 - 등록된 공통 지문 목록이 열려있는 경우 닫기
    commonPassageModal.querySelector("#commonPassageListContainer").classList.remove('show')
    commonPassageModal.querySelector("#btnShowCommonPassageList").innerHTML = `<i class="fas fa-list-alt"></i> 등록된 공통 지문 보기`
    
    // 모달창 숨김
    commonPassageModal.style.display = 'none'
}

// 텍스트 지문 UI
const createPassageTextHtml = (questionNum) => {
    // 안내 메시지 HTML 구성
    const tipHtml = 
    `
        <div class="passage-tip-box">
            <i class="fas fa-info-circle"></i>
            <p class="passage-tip-text">
                <strong>💡 작성 Tip:</strong> 이미지와 텍스트가 모두 포함된 지문은 
                아래 에디터의 <strong>이미지 삽입 버튼</strong>을 이용해 함께 작성할 수 있습니다.
            </p>
        </div>
    `
    return `
        <div class="editor-container" id="editor-wrapper-${questionNum}">
            ${tipHtml}
            <div id="editor-${questionNum}" class="quill-editor-box"></div>
            <div style="display:none;">
                <textarea class="form-control no-resize passage-text" rows="6" id="passage-text-${questionNum}" data-q-num="${questionNum}" maxlength="1000" placeholder="문항에 필요한 지문 내용을 입력하세요."></textarea>
            </div>
        </div>
        
    `
}

// 이미지 지문 UI
const createPassageImageHtml = (questionNum) => {
    return `
        <input type="file" class="form-control passage-image-file" accept=".jpg, .jpeg, .png" data-q-num="${questionNum}">
        <small class="form-text text-muted">최대 5MB 이하의 이미지 파일만 첨부 가능합니다.</small>
        <div class="image-preview" id="image-preview-${questionNum}"></div>
    `
}

// 지문 입력 영역을 초기화
const createPassagePlaceholderHtml = () => {
    return `
        <p class="placeholder-text">지문 유형을 선택해주세요.</p>
    `
}


// 버튼 초기화
const resetButton = (btn) => {
    btn.classList.remove('active', 'btn-secondary')
    btn.classList.add('btn-outline-secondary') // 비활성화 스타일
}

// 버튼 활성화
const activeButton = (btn) => {
    btn.classList.remove('btn-outline-secondary')
    btn.classList.add('active', 'btn-secondary')
}

// 공통 지문 적용 범위
const parseQuestionRange = (rangeInput) => {
    const [start, end] = rangeInput.split("~").map(num => parseInt(num))
    
    if(start - end > 0){
        alert('적용할 문항 범위를 올바르게 입력해주세요. (예: 1~3)')
        return 
    }

    const rangeArray = []
    for(let i = start; i <= end; i++){
        rangeArray.push(i)
    }

    rangeArray.sort()
    return rangeArray
}

// 등록된 공통 지문 목록 UI
const createCommonPassageListHtml = (passage) => {
    let contentPreview = ''
    if(passage.type == 'text'){
        // 1. HTML 태그 제거
        const plainText = passage.content.replace(/<[^>]*>?/gm, '')

        // 2. 텍스트가 있으면 길이를 자르고, 없는데 이미지만 있다면 안내 문구 표시
        if(plainText.trim().length > 0){
            // 텍스트만 있거나, 텍스트 + 이미지가 있거나
            const hasImage = passage.content.includes('<img')
            contentPreview = (hasImage ? '🖼️ ' : '') +
                            plainText.substring(0, 30) +
                            (plainText.length > 30 ? '...' : '')
        } else if(passage.content.includes('<img')){
            // 이미지만 있는 경우
            contentPreview = '🖼️ [이미지 포함 지문]'
        } else {
            // 아무 내용도 없는 경우
            contentPreview = '(내용 없음)'
        }
    } else if(passage.type == 'image'){
        contentPreview = '🖼️ [이미지 파일]'
    }
    return `
        <div class="common-passage-list-item" data-passage-id="${passage.id}" data-passage-type="${passage.type}">
            <div style="flex-grow: 1;">
                <span style="margin-left: 10px; font-weight: 600; color: #34495e;">
                    문항: ${passage.rangeText}
                </span>
                
                <p style="margin: 5px 0 0 10px; font-size: 0.9em; color: #555;">
                    지문 내용: ${contentPreview}
                </p>
            </div>

            <div class="list-actions" style="display: flex; gap: 5px; margin-left: 15px;">
                <button type="button" class="btn btn-sm btn-apply-common-passage" 
                    data-passage-id="${passage.id}" data-passage-type="${passage.type}">
                    <i class="fas fa-check"></i>
                </button>

                <button type="button" class="btn btn-sm btn-delete-common-passage" 
                    data-passage-id="${passage.id}">
                    <i class="far fa-trash-alt"></i>
                </button>
            </div>
        </div>
    `
}

// 등록된 공통 지문 목록 렌더링
const renderCommonPassageList = () => {
    const listContainer = document.querySelector("#commonPassageListContainer")
    listContainer.innerHTML = ''

    if(passages.length == 0){
        listContainer.innerHTML = `
            <p style="color: #999;">등록된 공통 지문이 없습니다.</p>
        `
        return
    }

    passages.forEach((passage) => {
        listContainer.insertAdjacentHTML('afterbegin', createCommonPassageListHtml(passage))
    })
}

// 보기 추가 함수
const addOption = (questionItem) => {
    const optionsDiv = questionItem.querySelector(".option-inputs")
    const currentCount = optionsDiv.querySelectorAll(".option-input").length // input 태그 개수
    
    let nextCount = currentCount + 1
    if(currentCount >= maxOptions) {
        alert(`선택지는 최대 ${maxOptions}개까지만 추가할 수 있습니다.`)
        return
    } else {
        let output = ''
        output += 
        `
            <div class="option-item-${nextCount}">
                <input type="text" class="form-control option-input" data-choice-num="${nextCount}" placeholder="보기 ${nextCount}">
                <button type="button" class="btn btn-danger btn-sm btn-remove-option">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `
    
        optionsDiv.insertAdjacentHTML('beforeend', output)
    }
}

// 보기 삭제 함수
const removeOption = (removeOptionBtn) => {
    const optionDiv = removeOptionBtn.closest('[class^="option-item"]')
    if(!optionDiv) return
    
    const optionscommonContentContainer = removeOptionBtn.closest('.option-inputs')
    optionDiv.remove()

    // 남은 보기 번호 재정렬
    const options = optionscommonContentContainer.querySelectorAll('[class^="option-item"]')
    options.forEach((option, index) => {
        option.className = `option-item-${index + 1}`
        option.querySelector('.option-input').placeholder = `보기 ${index + 1}`
    })
}

// 토스트 메시지
const showToastMessage = (message) => {
    const toastContainer = document.querySelector("#toastMessage")
    toastContainer.textContent = message

    toastContainer.classList.add('show')

    setTimeout(() => {
        toastContainer.classList.remove('show')
    }, 3000)
}

// 시험지 최종 등록
const saveExam = () => {
    const formData = new FormData()
    const cards = questionContainer.querySelectorAll('.question-item.card')
    if(cards.length == 0){
        alert('시험지 문항을 작성하신 후 시도해주세요.')
        return
    } 

    const selectedType = examinfoContainer.querySelector("#examType").value
    const selectedRound = examinfoContainer.querySelector("#examRound").value
    const selectedSubject = examinfoContainer.querySelector("#examSubject").value
    if(!selectedType || !selectedRound || !selectedSubject){
        alert('시험지 정보를 작성해주세요.')
        return
    }

    // 시험 정보
    const examInfo = {
        type: selectedType,
        round: selectedRound,
        subject: selectedSubject,
        folderId: folderId
    }

    // 공통 지문에 파일이 있는 경우 담기
    passages.forEach(p => {
        if(p.type == 'image' && p.file){
            formData.append(p.fileKey, p.file)
        }
    })

    // 시험 문제들
    const examData = []
    for(const card of cards){
        const questionNum = card.getAttribute("data-question-num")
        const questionObj = {}

        // 1. 문항 번호
        questionObj.questionNum = questionNum 

        // 2. 공통 지문 처리
        const commonPassageCheckInput = card.querySelector(".common-passage-toggle")
        if(commonPassageCheckInput.checked){
            const commonPassageId = card.getAttribute("data-selected-passage-id")
            if(!commonPassageId){
                alert(`${questionNum}번 문항의 공통 지문이 등록되지 않았습니다.`)
                return
            } 
            questionObj.useCommonPassage = 'Y'
            const originalPassage = passages.find(p => p.id == commonPassageId)

            // 서버 DTO 구조에 맞추기 위해 공통 지문 객체에서 파일(file) 속성 제외
            const restPassage = Object.assign({}, originalPassage) // 객체 복사
			delete restPassage.file // 복사본에서 file 속성만 삭제
            questionObj.commonPassage = restPassage
        } else {
            questionObj.useCommonPassage = 'N'
            questionObj.commonPassage = null
        }

        // 3. 개별 지문 처리
        const controls = card.querySelector(".passage-controls.passage-container")
        const activePassageBtn = controls.querySelector(".btn-passage-type.active")

        if(activePassageBtn){
            questionObj.useIndividualPassage = 'Y'
            const dataType = activePassageBtn.getAttribute("data-type")
            const contentContainer = card.querySelector(".passage-content")
            let individualPassage = {}

            if(dataType == 'text'){
                const passageTextarea = contentContainer.querySelector("textarea.passage-text")
                const textareaValue = passageTextarea ? passageTextarea.value.trim() : ''
                if(!textareaValue){
                    alert(`${questionNum}번 문항의 개별 지문을 작성해주세요.`)
                    return
                }
                individualPassage = {
                    type: 'text',
                    content: textareaValue
                }
            } else if(dataType == 'image'){
                const fileInput = contentContainer.querySelector("input[type='file']")
                if(!fileInput || fileInput.files.length == 0){
                    alert(`${questionNum}번 문항의 이미지를 등록해주세요.`)
                    return
                }

                individualPassage = {
                    type: 'image',
                    content: fileInput.files[0].name,
                    fileKey: `question_${questionNum}_individual_image`
                }
                formData.append(`question_${questionNum}_individual_image`, fileInput.files[0])
            }

            questionObj.individualPassage = individualPassage
        } else {
            questionObj.useIndividualPassage = 'N'
            questionObj.individualPassage = null
        }

        // 4. 문항 내용
        const questionTextarea = card.querySelector("textarea.question-text")
        const questionText = questionTextarea ? questionTextarea.value.trim() : ''
        if(!questionText){
            alert(`${questionNum}번 문항 내용을 작성해주세요.`)
            return
        }
        questionObj.questionText = questionText

        // 5. 선택지
        let choices = []
        const optionInputs = card.querySelectorAll("input.option-input")
        const choiceLabels = ['①', '②', '③', '④', '⑤']

        for(const input of optionInputs){
            const choiceContent = input ? input.value.trim() : ''
            if(!choiceContent){
                alert(`${questionNum}번 문항의 선택지를 입력해주세요.`)
                return
            }

            const choiceNum = parseInt(input.getAttribute("data-choice-num"), 10)

            choices.push({
                choiceNum: choiceNum,
                choiceText: choiceContent,
                choiceLabel: choiceLabels[choiceNum - 1]
            })
        }
        questionObj.questionChoices = choices

        // 6. 정답
        const answerInput = card.querySelector(`input#answer-${questionNum}`)
        const answerText = answerInput ? answerInput.value.trim() : ''
        if(!answerText){
            alert(`${questionNum}번 문항의 정답을 입력해주세요.`)
            return
        }
        questionObj.answerText = answerText

        examData.push(questionObj)
    }

    // 데이터 전송
    formData.append("examInfo", JSON.stringify(examInfo))
    formData.append("questions", JSON.stringify(examData))
    
    axios.post('/exam/saveExamByForm', formData)
        .then(response => {
            if(response.data){
                alert('시험지가 성공적으로 등록되었습니다.')
            } else {
                alert('시험지 등록에 실패했습니다. 잠시 후 다시 시도해주세요.')
            }
            location.href = `/admin/main`
        })
        .catch(error => {
            console.error('error: ', error)
        })
    
}


window.common = {
    // 개별지문 관련
    resetButton,
    activeButton,
    createPassageTextHtml,
    createPassageImageHtml,

    // 공통지문 관련
    openCommonPassageModal,
    closeCommonPassageModal,
    renderCommonPassageList,
    showToastMessage,
    handleModalClick: CommonPassageHandler.handleModalClick,
    handleQuestionContainerClick: QuestionContainerHandler.handleQuestionContainerClick.bind(QuestionContainerHandler),
    setupCommonPassages: QuestionContainerHandler.setupCommonPassages
}