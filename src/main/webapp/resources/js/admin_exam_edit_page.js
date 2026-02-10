// 전역 변수
window.passages = []
const commonPassageModal_edit = document.querySelector("#commonPassageModal")
const questionContainer_edit = document.querySelector("#question-list-container")

document.addEventListener('DOMContentLoaded', () => {
    if(!document.querySelector(".exam_edit_page")) return

    ExamEditor.init()
    
    // '공통 지문 보기' 버튼 클릭 시
    const commonPassageViewBtn = document.querySelector("#commonPassageViewBtn")
    commonPassageViewBtn.addEventListener('click', () => {
        const data = ExamEditor.getData()
        window.common.openCommonPassageModal(data.qNum)
    }) 

    // 공통 지문 모달
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

    // 체크박스 클릭 시 '공통 지문 설정' 버튼 상태 변경
    const toggle = document.querySelector(".common-passage-toggle")
    toggle.addEventListener('click', () => {
        commonPassageViewBtn.disabled = !toggle.checked

        // 초기 데이터에 공통 지문이 없는데 공통 지문 박스에 체크한 경우
        // UI 렌더링만 실행
        const currentData = ExamEditor.getData()
        if(currentData.common == null || currentData.common == '' ){
            ExamEditor.bindPassageEvents()
        }
    })

})


// Quill 에디터 초기화
const ExamEditor = {
    editors : {},

    dataCache: {
        qNum: null,
        individual: '',
        common: ''
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
            this.bindPassageEvents(this.dataCache.individual, this.dataCache.common)
        } catch (error) {
            console.error("데이터 파싱 중 오류 발생: ", error)
        }

    },

    getData(){
        return this.dataCache
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

    // 지문 에디터 이벤트 (등록 페이지에서는 인자 없이 호출됨)
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

            setTimeout(() => {
                this.initQuillEditor(qNum, initialData)
            }, 100)
        } else if(type == 'image'){
            window.common.activeButton(imageBtn)
            window.common.resetButton(textBtn)

            passageContainer.innerHTML = window.common.createPassageImageHtml(qNum)        

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
                if(hiddenTextarea) hiddenTextarea.value = initialData
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

window.edit_common = {
    renderPassageInput: ExamEditor.renderPassageInput.bind(ExamEditor),
    bindPassageEvents: ExamEditor.bindPassageEvents.bind(ExamEditor)
}
