package com.my.ex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.my.ex.dao.UserDao;
import com.my.ex.dto.UserDto;

@Service
public class UserService implements IUserService {

	@Autowired
	private UserDao dao;
	
	@Override
	public boolean signup(UserDto dto) {
		return dao.signup(dto) > 0;
	}

	@Override
	public String getUserPassword(String userId) {
		return dao.getUserPassword(userId);
	}

	@Override
	public boolean checkIdDuplicate(String checkId) {
		return dao.checkIdDuplicate(checkId) > 0;
	}
	
}
