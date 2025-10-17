document.addEventListener('DOMContentLoaded', () => {
	alert('⏰ 제한 시간은 60분이며, 시간이 종료되면 자동 제출됩니다.')

    /* 시험 타이머
    ================================================== */
    const timer = document.querySelector("#timer")
    let timeLimit = 60 * 60 // 60분
    const countdown = setInterval(() => {
        const minutes = Math.floor(timeLimit / 60)
        const seconds = timeLimit % 60

        if(timeLimit < 0){
            clearInterval(countdown)
            timer.textContent = "시험 종료"
            alert("시험 시간이 종료되었습니다. 시험이 자동 제출됩니다.")
            document.querySelector("#examForm").submit()
        }

        // 00:00 형식으로 표시
        timer.textContent = 
            (minutes < 10 ? '0' + minutes : minutes) + ':' +
            (seconds < 10 ? '0' + seconds : seconds)

        timeLimit--
    }, 1000)
    
})
