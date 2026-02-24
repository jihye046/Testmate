package com.my.ex.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamInfoDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.request.ExamCreateRequestDto;
import com.my.ex.dto.response.ExamPageDto;
import com.my.ex.dto.response.ExamTitleDto;
import com.my.ex.dto.service.ParsedExamData;

public interface IExamSelectionService {
	List<ExamTypeDto> getExamTypes();
	List<ExamTypeDto> getAllExamTypes();
	List<ExamTypeDto> getExamPaperTypes();
	List<String> getExamRounds(String examTypeCode);
	List<String> getSubjectsByExamRound(String examTypeCode, String examRound);
	List<ExamTitleDto> getAllExamTitlesByFolderId(int folderId);
	boolean saveParsedExamData(ExamInfoDto examInfo, List<Map<String, Object>> questions);
	String getExamtypename(String examType);
	List<ExamQuestionDto> getExamQuestions(String examType, String examRound, String examSubject);
	List<ExamQuestionDto> getExamQuestionsByExamId(int examId);
	List<ExamChoiceDto> getExamChoicesByExamId(int examId);
	List<ExamChoiceDto> getExamChoicesByQuestionId(int questionId);
	Set<ExamPageDto.ExamCommonpassageDto> getCommonPassageInfo(String examType, String examRound, String examSubject);
	int getTotalQuestionCount(int examId);
	boolean deleteExams(List<Integer> examIds);
	List<String> getSubjectsForExamType(String examTypeCode);
	int findTypeIdByCode(String type);
	boolean saveExamByForm(ExamCreateRequestDto request);
	ParsedExamData buildParsedExamData(ExamCreateRequestDto request);
	String ensureImageFolderExists(String folderPath, String filename, MultipartFile file);
	List<Map<String, Object>> parsePdfToQuestions(MultipartFile file) throws Exception;
	String findExistingExamFolderId(ExamInfoDto examInfoDto);
	ExamInfoDto getExamInfoByExamId(int examId);
	ExamQuestionDto getExamQuestionByQuestionId(int questionId);
	String saveEditorImage(MultipartFile image) throws IOException;
	String processHtmlEmbeddedImages(String content, String examType, String examRound, String examSubject);
	int findFolderIdByExamId(int examId);
	void updateExamByForm(ExamCreateRequestDto request);
	int getExamIdByExamTypeId(String examTypeCode,String examRound, String examSubject);
	int getExamTypeIdByExamTypeCode(String examTypeCode);
}
