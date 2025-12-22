// 전역 변수
const examinfoContainer = document.querySelector(".exam-info-controls")
const questionContainer = document.querySelector("#question-list-container")
const commonPassageModal = document.querySelector("#commonPassageModal")
const urlParam = new URLSearchParams(window.location.search)
const folderId = urlParam.get("folderId")
let commonPassageCounter = 1
let passages = [] // 공통 지문 데이터를 저장할 배열

document.addEventListener('DOMContentLoaded', () => {

    /* 시험지 정보 컨테이너 리스너
    ================================================== */
    examinfoContainer.addEventListener('click', (e) => {
        const examTypeSelect = e.target.closest('#examType')
        if(examTypeSelect){
            examTypeSelect.addEventListener('change', () => {
                const examTypeSelectedValue = examTypeSelect.value
                if(!examTypeSelectedValue) return
    
                const params = {
                    examTypeCode: examTypeSelectedValue
                }
                
                axios.get('/exam/getSubjectsForExamType', { params })
                    .then(response => {
                        updateExamSubjects(response.data)
                    })
                    .catch(error => {
                        console.error('error: ', error)
                    })
            })
            return
        }

    })


    /* '문항 추가' 버튼 클릭 리스너
    ================================================== */
    document.querySelector("#btnAddQuestion").addEventListener('click', addQuestion)

    /* '시험지 최종 등록' 버튼 클릭 리스너
    ================================================== */
    document.querySelector("#btnSaveExam").addEventListener('click', saveExam)

    /* 시험지 컨테이너 리스너
    ================================================== */
    questionContainer.addEventListener('click', (e) => {
        // '문항 삭제' 버튼 리스너
        const removeQuestionBtn = e.target.closest('.btn-remove-question')
        if(removeQuestionBtn){
            const questionCard = removeQuestionBtn.closest('.question-item.card')
            removeQuestion(questionCard)
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
            const optionDiv = removeOptionBtn.closest(".option-inputs").querySelectorAll("[class^='option-item']") // 각각의 보기 div
            
            if(optionDiv.length > 2){
                removeOption(removeOptionBtn)
            } else {
                alert('보기는 최소 2개 이상이어야 합니다.')
            }
            return
        }

        // 공통 지문 모달창 열기
        const commonPassageOpenBtn = e.target.closest('.common-passage-toggle')
        const questionNum = e.target.closest('.question-item.card').getAttribute('data-question-num')
        const passageBtns = questionContainer.querySelectorAll(`.btn-passage-type[data-q-num="${questionNum}"]`)
        const passageContent = questionContainer.querySelector(`#passage-content-${questionNum}`)

        if(commonPassageOpenBtn){
            // 공통지문 버튼 클릭 시
            if(commonPassageOpenBtn.checked){
                openCommonPassageModal(questionNum)
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
            const previewDiv = document.querySelector(`#image-preview-${questionNum}`)
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
        }
    })

    /* 공통 지문 모달창 리스너
    ================================================== */
    commonPassageModal.addEventListener('click', (e) => {
        // 공통 지문 모달창 닫기
        const closeBtn = e.target.closest('.close-button')
        if(closeBtn){
            closeCommonPassageModal()
            return
        }

        // 공통 지문 유형 버튼
        const commonPassageTypeBtn = e.target.closest('.modal-btn-passage-type')
        const commonContentContainer = commonPassageModal.querySelector('#modal-passage-input')
        const questionModalNum = commonPassageModal.getAttribute("data-q-modal-num")
        if(commonPassageTypeBtn){
            const dataType = commonPassageTypeBtn.getAttribute("data-type")
            const commonControls = commonPassageTypeBtn.closest(".passage-controls")
            commonControls.querySelectorAll('.modal-btn-passage-type').forEach((btn) => {
                resetButton(btn)
            })
            activeButton(commonPassageTypeBtn)

            if(dataType == 'text'){
                commonContentContainer.innerHTML = createPassageTextHtml(questionModalNum)
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

        // 공통 지문 임시 저장 버튼
        const commonPassageSaveBtn = e.target.closest('#btnSaveCommonPassageModal')
        const controls = commonPassageModal.querySelector(".passage-controls")
        let passageData = {}
        let content = null
        if(commonPassageSaveBtn) {
            // 1. 지문 유형 저장
            const activeBtn = controls.querySelector(".modal-btn-passage-type.active")
            if(!activeBtn){
                alert('지문 유형을 선택해주세요.')
                return
            }
            const type = activeBtn.dataset.type
            passageData.type = type

            // 2. 지문 내용 저장
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
                content = fileInput.files[0].name
            }
            passageData.content = content
            
            // 3. 지문 적용 범위 저장
            const rangeInput = commonPassageModal.querySelector("#common-passage-range").value.trim()
            if(!rangeInput || !rangeInput.includes("~")){
                alert('적용할 문항 범위를 올바르게 입력해주세요. (예: 1~3)')
                return
            }
            const rangeArray = parseQuestionRange(rangeInput)
            if(rangeArray.length == 0){
                alert('적용할 문항 범위를 올바르게 입력해주세요. (예: 1~3)')
                return
            }
            passageData.rangeText = rangeInput
            passageData.rangeArray = rangeArray

            // 4. 공통 지문 id 저장
            passageData.id = commonPassageCounter++

            // 5. 임시 저장 버튼 클릭 시 공통 지문 id를 selectedPassageId 값에 저장
            commonPassageModal.setAttribute("data-selected-passage-id", passageData.id)
            commonPassageModal.setAttribute("data-selected-passage-type", passageData.type)

            // 6. 공통 지문 관리 배열에 passageData 객체 저장
            passages.push(passageData)
            renderCommonPassageList()
            showToastMessage('공통 지문이 등록되었습니다.')
        }

        // 공통 지문 목록 보기 버튼 리스너
        const commonPassageShowListBtn = e.target.closest('#btnShowCommonPassageList')
        if(commonPassageShowListBtn){
            renderCommonPassageList()
            const listContainer = document.querySelector("#commonPassageListContainer")

            if(listContainer.classList.contains("show")){
                listContainer.classList.remove('show')
                commonPassageShowListBtn.innerHTML = `<i class="fas fa-list-alt"></i> 등록된 공통 지문 보기`
            } else {
                listContainer.classList.add('show')
                commonPassageShowListBtn.innerHTML = `<i class="fas fa-chevron-up"></i> 목록 닫기`
            }
            return
        }

        // 공통 지문 목록 삭제 버튼 리스너
        const commonPassageDeleteListBtn = e.target.closest('.btn-delete-common-passage')
        if(commonPassageDeleteListBtn){
            const passageId = commonPassageDeleteListBtn.getAttribute("data-passage-id")
            passages = passages.filter(passage => passage.id != passageId) // 삭제 버튼을 누르지 않은 passage만 모아서 배열 업데이트
            renderCommonPassageList()
        }

        // 공통 지문 목록 적용 버튼 리스너
        const commonPassageApplyBtn = e.target.closest('.btn-apply-common-passage')
        if(commonPassageApplyBtn){
            const passageId = commonPassageApplyBtn.getAttribute('data-passage-id')
            const passageType = commonPassageApplyBtn.getAttribute('data-passage-type')
            // 임시 저장 목록에서 적용 버튼 클릭 시 selectedPassageId 값 갱신
            commonPassageModal.setAttribute("data-selected-passage-id", passageId)
            commonPassageModal.setAttribute("data-selected-passage-type", passageType)

            const passageObj = passages.find(passage => passage.id == passageId)
            let activeBtn = null
            controls.querySelectorAll('.modal-btn-passage-type').forEach((btn) => {
                resetButton(btn)
            })
            let output = ''
            if(passageObj.type == 'text'){
                activeBtn = controls.querySelector(".modal-btn-passage-type[data-type='text']")
                output = 
                `
                    <textarea class="form-control no-resize passage-text" rows="6" data-q-num="${questionModalNum}" maxlength="1000">${passageObj.content}</textarea>
                `
            } else if(passageObj.type == 'image'){
                activeBtn = controls.querySelector(".modal-btn-passage-type[data-type='image']")
                output =
                `
                    <input type="file" class="form-control passage-image-file" accept=".jpg, .jpeg, .png" data-q-num="${questionModalNum}">
                    <small class="form-text text-muted" style="color: #0056b3;">⚠️ ${passageObj.content} 파일을 다시 업로드해주세요.</small>
                    <div class="image-preview" id="image-preview-${questionModalNum}"></div>
                `
            }
            commonContentContainer.innerHTML = output.trim()
            activeButton(activeBtn)
            commonPassageModal.querySelector("#common-passage-range").value = passageObj.rangeText
        }

        // 공통 지문 작성 완료 버튼 리스너
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
    })
})

/* 시험 과목 UI 초기화
================================================== */
const updateExamSubjects = (examSubjects) => {
    const examSubject = document.querySelector("#examSubject")

    let options = '<option value="" selected disabled>과목 선택</option>'
    examSubjects.forEach((subject) => {
        options += `<option value="${subject}">${subject}</option>`
    })

    examSubject.innerHTML = options
}

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
                    </div>

                    <div class="passage-controls passage-container">
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

                <div class="form-group">
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
    return `
        <textarea class="form-control no-resize passage-text" rows="6" data-q-num="${questionNum}" maxlength="1000" placeholder="문항에 필요한 지문 내용을 입력하세요."></textarea>
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
    const contentPreview = 
        passage.type == 'text'
        ? passage.content.substring(0, 30) + (passage.content.length > 30 ? '...' : '')
        : `[${passage.content} - 이미지 지문]` 
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
    console.log(selectedSubject)

    // 시험 정보
    const examInfo = {
        type: selectedType,
        round: selectedRound,
        subject: selectedSubject,
        folderId: folderId
    }

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
            questionObj.commonPassage = passages.find(p => p.id == commonPassageId)
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
                    content: fileInput.files[0].name
                }
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
            alert(`${questionNum}번 문제를 작성해주세요.`)
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
    const data = {
        examInfo: examInfo,
        questions: examData
    }
    
    // POST 요청 시 JSON body 형태로 데이터를 전송
    // 컨트롤러에서는 @RequestBody DTO, Map 으로 받음
    // @RequestParam 으로 받으려면 URL 쿼리파라미터로 전달해야 함(보통은 비추천, URL 노출)
    axios.post('/exam/saveExamByForm', data)
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

/**
 * 추후 확장 계획
 * 1. 공통 지문 체크 표시된 상태에서 모달창 열기 -> 작성된 내용 복원시키기
 * 2. 개별 지문 비활성화 버튼 추가(active 시켰다가 비활성화 시키고 싶을 수도 있기때문에)
 */