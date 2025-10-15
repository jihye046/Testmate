package com.my.ex.dao;


import com.my.ex.dto.UserDto;

public interface IUserDao {
	String getUserPassword(String userId);
	int signup(UserDto dto);
	int checkIdDuplicate(String checkId);
}
