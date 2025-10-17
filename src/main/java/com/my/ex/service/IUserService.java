package com.my.ex.service;

import com.my.ex.dto.UserDto;

public interface IUserService {
	String getUserPassword(String userId);
	boolean signup(UserDto dto);
	boolean checkIdDuplicate(String checkId);
}
