package com.my.ex.dto;

import lombok.Data;

@Data
public class ExamFolderDto {
	private int folderId;
	private String folderName;
	private int examCount;
	private char isDeleted;
}
