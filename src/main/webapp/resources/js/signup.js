/* 전역 변수
================================================== */
	// 아이디 관련
const userIdInput = document.querySelector("#userId")
const idRequirementMessage = document.querySelector("#idRequirement")              // 아이디 요구사항 출력 필드
let isIdChecked = false

	// 비밀번호 관련
const newPasswordInput = document.querySelector("#password")                       // 설정할 비밀번호 필드
const confirmPasswordInput = document.querySelector("#passwordCheck")              // 새 비밀번호 확인 필드
const passwordRequirementMessage = document.querySelector("#passwordRequirement")  // 비밀번호 요구사항 출력 필드
const mismatchMessage = document.querySelector("#passwordMismatchMessage")         // 비밀번호 일치여부 출력 필드

	// 버튼
const singupBtn = document.querySelector("#signupBtn")                             // 회원가입 버튼

/* 메시지 스타일
================================================== */
    // 성공
const setValidStyle = (el, message) => {
    el.textContent = message
    el.style.color = '#28a745'
}

    // 실패
const setInvalidStyle = (el, message) => {
    el.textContent = message
    el.style.color = '#ff6347'
}

/* 아이디 유효성 검사
================================================== */
const validateId = (id) => {
    const minLenght = 4                           // 최소 4자 이상
    const maxLenght = 12                          // 최대 12자 이하
    const hasSpace = /\s/.test(id)                // 스페이스, 탭, 줄바꿈이 있으면 true, 없으면 false
    const isFirstCharLower = /^[a-z]/.test(id)    // 첫 글자가 소문자이면 true, 아니면 false
    const hasInvalidChar = /[^a-z0-9_.]/.test(id) // 허용되지 않은 문자 포함 시 true, 없으면 false
    
    if(!isFirstCharLower){
        setInvalidStyle(idRequirementMessage, '첫 글자는 소문자로 시작해야 합니다.')
        return false
    } else if(hasInvalidChar){
        setInvalidStyle(idRequirementMessage, '아이디는 소문자로 시작해야 하며, 소문자, 숫자, 특수문자(_, .)만 사용할 수 있습니다.')
        return false
    } else if(id.length < minLenght || id.length > maxLenght){
        setInvalidStyle(idRequirementMessage, '아이디는 4자 이상 12자 이하로 입력해주세요.')
        return false
    } else if(hasSpace){
        setInvalidStyle(idRequirementMessage, '아이디에 공백은 사용할 수 없습니다.')
        return false
    } else {
        setValidStyle(idRequirementMessage, '')
        return true
    }
}

/* 비밀번호 유효성 검사
================================================== */
const validatePassword = (password, showMessage = true) => {
    const minLenght = 8                      // 최소 8자 이상
    const maxLenght = 16                     // 최대 16자 이하
    const hasSpace = /\s/.test(password)     // 스페이스, 탭, 줄바꿈이 있으면 true, 없으면 false
    const hasUpper = /[A-Z]/.test(password)  // 대문자가 하나 이상이면 true, 없으면 false
    const hasLower = /[a-z]/.test(password)  // 소문자가 하나 이상이면 true, 없으면 false
    const hasNumber = /[0-9]/.test(password) // 숫자가 하나 이상이면 true, 없으면 false
    const hasSpecial = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password) // 특수문자가 하나 이상이면 true, 없으면 false
    
    if(password.length < minLenght || password.length > maxLenght){
        // 사용자가 비밀번호 입력 전에도 경고 메시지가 뜨는 현상을 막기 위해 사용
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호는 8자 이상 16자 이하로 입력해주세요.')
        }
        return false
    } else if(hasSpace){
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호에 공백은 사용할 수 없습니다.')
        }
        return false
    } else if(!hasUpper){
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호에 최소 하나 이상의 대문자가 포함되어야 합니다.')
        }
        return false
    } else if(!hasLower){
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호에 최소 하나 이상의 소문자가 포함되어야 합니다.')
        }
        return false
    } else if(!hasNumber){
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호에 최소 하나 이상의 숫자가 포함되어야 합니다.')
        }
        return false
    } else if(!hasSpecial){
        if(showMessage){
            setInvalidStyle(passwordRequirementMessage, '비밀번호에 최소 하나 이상의 특수문자가 포함되어야 합니다.')
        }
        return false
    } else {
        if(showMessage){
            setValidStyle(passwordRequirementMessage, '')
        }
        return true
    }
}

