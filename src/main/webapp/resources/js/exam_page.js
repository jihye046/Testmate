const submitHandler = {
    examInfo: {},

    questionObj: {},

    countdown: null, // 타이머 인스턴스

    init(){
        const button = document.querySelector(".submit-button")
        button.addEventListener('click', (e) => {
            e.preventDefault()
            if(confirm('제출하시겠습니까?')){
                this._submitExam()
            }
        })

        this._startTimer(60*60) // 60분
    },

    _submitExam(){
        if(this.countdown) clearInterval(this.countdown)
        // document.querySelector("#examForm").submit()
        const form = document.querySelector("#examForm")
        axios.post('/exam/checkAnswers', new FormData(form))
            .then(response => {
                console.log(response.data)
                this._showResult(response.data)
            })
            .catch(error => {
                console.error('error: ', error)
            })
    },

    _startTimer(timeLimit){
        const timer = document.querySelector("#timer")
        this.countdown = setInterval(() => {
            if(timeLimit < 0){
                timer.textContent = "시험 종료"
                alert("시험 시간이 종료되었습니다. 시험이 자동 제출됩니다.")
                this._submitExam()
                return
            }
            
            const minutes = Math.floor(timeLimit / 60)
            const seconds = timeLimit % 60

            // 00:00 형식으로 표시
            timer.textContent = 
                (minutes < 10 ? '0' + minutes : minutes) + ':' +
                (seconds < 10 ? '0' + seconds : seconds)

            timeLimit--
        }, 1000)
    },

    _showResult(data){
        document.querySelector(".exam-content-wrapper").classList.add('result-mode')
        document.querySelector(".submit-button").classList.add('hidden')
        document.querySelector("#timer").innerHTML = `최종 점수: ??점`

        data.forEach((dto) => {
            const qElement = document.querySelector(`#qnum_${dto.questionId}`)
            if(dto.isCorrect == 'Y'){
                qElement.classList.add('mark-correct')
            } else {
                qElement.classList.add('mark-wrong')
                this._highlightCorrect(dto.questionId, dto.correctAnswer, dto.userAnswer)
            }
        })
    },

    _highlightCorrect(questionId, correctAnswerNum, userAnswer){
        const questionContainer = document.querySelector(`.question-item[data-question-id="${questionId}"]`)
        const correctChoice = questionContainer.querySelector(`.option-label[data-choice-num="${correctAnswerNum}"]`)
        
        if(correctChoice){
            correctChoice.classList.add('actual-answer')
        }

        if(userAnswer == null || userAnswer == 0){
            correctChoice.classList.add('actual-answer')
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
	alert('⏰ 제한 시간은 60분이며, 시간이 종료되면 자동 제출됩니다.')
    submitHandler.init()
})
