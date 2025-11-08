package com.my.ex.service;

import java.util.List;

import com.my.ex.dto.ExamFolderDto;
import com.my.ex.dto.request.MoveExamsToFolderDto;

public interface IAdminService {
	boolean saveFolder(String folderName);
	boolean isFolderNameExists(String folderName);
	boolean isDeleted(String folderName);
	boolean restoreFolder(String folderName);
	List<ExamFolderDto> getFolderList();
	List<ExamFolderDto> getFolderListExcluding(int excludeFolderId);
	boolean moveExamsToFolder(MoveExamsToFolderDto dto);
	boolean deleteFolder(int folderId);
	ExamFolderDto getFolderInfoByExamId(int examId);
}
