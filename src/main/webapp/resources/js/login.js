// 회원가입 결과
const singupResult = document.querySelector("#singupResult").getAttribute("data-singup-result")
if(singupResult == "true") {
	alert('회원가입을 축하드립니다!')
} else if(singupResult == "false"){
	alert('일시적인 오류로 회원가입이 완료되지 않았습니다. 잠시 후 다시 시도해 주세요.')
} 

// 로그인 실패 시 결과
const loginFail = document.querySelector("#loginFail").getAttribute("data-loginFail")
if(loginFail == "true"){
    alert('아이디 또는 비밀번호가 일치하지 않습니다. 다시 시도해 주세요.')
}