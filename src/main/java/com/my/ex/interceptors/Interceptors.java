package com.my.ex.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;

public class Interceptors implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession();
		String userId = (String)session.getAttribute("userId");
		
		// 로그인이 되어있지 않다면
		if(userId == null) {
			String uri = request.getRequestURI();
			String queryString = request.getQueryString();
			String targetLocation = uri + "?" + queryString;
			session.setAttribute("targetLocation", targetLocation);
			response.sendRedirect("/user/loginPage");
			
			return false; // 더이상 서버 로직을 수행하지 않도록 false 반환
		}
		
		return true; // 서버 로직을 수행하도록 true 반환
	}
}
