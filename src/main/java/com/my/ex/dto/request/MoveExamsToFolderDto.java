package com.my.ex.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class MoveExamsToFolderDto {
	int folderId;
	List<Integer> examIds;
}
