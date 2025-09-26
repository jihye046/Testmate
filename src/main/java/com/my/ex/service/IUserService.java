package com.my.ex.service;

import org.springframework.web.bind.annotation.RequestParam;

import com.my.ex.dto.UserDto;

public interface IUserService {
	String getUserPassword(String userId);
	boolean signup(UserDto dto);
	boolean checkIdDuplicate(String checkId);
}
