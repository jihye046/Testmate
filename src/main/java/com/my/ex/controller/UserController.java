package com.my.ex.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.my.ex.dto.UserDto;
import com.my.ex.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService service;
	
	@GetMapping("/loginPage")
	public String loginForm() {
		return "/user/login";
	}
	
	@GetMapping("/signupPage")
	public String signupForm() {
		return "/user/signup";
	}
	
	// 로그인
	@PostMapping("/login")
	public String login(
			@RequestParam String userId, 
			@RequestParam String userPw,
			HttpSession session,
			RedirectAttributes rttr) {
		String encodePassword = service.getUserPassword(userId);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		// 비밀번호 일치 여부 확인
		boolean isPasswordMatch = passwordEncoder.matches(userPw, encodePassword);
		
		if(isPasswordMatch) {
			session.setAttribute("userId", userId);
			String targetLocation = (String)session.getAttribute("targetLocation");
			return "redirect:/exam/main";
//			return (targetLocation != null) ? "redirect:" + targetLocation : "redirect:/exam/main";
		} else {
			rttr.addFlashAttribute("loginFail", true);
			return "redirect:loginPage";
		}
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/exam/main";
	}
	
	// 회원가입
	@PostMapping("/signup")
	public String signup(UserDto dto, RedirectAttributes rttr) {
		boolean isPasswordValid = isPasswordValid(dto.getUserPw());
		boolean isIdValid = isIdValid(dto.getUserId());
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		if(isPasswordValid && isIdValid) {
			String hashedPassword = passwordEncoder.encode(dto.getUserPw());
			dto.setUserPw(hashedPassword);
			boolean singupResult = service.signup(dto);
			
			if(singupResult) {
				rttr.addFlashAttribute("userId", dto.getUserId());
				rttr.addFlashAttribute("singupResult", true);
			}
		} else {
			rttr.addFlashAttribute("singupResult", false);
		}
		
		return "redirect:loginPage";
	}

	// 아이디 중복 확인
	@GetMapping("/check-id-duplicate")
	@ResponseBody
	public boolean checkIdDuplicate(@RequestParam String checkId) {
		return service.checkIdDuplicate(checkId);
	}
	
	// 비밀번호 유효성 검사(서버에서 한번 더 검사)
	private boolean isPasswordValid(String password) {
		if(password == null || password.length() < 8 || password.length() > 16) return false;
		if(password.matches(".*\\s+.*")) return false; // 공백 체크
		if(!password.matches(".*[A-Z].*")) return false; // 대문자
		if(!password.matches(".*[a-z].*")) return false; // 소문자
		if(!password.matches(".*\\d.*")) return false; // 숫자
		if(!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\/,.<>\\/?].*")) return false; // 특수문자
		return true;
	}
	
	// 아이디 유효성 검사(서버에서 한번 더 검사)
	private boolean isIdValid(String userId) {
		if(userId == null || userId.length() < 4 || userId.length() > 12) return false;
		if(userId.matches(".*\\s+.*")) return false; // 공백 체크
		if(!userId.matches("^[a-z][a-z0-9_.]*$")) return false; // 소문자 시작 + 허용 문자만 사용
		return true;
	}
	
}
