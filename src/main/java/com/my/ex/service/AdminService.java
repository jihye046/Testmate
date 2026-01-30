package com.my.ex.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.my.ex.dao.AdminDao;
import com.my.ex.dto.ExamFolderDto;
import com.my.ex.dto.request.MoveExamsToFolderDto;

@Service
public class AdminService implements IAdminService {

	@Autowired
	private AdminDao dao;
	
	// 새 폴더 생성
	@Override
	public boolean saveFolder(String folderName) {
		return dao.saveFolder(folderName) > 0;
	}

	// 입력된 폴더명 중복 여부 확인
	@Override
	public boolean isFolderNameExists(String folderName) {
		return dao.isFolderNameExists(folderName) > 0;
	}
	
	// 삭제된 폴더인지 확인
	@Override
	public boolean isDeleted(String folderName) {
		return dao.isDeleted(folderName) > 0;
	}
	
	// 삭제된 폴더 복구
	@Override
	public boolean restoreFolder(String folderName) {
		return dao.restoreFolder(folderName) > 0;
	}

	// 폴더 리스트 조회
	@Override
	public List<ExamFolderDto> getFolderList() {
		return dao.getFolderList();
	}
	
	// 현재 위치한 폴더를 제외한 나머지 폴더 리스트 조회
	@Override
	public List<ExamFolderDto> getFolderListExcluding(int excludeFolderId) {
		return dao.getFolderListExcluding(excludeFolderId);
	}

	// 폴더 삭제
	@Override
	@Transactional
	public void deleteFolder(int folderId) {
		dao.deleteExamInFolder(folderId); 
		dao.deleteFolder(folderId);
	}

	// 선택된 시험지를 지정된 폴더로 이동
	@Override
	public boolean moveExamsToFolder(MoveExamsToFolderDto dto) {
		return dao.moveExamsToFolder(dto) > 0;
	}
	
	// 선택된 시험지의 폴더ID와 폴더Name 조회
	@Override
	public ExamFolderDto getFolderInfoByExamId(int examId) {
		return dao.getFolderInfoByExamId(examId);
	}

}
