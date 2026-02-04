package com.my.ex.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamPdfPreview {
	private boolean saved;
	private String resultMessage;
	private List<Map<String, Object>> questions;
}
