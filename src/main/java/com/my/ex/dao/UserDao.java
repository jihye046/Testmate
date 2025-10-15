package com.my.ex.dao;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.my.ex.dto.UserDto;

@Repository
public class UserDao implements IUserDao {

	private final String NAMESPACE = "com.my.ex.UserMapper.";
	
	@Autowired
	private SqlSession session;

	@Override
	public int signup(UserDto dto) {
		return session.insert(NAMESPACE + "signup", dto);
	}

	@Override
	public String getUserPassword(String userId) {
		return session.selectOne(NAMESPACE + "getUserPassword", userId);
	}

	@Override
	public int checkIdDuplicate(String checkId) {
		return session.selectOne(NAMESPACE + "checkIdDuplicate", checkId);
	}

}
