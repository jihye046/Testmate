// 전역 변수
window.passages = []
const commonPassageModal_edit = document.querySelector("#commonPassageModal")
const questionContainer_edit = document.querySelector("#question-list-container")

document.addEventListener('DOMContentLoaded', () => {
    ExamEditor.init()
    
    const data = ExamEditor.getData()
    document.querySelector("#commonPassageViewBtn").addEventListener('click', () => {
        window.common.openCommonPassageModal(data.qNum)
    }) 

    
    // 공통 지문 모달
    commonPassageModal.addEventListener('click', (e) => {
        const context = {
            commonPassageModal: commonPassageModal_edit,
            renderCommonPassageList: window.common.renderCommonPassageList,
            createPassageTextHtml: window.common.createPassageTextHtml,
            createPassageImageHtml: window.common.createPassageImageHtml,
            closeCommonPassageModal: window.common.closeCommonPassageModal,
            showToastMessage: window.common.showToastMessage,
            questionContainer: questionContainer_edit
        }
    
        window.common.handleModalClick(e, context)
    })

})


// Quill 에디터 초기화
const ExamEditor = {
    editors : {},

    dataCache: {
        qNum: null,
        individual: null,
        common: null
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

            // 1. 개별 지문 초기 렌더링
            this.initSection('individual', this.dataCache.qNum, this.dataCache.individual)

            // 2. 공통 지문 초기 렌더링
            this.initSection('common', 'modal', this.dataCache.common)

            // 3. 이벤트 바인딩(데이터 유무와 상관없이 항상 실행)
            this.bindPassageEvents()
        } catch (error) {
            console.error("데이터 파싱 중 오류 발생: ", error)
        }

    },

    getData(){
        return this.dataCache
    },

    initSection(category, id, passageData){
        if(!passageData || passageData.trim() == ''){
            return
        }

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

    // 지문 에디터 이벤트
    bindPassageEvents(){
        // 개별 지문 내 유형 선택
        document.querySelectorAll('.btn-passage-type').forEach((btn) => {
            btn.addEventListener('click', () => {
                const type = btn.dataset.type
                const qNum = btn.dataset.qNum
                this.renderPassageInput(qNum, type, this.dataCache.individual)
            })
        })

        // 모달 내 유형 선택
        document.querySelectorAll('.modal-btn-passage-type').forEach((btn) => {
            btn.addEventListener('click', () => {
                const type = btn.dataset.type
                this.renderPassageInput('modal', type, this.dataCache.common)
            })
        })
    },

    /**
     * 
     * @param {string*} qNum - 문항 번호 또는 'modal'
     * @param {string} type - 'text' 또는 'image'
     * @param {string} initialData - 초기 채워넣을 지문 데이터
     */
    renderPassageInput(qNum, type, initialData = ''){
        // 버튼 컨테이너
        const btnContainer = (qNum == 'modal')
            ? document.querySelector('#modal-passage-controls')
            : document.querySelector(`#passage-controls-${qNum}`)

        // 지문 컨테이너
        const passageContainer = (qNum == 'modal')
            ? document.querySelector('#modal-passage-input')
            : document.querySelector(`#passage-content-${qNum}`)

        // 버튼
        const textBtn = btnContainer.querySelector('button[data-type="text"]')
        const imageBtn = btnContainer.querySelector('button[data-type="image"]')
        
        passageContainer.innerHTML = '' // 초기화

        if(type == 'text'){
            window.common.activeButton(textBtn)
            window.common.resetButton(imageBtn)

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
            // textarea HTML 구성
            const originalHtml = window.common.createPassageTextHtml(qNum)
            passageContainer.innerHTML =
            `  
                <div class="editor-container" id="editor-wrapper-${qNum}">
                    ${tipHtml}
                    <div id="editor-${qNum}" class="quill-editor-box"></div>
                    <div style="display:none;">${originalHtml}</div>
                </div>
            `

            this.initQuillEditor(qNum, initialData)
        } else if(type == 'image'){
            window.common.activeButton(imageBtn)
            window.common.resetButton(textBtn)

            passageContainer.innerHTML = window.common.createPassageImageHtml(qNum)        

            if(this.editors[qNum]){
                delete this.editors[qNum]
            }
        }
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

            originalLog.apply(console, args)
        }

        // Quill에 이미지 리사이즈 모듈 등록 
        if (!Quill.import('modules/imageResize')) {
            Quill.register('modules/imageResize', ImageResize.default)
        }

        const editorId = `#editor-${qNum}`
        const quill = new Quill(editorId, {
            theme: 'snow',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline', 'strike'],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    ['link', 'image'],
                    ['clean']
                ],
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
        if(initialData && !initialData.includes("<img")){ // 이미지 경로가 아닌 텍스트일 때만
            quill.root.innerHTML = initialData
            const hiddenTextarea = document.querySelector(`#passage-text-${qNum}`)
            if(hiddenTextarea) hiddenTextarea.value = initialData
        }

        // 내용 변경 시
        quill.on('text-change', () => {
            const html = quill.root.innerHTML
            const hiddenTextarea = document.querySelector(`#passage-text-${qNum}`)
            if(hiddenTextarea){
                hiddenTextarea.value = html 
            }
        })  
    }
}


