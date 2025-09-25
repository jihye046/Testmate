package com.my.ex.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExamInfoGroup {
	private List<String> examRounds;
	private List<String> examSubjects;
}
