package com.my.ex.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.my.ex.dto.ExamFolderDto;
import com.my.ex.dto.request.MoveExamsToFolderDto;

@Repository
public class AdminDao implements IAdminDao {

	private final String NAMESPACE = "com.my.ex.AdminMapper.";
	
	@Autowired
	private SqlSession session;
	
	@Override
	public int saveFolder(String folderName) {
		return session.insert(NAMESPACE + "saveFolder", folderName);
	}

	@Override
	public int isFolderNameExists(String folderName) {
		return session.selectOne(NAMESPACE + "isFolderNameExists", folderName);
	}

	@Override
	public List<ExamFolderDto> getFolderList() {
		return session.selectList(NAMESPACE + "getFolderList");
	}

	@Override
	public List<ExamFolderDto> getFolderListExcluding(int excludeFolderId) {
		return session.selectList(NAMESPACE + "getFolderListExcluding", excludeFolderId);
	}
	
	@Override
	public int deleteFolder(int folderId) {
		return session.update(NAMESPACE + "deleteFolder", folderId);
	}
	
	@Override
	public int deleteExamInFolder(int folderId) {
		return session.update(NAMESPACE + "deleteExamInFolder", folderId);
	}

	@Override
	public int isDeleted(String folderName) {
		return session.selectOne(NAMESPACE + "isDeleted", folderName);
	}

	@Override
	public int restoreFolder(String folderName) {
		return session.update(NAMESPACE + "restoreFolder", folderName);
	}

	@Override
	public int moveExamsToFolder(MoveExamsToFolderDto dto) {
		return session.update(NAMESPACE + "moveExamsToFolder", dto);
	}

	@Override
	public ExamFolderDto getFolderInfoByExamId(int examId) {
		return session.selectOne(NAMESPACE + "getFolderInfoByExamId", examId);
	}

}
