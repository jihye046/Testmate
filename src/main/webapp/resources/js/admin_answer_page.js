document.addEventListener('DOMContentLoaded', () => {
    // renderAnswers()
    document.querySelector("#btnSaveAnswers").addEventListener("click", updateAnswers)

    document.querySelectorAll(".answer-input").forEach((input) => {
        const questionNum = input.closest('tr').querySelector("td").textContent
        updateStatus(input, questionNum)
    })
})

// 뱃지 상태 업데이트
const updateStatus = (input, questionNum) => {
    const badge = document.querySelector(`#status-${questionNum}`)

    if(input.value){
        badge.className = 'status-badge status-done'
        badge.innerHTML = '<i class="fas fa-check"></i> 완료'
    } else {
        badge.className = 'status-badge status-wait'
        badge.innerHTML = '<i class="fas fa-clock"></i> 대기'
    }

    // 요약 정보 실시간 업데이트
    const inputs = document.querySelectorAll(".answer-input")
    const total = inputs.length
    const done = Array.from(inputs).filter(i => i.value !== "").length
    updateSummary(total, done)
}

// '총 문항 / 입력 완료 / 미입력' 개수 업데이트
const updateSummary = (total, done) => {
    document.querySelector("#totalCount").textContent = total
    document.querySelector("#doneCount").textContent = done
    document.querySelector("#waitCount").textContent = total - done
}

// 입력값 비우기
const resetInput = (btn, questionNum) => {
    const input = btn.closest('tr').querySelector("input")
    input.value = ""
    updateStatus(input, questionNum)
}

// 정답지 저장
const updateAnswers = () => {
    let answers = []

    document.querySelectorAll(".answer-input").forEach((input) => {
        const answerObj = {
            questionId: input.closest('tr').querySelector(".q-num").dataset.questionId,
            correctAnswer: input.value
        }
        answers.push(answerObj)
    })

    axios.patch('/exam/updateAnswers', {answers: answers})
        .then(response => {
            console.log(response.data)
        })
        .catch(error => {
            console.error('error: ', error)
        })
}
