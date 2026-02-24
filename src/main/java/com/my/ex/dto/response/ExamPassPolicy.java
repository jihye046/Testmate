package com.my.ex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExamPassPolicy {
	MIDDLE_GEOMJEONG(1, "중졸 검정고시", 60),
	HIGH_GEOMJEONG(2, "고졸 검정고시", 60);
	
	private int examTypeId;
	private String examTypeName;
	private int limitScore;
	
	
	public static boolean checkPass(int examTypeId, int totalScore) {
		for(ExamPassPolicy policy : ExamPassPolicy.values()) {
			if(policy.examTypeId == examTypeId) {
				return totalScore >= policy.limitScore;
			}
		}
		return false;
	}
}
