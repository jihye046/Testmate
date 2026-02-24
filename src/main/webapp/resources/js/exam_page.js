const submitHandler = {
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
        const form = document.querySelector("#examForm")
        axios.post('/exam/checkAnswers', new FormData(form))
            .then(response => {
                this._showResult(
                    response.data.result,
                    response.data.totalScore,
                    false)
                    // response.data.isPassed)
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

    _showResult(result, totalScore, isPassed){
        document.querySelector(".exam-content-wrapper").classList.add('result-mode')
        document.querySelector(".submit-button").classList.add('hidden')

        // 점수 표시
        document.querySelector("#timer").innerHTML = `${totalScore} / 100`

        // 합격/불합격 배지 표시
        const badge = document.querySelector(".result-badge")
        if(badge){
            badge.classList.remove('hidden')

            if(isPassed){
                badge.textContent = "합격"
                badge.className = "result-badge pass"
            } else {
                badge.textContent = "불합격"
                badge.className = "result-badge fail"
            }
        }
        
        // 각 문제별 정답/오답 표시
        result.forEach((dto) => {
            const qElement = document.querySelector(`#qnum_${dto.questionId}`)
            if(dto.isCorrect == 'Y'){
                qElement.classList.add('mark-correct')
            } else {
                qElement.classList.add('mark-wrong')
                this._highlightCorrect(dto.questionId, dto.correctAnswer, dto.userAnswer)
            }
        })

        // 결과 효과 트리거
        this._triggerResultEffect(isPassed)
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
    },

    _triggerResultEffect(isPassed){
        if(isPassed){
            // 합격 시: 화려한 폭죽 효과
            const duration = 3 * 1000; // 3초 동안
            const end = Date.now() + duration;

            const successColors = ['#FFD700', '#2ecc71', '#ffffff', '#FFB800'];
            (function frame() {
                confetti({
                    particleCount: 3,
                    angle: 60,
                    spread: 55,
                    origin: { x: 0 }, // 왼쪽에서 발사
                    colors: successColors
                })
                confetti({
                    particleCount: 3,
                    angle: 120,
                    spread: 55,
                    origin: { x: 1 }, // 오른쪽에서 발사
                    colors: successColors
                })

                if (Date.now() < end) {
                    requestAnimationFrame(frame)
                }
            }())
        } 
    }
}

document.addEventListener('DOMContentLoaded', () => {
	alert('⏰ 제한 시간은 60분이며, 시간이 종료되면 자동 제출됩니다.')
    submitHandler.init()
})
