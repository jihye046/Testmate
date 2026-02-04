document.addEventListener('DOMContentLoaded', () => {

    /* 전역 변수
    ================================================== */
    const examTypeSelect = document.querySelector("#examType")
    const examRoundSelect = document.querySelector("#examRound")
    const examSubjectSelect = document.querySelector("#examSubject")
    const nextButton = document.querySelector("#nextButton")
    
    /* 시험 과목 DB 초기화
    ================================================== */
    const getSubjectsByExamRound = (params) => {
        axios.get('/exam/getSubjectsByExamRound', { params })
            .then(response => {
                const examSubjects = response.data.examSubjects
                updateExamSubjects(examSubjects)

                if(examSubjectSelect.value){
                    nextButton.disabled = false
                }
            })
            .catch(error => {
                console.error('error: ', error)
            })
    }

    /* 시험 회차 UI 초기화
    ================================================== */
    const updateExamRounds = (examRounds) => {
        let options = ''
        examRounds.forEach((round) => {
            options += `<option value="${round}">${round}</option>`
        })
        examRoundSelect.innerHTML = options
        examRoundSelect.disabled = false

        const params = {
            examTypeCode: examTypeSelect.value,
            examRound: examRoundSelect.value
        }

        getSubjectsByExamRound(params)
    }

    /* 시험 과목 UI 초기화
    ================================================== */
    const updateExamSubjects = (examSubjects) => {
        let options = ''
        examSubjects.forEach((subject) => {
            options += `<option value="${subject}">${subject}</option>`
        })
        examSubjectSelect.innerHTML = options
        examSubjectSelect.disabled = false
    }
    
    /* 시험 종류 변경 시
    ================================================== */
    examTypeSelect.addEventListener('change', () => {
        const examTypeSelectedValue = examTypeSelect.value
        if(!examTypeSelectedValue) return

        const params = {
            examTypeCode: examTypeSelectedValue
        }
        
        // 회차 데이터 가져오기
        axios.get('/exam/getExamRounds', { params })
            .then(response => {
                updateExamRounds(response.data.examRounds)
            })
            .catch(error => {
                console.error('error: ', error)
            })
    })

    /* 시험 회차 변경 시
    ================================================== */
    examRoundSelect.addEventListener('change', () => {
        const examTypeSelectedValue = examTypeSelect.value
        const examRoundSelectedValue = examRoundSelect.value
        if(!examTypeSelectedValue || !examRoundSelectedValue) return

        const params = {
            examTypeCode: examTypeSelectedValue,
            examRound: examRoundSelectedValue
        }
        
        // 과목 데이터 가져오기
        getSubjectsByExamRound(params)
    })

    /* 다음 버튼 클릭 시
    ================================================== */
    nextButton.addEventListener('click', (e) => {
        e.preventDefault()
        window.location.href = 
        	`/exam/showExamPage?examTypeEng=${examTypeSelect.value}` +
            `&examRound=${examRoundSelect.value}` +
            `&examSubject=${examSubjectSelect.value}`

        /* 
        const params = new URLSearchParams({
            examTypeEng: examTypeSelect.value,
            examRound: examRoundSelect.value,
            examSubject: examSubjectSelect.value
        }).toString()
        

        window.location.href = `/exam/showExamPage?${params}`
        */
    })
})