/* 비밀번호 일치 여부 검사
================================================== */
const checkPasswordMatch = () => {
    const password = newPasswordInput.value
    let confirmPassword = confirmPasswordInput.value

    if(password != confirmPassword){
        setInvalidStyle(mismatchMessage, '비밀번호가 일치하지 않습니다.')
        return false
    } else {
        setValidStyle(mismatchMessage, '')
        return true
    }
}

/* 폼 전송 제어
================================================== */
const form = document.querySelector("#signup-form")
form.addEventListener('submit', (e) => {
    e.preventDefault()

    const isPasswordValid = validatePassword(newPasswordInput.value)
    const isPasswordMatched = checkPasswordMatch()
    const isIdValid = validateId(userIdInput.value)

    if(!isPasswordValid || !isPasswordMatched){
        alert('비밀번호를 다시 확인해주세요.')
        return
    } else if(!isIdValid) {
        alert('아이디를 다시 확인해주세요.')
        return
    } else if(!isIdChecked){
        alert('아이디 중복 여부를 확인해 주세요.')
    }

    form.submit()
})

/* 페이지 로드 시 실행될 함수
================================================== */
document.addEventListener("DOMContentLoaded", function() {
    /* 회원가입 버튼 활성화 조건
        - 새 비밀번호 유효성 검사 통과
        - 새 비밀번호와 확인 비밀번호 일치
        - 아이디 유효성 검사 통과
        - 아이디 중복 검사 통과
        - 닉네임 중복 검사 통과
    ================================================== */
  	// 조건에 따라 회원가입 버튼 활성화/비활성화 처리
    const updateSingupBtnState = () => {
        const isPasswordValid = validatePassword(newPasswordInput.value, false) // 비밀번호 유효성 검사
        const isPasswordMatched = checkPasswordMatch(false)                     // 비밀번호 일치 여부 확인
        const isIdValid = validateId(userIdInput.value, false)                  // 아이디 유효성 검사

        // 일곱가지 조건 모두 만족해야 버튼 활성화
        singupBtn.disabled = !(
            isPasswordValid &&
            isPasswordMatched && 
            isIdValid && 
            isIdChecked
        )
    }

  	// 새 비밀번호 입력 시 리스너
    newPasswordInput.addEventListener('input', () => {
        validatePassword(newPasswordInput.value, true)
        checkPasswordMatch(true)
        updateSingupBtnState()
    })

  	// 새 비밀번호 확인 입력 시 리스너
    confirmPasswordInput.addEventListener('input', () => {
        validatePassword(newPasswordInput.value, true)
        checkPasswordMatch(true)
        updateSingupBtnState()
    })

  	// 아이디 입력 시 리스너
    userIdInput.addEventListener('input', () => {
        isIdChecked = false
        const isIdValid = validateId(userIdInput.value)
        checkIdButton.disabled = !isIdValid
        updateSingupBtnState()
    })

    // 아이디 중복 확인 버튼 리스너
    document.querySelector("#checkIdButton").addEventListener('click', () => {
        axios.get('/user/check-id-duplicate', {
            params: {checkId: userIdInput.value}
        })
            .then(response => {
                if(response.data){
                    alert('사용중인 아이디입니다.')
                    isIdChecked = false
                } else if(!(response.data)) {
                    alert('사용 가능한 아이디입니다.')
                    isIdChecked = true
                }
                updateSingupBtnState()
            })
            .catch(error => {
                console.error('error: ', error)
            })
    })

})
