
document.addEventListener('DOMContentLoaded', () => {
    
    /* 문항 수정 버튼 리스너
    ================================================== */
    // const editBtn = document.querySelectorAll(".btn-edit-question")
    document.querySelectorAll(".btn-edit-question").forEach((editBtn) => {
        editBtn.addEventListener('click', (e) => {
            const urlParam = new URLSearchParams(window.location.search)
            const examId = urlParam.get("examId")
            const questionId = editBtn.dataset.questionId
            location.href = `/exam/editQuestion/${questionId}/${examId}`
        })
    })

    // 맨 위로 버튼
    const topBtn = document.querySelector("#btn-back-to-top")

    // 스크롤 감지: 300px 이상 내려오면 버튼 표시
    window.onscroll = () => {
        if (document.body.scrollTop > 300 || document.documentElement.scrollTop > 300) {
            topBtn.style.display = "flex";
        } else {
            topBtn.style.display = "none";
        }
    }

    topBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        })
    })
})

