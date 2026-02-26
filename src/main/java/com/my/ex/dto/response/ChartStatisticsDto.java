package com.my.ex.dto.response;

import java.util.List;

import com.my.ex.dto.ExamInfoDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChartStatisticsDto {
	private int totalExamCount;
	private List<ExamInfoDto> missingAnswerExams;
}
