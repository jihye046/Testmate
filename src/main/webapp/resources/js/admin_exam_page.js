
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
    
})

