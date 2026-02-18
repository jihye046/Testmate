// 전역 변수
window.passages = []
const commonPassageModal_edit = document.querySelector("#commonPassageModal")
const questionContainer_edit = document.querySelector("#question-list-container")


// Quill 에디터 초기화
const ExamEditor = {
    editors : {},

    dataCache: {
        qNum: null,
        individual: '',
        common: '',
        passageScope: '',
        examType: '',
        examRound: '',
        examSubject: ''
    },

    init(){
        const rawElement = document.querySelector("#examPageDtoJson")
        if(!rawElement) return

        try {
            const examPageDtoJson = JSON.parse(rawElement.dataset.examPageDtoJson)
            const q = examPageDtoJson.examQuestion

            this.dataCache.qNum = q.questionNum
            this.dataCache.individual = q.individualPassage
            this.dataCache.common = q.commonPassage
            this.dataCache.examType = examPageDtoJson.examType
            this.dataCache.examRound = examPageDtoJson.examRound
            this.dataCache.examSubject = examPageDtoJson.examSubject
            if(q.commonPassage){
                this.dataCache.passageScope = q.passageScope
            }

            // 1. 개별 지문 초기 렌더링
            this.initSection('individual', this.dataCache.qNum, this.dataCache.individual)

            // 2. 공통 지문 초기 렌더링
            this.initSection('common', 'modal', this.dataCache.common)

            // 3. 이벤트 바인딩(데이터 유무와 상관없이 항상 실행)
            // this.bindPassageEvents(this.dataCache.individual, this.dataCache.common)
        } catch (error) {
            console.error("데이터 파싱 중 오류 발생: ", error)
        }

    },

    getData(){
        return this.dataCache
    },

    getEditorContent(qNum){
        const quill = ExamEditor.editors[qNum]
        if(quill){
            return quill.root.innerHTML
        }
        return
    },

    initSection(category, id, passageData){
        if(!passageData || passageData.trim() == '') return

        let initialType = 'text'
        if (passageData && (
            passageData.endsWith(".jpg") || 
            passageData.endsWith(".jpeg") || 
            passageData.endsWith(".png")
        )) {
            initialType = 'image'
        } 
        this.renderPassageInput(id, initialType, passageData)
    },

    // 지문 내 '텍스트 지문/이미지 지문' 버튼 클릭 리스너 (등록 페이지에서는 인자 없이 호출됨)
    bindPassageEvents(individualPassage = '', commonPassage = ''){
        // 개별 지문 내 유형 선택
        const questionContainer = document.querySelector("#question-list-container")
        questionContainer.addEventListener('click', (e) => {
            const btn = e.target.closest('.btn-passage-type')
            if(btn){
                const type = btn.dataset.type
                const qNum = btn.dataset.qNum
                this.renderPassageInput(qNum, type, individualPassage)
            }
        })


        // 모달(공통지문) 내 유형 선택
        const modalContainer = document.querySelector("#commonPassageModal")
        modalContainer.addEventListener('click', (e) => {
            const btn = e.target.closest('.modal-btn-passage-type')
            if(btn){
                const type = btn.dataset.type
                // 등록페이지면 commonPassage는 빈값임
                this.renderPassageInput('modal', type, commonPassage)
            }
        })
    },

    /**
     * 지문 입력 UI 렌더링
     * @param {string*} qNum - 문항 번호 또는 'modal'
     * @param {string} type - 'text' 또는 'image'
     * @param {string} initialData - 초기 채워넣을 지문 데이터
     */
    renderPassageInput(qNum, type, initialData = ''){
        // 버튼 컨테이너
        const btnContainer = (qNum == 'modal')
            ? document.querySelector('#modal-passage-controls') // 공통
            : document.querySelector(`#passage-controls-${qNum}`) // 개별
        // 지문 컨테이너
        const passageContainer = (qNum == 'modal')
            ? document.querySelector('#modal-passage-input') // 공통
            : document.querySelector(`#passage-content-${qNum}`) // 개별
        // ⭐ modal이고 기존 editor가 있으면 먼저 정리
        if (qNum === 'modal' && this.editors[qNum]) {
            this.destroyEditor(qNum)
        }

        // 버튼
        const textBtn = btnContainer.querySelector('button[data-type="text"]')
        const imageBtn = btnContainer.querySelector('button[data-type="image"]')
        
        passageContainer.innerHTML = '' // 초기화

        if(type == 'text'){
            window.common.activeButton(textBtn)
            window.common.resetButton(imageBtn)
            
            passageContainer.innerHTML = window.common.createPassageTextHtml(qNum)

            // 기존 등록된 이미지 파일명이 텍스트 에디터에 노출되는 것을 방지하기 위해 확장자 체크 후 초기화
            const isImageFile = (initialData.match(/\.(jpg|jpeg|png|gif)$/i))
            const cleanData = isImageFile ? '' : initialData

            setTimeout(() => {
                this.initQuillEditor(qNum, cleanData)
            }, 100)
        } else if(type == 'image'){
            window.common.activeButton(imageBtn)
            window.common.resetButton(textBtn)
            
            const d = this.getData()
            const examType = this.dataCache.examType
            const examRound = d.examRound
            const examSubject = d.examSubject
            // passageContainer.innerHTML = window.common.createPassageImageHtml(qNum)        
            passageContainer.innerHTML = 
            `
                <input type="file" class="form-control passage-image-file" accept=".jpg, .jpeg, .png" data-q-num="${qNum}">
                <small class="form-text text-muted">최대 5MB 이하의 이미지 파일만 첨부 가능합니다.</small>
                <div class="image-preview show" id="image-preview-${qNum}">
                    <p class="text-muted small mb-1">현재 등록된 지문 이미지:</p>
                    <img src="/exam/getExamImagePath?filename=${initialData}&examType=${examType}&examRound=${examRound}&examSubject=${examSubject}">
                    <p class="text-primary small">※ 파일을 새로 선택하지 않으면 위 이미지가 유지됩니다.</p>
                </div>
            ` 

            if(this.editors[qNum]){
                delete this.editors[qNum]
            }
        }
    },

    destroyEditor(qNum){
        const quill = this.editors[qNum]
        console.log('quill: ',quill)
        if (!quill) return

        // Quill 이벤트 제거
        quill.off('text-change')

        // DOM 정리
        const container = document.querySelector(`#editor-${qNum}`)
        if (container) {
            container.innerHTML = ''
        }

        delete this.editors[qNum]
    },

    // Quill 에디터 초기화
    initQuillEditor(qNum, initialData){
        // Quill 내부의 디버그 메시지 출력을 차단
        const originalLog = console.log
        console.log = function(...args){
            // 로그 내용에 'Resize', 'DisplaySize' 등이 포함되어 있으면 출력하지 않음
            if (args[0] && typeof args[0] === 'string' && args[0].includes('this.options.modules')) {
                return
            }
            if (args[0] && args[0] === 0) return 

            //originalLog.apply(console, args)
        }

        // Quill에 이미지 리사이즈 모듈 등록 
        if (!Quill.import('modules/imageResize')) {
            Quill.register('modules/imageResize', ImageResize.default)
        }

        const editorId = `#editor-${qNum}`
        const container = document.querySelector(editorId)
        // 1. 컨테이너 존재 확인 (에러 방지 핵심)
        if (!container) {
            console.warn(`${editorId} 요소를 찾을 수 없어 에디터 초기화를 중단합니다.`);
            return;
        }

        // 2. 이미 초기화된 경우 중복 생성 방지
        if (container.classList.contains('ql-container')) {
            return;
        }

        try {
            const quill = new Quill(editorId, {
                theme: 'snow',
                modules: {
                    toolbar: {
                        container: [
                            ['bold', 'italic', 'underline', 'strike'],
                            [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                            ['link', 'image'],
                            ['clean']
                        ],
                        handlers: {
                            image: () => {
                                // 이미지를 업로드했을 때 실행될 핸들러
                                this.imageHandler(qNum)
                            }
                        }
                    },
                    imageResize: {
                        displaySize: true, // 이미지 크기 정보(px)를 보여줄지 여부
                        modules: [ 'Resize', 'DisplaySize', 'Toolbar' ] // 크기조절, 크기표시, 정렬 툴바
                    }
                },
                placeholder: '지문 내용을 입력해 주세요.'
            })
    
            // 로그 복구
            // console.log = originalLog
    
            this.editors[qNum] = quill
    
            // 초기 데이터가 있으면 에디터와 hidden textarea에 주입
            if(initialData){ 
                quill.root.innerHTML = initialData
                const hiddenTextarea = document.querySelector(`#passage-text-${qNum}`)
                if(hiddenTextarea) {hiddenTextarea.value = initialData} else {alert('textarea 못찾음')}
            }
    
            // 내용 변경 시 hidden textarea 동기화
            quill.on('text-change', () => {
                const html = quill.root.innerHTML
                const hiddenTextarea = document.querySelector(`#passage-text-${qNum}`)
                if(hiddenTextarea){
                    hiddenTextarea.value = html 
                }
            })  
        } catch (error) {
            console.error("Quill 초기화 중 치명적 오류 발생:", error)
        }
    },

    // Quill 에디터는 이미지 업로드 시 이미지를 서버로 전송하지 않고 브라우저 내에 텍스트로 변환하여 저장함
    // 이를 서버에 저장할 수 있도록 이미지를 업로드하는 시점에 즉시 서버로 업로드하고,
    // 서버가 반환한 이미지 경로(URL)을 에디터에 삽입
    imageHandler(qNum){
        const input = document.createElement('input')
        input.setAttribute('type', 'file')
        input.setAttribute('accept', 'image/*')
        input.click() // 커스텀 핸들러 등록 시 Quill의 파일창 열기 기능이 해제되므로, 파일창 열기를 수동으로 트리거

        // 사용자가 파일 열기를 하는 시점
        input.onchange = () => {
            const file = input.files[0]
            if(!file) return

            const formData = new FormData()
            formData.append('image', file)

            axios.post('/exam/uploadEditorImage', formData)
                .then(response => {
                    const result = response.data
                    if (result.fileName) {
                        // 서버가 반환한 파일명으로 이미지 경로(URL) 생성 및 에디터 삽입
                        const imageUrl = `/exam/getExamImagePath?filename=${result.fileName}`
                        const quill = this.editors[qNum]
                        const range = quill.getSelection() // 커서 위치
                        const index = range ? range.index : quill.getLength() // 커서가 없으면 맨 끝에 삽입

                        quill.insertEmbed(index, 'image', imageUrl) // 커서 위치가 확인되면 이미지를 그 자리에 삽입
                        quill.setSelection(index + 1) // 이미지를 넣은 후 커서를 이미지 바로 다음 칸으로 이동시킴
                    }
                })
                .catch(error => {
                    console.error('error: ', error)
                    alert('이미지 업로드에 실패했습니다.')
                })
        }
    }

}

const QuestionEditHandler = {
    examInfo: { 
        examId: null, type: '', typeKor: '', round: '', subject: '', folderId: null
    },

    questionObj: {
        questionId: null,
        questionNum: '',
        questionText: '',
        questionChoices: [],
        questionAnswer: { 
            answerId: null, 
            correctAnswer: '' 
        },
        useCommonPassage: 'N',
        commonPassage: { 
            type: '', fileKey: '', content: '', rangeText: '', rangeArray: [], id: null 
        },
        useIndividualPassage: 'N',
        individualPassage: { type: '', content: '', fileKey: '' },
    },

    init(){
        this._parseExamData()
    },

    getExamInfo(){ return this.examInfo },

    getExamData(){ return this.examData },

    handleEditClick(data){
        
        const qNum = data.qNum
        const card = document.querySelector(`.question-item.card[data-question-num="${qNum}"]`)

        if(qNum && card){
            const formData = new FormData()

            // 새로 입력받은 값들로 examData{} 업데이트
            if(this._collectData(card, qNum, formData)){
                formData.append('examInfo', JSON.stringify(this.examInfo))
                formData.append('question', JSON.stringify(this.questionObj))

                axios.post('/exam/updateExamByForm', formData)
                    .then(response => {
                        alert('수정이 완료되었습니다.')
                        location.href = response.data
                    })
                    .catch(error => {
                        console.error(error)
                        alert('수정 중 오류가 발생했습니다.\n메인페이지로 이동합니다.')
                        window.location.href = '/'
                    })
            }
        }
    },

    // 서버 데이터를 초기 객체에 바인딩
    _parseExamData(){
        const rawElement = document.querySelector("#examPageDtoJson")
        if(!rawElement) return
        
        try {
            const p = JSON.parse(rawElement.dataset.examPageDtoJson)
            const q = p.examQuestion
            const choices = p.examChoices
            
            // 1. 시험 정보 세팅
            this.examInfo = {
                examId: q.examId,
                type: p.examTypeEng,
                typeKor: p.examType,
                round: p.examRound,
                subject: p.examSubject,
                folderId: p.folderId
            }
            
            // 2. 문항 데이터 세팅
            this.questionObj.questionId = q.questionId
            this.questionObj.questionNum = q.questionNum
            this.questionObj.questionText = q.questionText

            // 공통 지문
            if(q.commonPassage){
                this.questionObj.useCommonPassage = 'Y'
                this.questionObj.commonPassage.content = q.commonPassage
                this.questionObj.commonPassage.rangeText = q.passageScope

                const isImage = (q.commonPassage.match(/\.(jpg|jpeg|png|gif)$/i))
                this.questionObj.commonPassage.type = isImage ? 'image' : 'text'
            }

            // 개별 지문
            if(q.individualPassage){
                this.questionObj.useIndividualPassage = 'Y'
                this.questionObj.individualPassage.content = q.individualPassage
                
                const isImage = (q.individualPassage.match(/\.(jpg|jpeg|png|gif)$/i))
                this.questionObj.individualPassage.type = isImage ? 'image' : 'text'
            }
            
            // 선택지
            this.questionObj.questionChoices = choices.map(c => ({
                choiceId: c.choiceId,
                choiceText: c.choiceText,
                // choiceNum: c.choiceNum,
                // choiceLabel: c.choiceLabel
            }))

            // 정답
            if(p.examAnswer){
                this.questionObj.questionAnswer = {
                    answerId: p.examAnswer.answerId,
                    correctAnswer: parseInt(p.examAnswer.correctAnswer)
                }
            }
        } catch (error) {
            console.error("데이터 파싱 중 오류 발생: ", error)
        }
    },

    // 수정한 데이터들 가져와서 questionObj에 set
    _collectData(card, qNum, formData){
        const passageGroup = card.querySelector(".passage-group")
        const questionGroup = card.querySelector(".question-group")
        const optionsGroup = card.querySelector(".options-group")
        const answerGroup = card.querySelector(".answer-group") 
        const selectedPassageId = card.dataset.selectedPassageId

        if(!this._collectPassages(passageGroup, qNum, selectedPassageId, formData)) return false
        if(!this._collectQuestion(questionGroup)) return false
        if(!this._collectChoices(optionsGroup)) return false
        if(!this._collectAnswer(answerGroup)) return false

        return true
    },

    _collectPassages(passageGroup, qNum, selectedPassageId, formData){
        // 개별 지문
        const individualActiveBtn = passageGroup.querySelector(`#passage-controls-${qNum} .btn-passage-type.active`)
        if(individualActiveBtn){
            const type = individualActiveBtn.dataset.type
            if(type == 'text'){
                const textarea = passageGroup.querySelector(`textarea[id="passage-text-${qNum}"]`)
                const textareaValue = textarea ? textarea.value.trim() : '' 
                if(!textareaValue){
                    alert(`${qNum}번 문항의 개별 지문을 작성해주세요.`)
                    textarea.focus()
                    return false
                }

                this.questionObj.individualPassage = {
                    type: 'text',
                    content: textareaValue
                }
            } else if(type == 'image'){
                const fileInput = passageGroup.querySelector(`input[type='file']`)
                const hasNewFile = fileInput && fileInput.files.length > 0 // 새로운 파일 업로드 여부 체크
                const currentContent = this.questionObj.individualPassage ? this.questionObj.individualPassage.content : ''
                const isExistingImageFile = (currentContent.match(/\.(jpg|jpeg|png|gif)$/i))
                if(!hasNewFile && !isExistingImageFile){
                    // 새 파일도 없고, 기존 파일도 없는 경우
                    alert(`${qNum}번 문항의 이미지를 등록해주세요.`)
                    return false
                }

                if(hasNewFile){
                    // 새 파일을 선택한 경우에만 individualPassage 객체 업데이트
                    this.questionObj.individualPassage = {
                        type: 'image',
                        content: fileInput.files[0].name,
                        fileKey: `question_${qNum}_individual_image`
                    }
                    formData.append(`question_${qNum}_individual_image`, fileInput.files[0])
                } 
            }

            this.questionObj.useIndividualPassage = 'Y'
        } else {
            this.questionObj.useIndividualPassage = 'N'
            this.questionObj.individualPassage = null
        }
        
        // 공통 지문 
        const checkInput = passageGroup.querySelector(`#common-passage-toggle-${qNum}`)

        if(checkInput && checkInput.checked){
            if(!selectedPassageId){
                alert(`공통 지문을 등록하신 후 다시 시도해주세요.`)
                return false
            }

            const passage = window.passages.find(p => p.id == selectedPassageId)
            const copyPassage = Object.assign({}, passage)
            delete copyPassage.file
            this.questionObj.commonPassage = copyPassage
            this.questionObj.useCommonPassage = 'Y'

            if(passage.type == 'image' && passage.file){
                formData.append(passage.fileKey, passage.file)
            } 
        } else {
            this.questionObj.useCommonPassage = 'N'
            this.questionObj.commonPassage = null
        }

        return true
    },

    _collectQuestion(questionGroup){
        const textarea = questionGroup.querySelector("textarea")
        const textareaValue = textarea ? textarea.value.trim() : ''
        if(!textareaValue){
            alert('문항 내용을 작성해주세요.')
            textarea.focus()
            return false
        }

        this.questionObj.questionText = textareaValue
        return true
    },

    _collectChoices(optionsGroup){
        const inputs = optionsGroup.querySelectorAll("input.option-input")

        for(const input of inputs){
            const choiceText = input.value.trim()
            if(!choiceText){
                alert('선택지를 작성해주세요.')
                input.focus()
                return false
            }
            
            const choiceId = input.dataset.choiceId
            const choice = this.questionObj.questionChoices.find(c => c.choiceId == choiceId)
            if(!choice){
                alert('새로고침 후 다시 시도해주세요.')
                return false
            } 
            
            choice.choiceText = choiceText
        }

        return true
    },

    _collectAnswer(answerGroup){
        const answerInput = answerGroup.querySelector('.question-answer')
        const answerValue = answerInput ? answerInput.value.trim() : ''
        if(!answerValue){
            alert('문항의 정답을 입력해주세요.')
            answerInput.focus()
            return false
        }

        this.questionObj.questionAnswer.correctAnswer = parseInt(answerValue)
        return true
    }

}

document.addEventListener('DOMContentLoaded', () => {
    if(!document.querySelector(".exam_edit_page")) return

    // 에디터 초기화
    ExamEditor.init()
    const data = ExamEditor.getData()

    // 공통지문 설정
    window.common.setupCommonPassages(data, window.passages)
    
    // 데이터 초기화
    QuestionEditHandler.init()

    // 시험지 리스너
    questionContainer_edit.addEventListener('click', (e) => {
        window.common.handleQuestionContainerClick(e, window.passages)
    })

    // 모달창 리스너
    commonPassageModal_edit.addEventListener('click', (e) => {
        const context = {
            commonPassageModal: commonPassageModal_edit,
            renderCommonPassageList: window.common.renderCommonPassageList,
            createPassageTextHtml: window.common.createPassageTextHtml,
            createPassageImageHtml: window.common.createPassageImageHtml,
            closeCommonPassageModal: window.common.closeCommonPassageModal,
            showToastMessage: window.common.showToastMessage,
            questionContainer: questionContainer_edit
        }
    
        window.common.handleModalClick(e, context) // UI 렌더링
    })

    // 수정 버튼 클릭 리스너
    const btnSaveExam = document.querySelector("#btnSaveExam")
    if(btnSaveExam){
        btnSaveExam.addEventListener('click', () => {
            const data = ExamEditor.getData()
            QuestionEditHandler.handleEditClick(data)
        })
    }
})




window.edit_common = {
    renderPassageInput: ExamEditor.renderPassageInput.bind(ExamEditor),
    bindPassageEvents: ExamEditor.bindPassageEvents.bind(ExamEditor)
}
