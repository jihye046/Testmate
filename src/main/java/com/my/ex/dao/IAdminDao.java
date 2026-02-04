package com.my.ex.dao;

import java.util.List;

import com.my.ex.dto.ExamFolderDto;
import com.my.ex.dto.request.MoveExamsToFolderDto;

public interface IAdminDao {
	int saveFolder(String folderName);
	int isFolderNameExists(String folderName);
	int isDeleted(String folderName);
	int restoreFolder(String folderName);
	List<ExamFolderDto> getFolderList();
	List<ExamFolderDto> getFolderListExcluding(int excludeFolderId);
	int moveExamsToFolder(MoveExamsToFolderDto dto);
	int deleteFolder(int folderId);
	int deleteExamInFolder(int folderId);
	ExamFolderDto getFolderInfoByExamId(int examId);
}
