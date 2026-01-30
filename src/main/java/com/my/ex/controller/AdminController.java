package com.my.ex.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.my.ex.config.EnvironmentConfig;
import com.my.ex.dto.ExamAnswerDto;
import com.my.ex.dto.ExamChoiceDto;
import com.my.ex.dto.ExamFolderDto;
import com.my.ex.dto.ExamQuestionDto;
import com.my.ex.dto.ExamTypeDto;
import com.my.ex.dto.request.MoveExamsToFolderDto;
import com.my.ex.dto.response.ExamInfoGroup;
import com.my.ex.dto.response.ExamPageDto;
import com.my.ex.dto.response.ExamTitleDto;
import com.my.ex.service.AdminService;
import com.my.ex.service.ExamAnswerService;
import com.my.ex.service.ExamSelectionService;
import com.my.ex.service.IAdminService;

// 관리자 페이지에서 수행하는 관리 영역
@RequestMapping("/admin")
@Controller
public class AdminController {

	@Autowired
//	private AdminService service;
	private IAdminService service;
	
	@Autowired
	private ExamSelectionService examService;
	
	@Autowired
	private ExamAnswerService answerService;
	
	@Autowired
	private EnvironmentConfig config;
	
	/**
	 * 폴더 목록 페이지 보여줌 (동기식 요청)
	 * - 사용자가 페이지에 처음 진입할 때 폴더 목록을 서버에서 받아와 렌더링
	 * 
	 * @return 폴더 목록 List
	 */
	@GetMapping("/main")
	public String adminMainPage(Model model) {
		// 시험 종류, 시험 회차, 시험 과목 불러오기
//		model.addAttribute("examList", examService.getAllExamTitles());
		model.addAttribute("folderList", service.getFolderList()); 
		
		return "/admin/main";
	}
	
	/**
	 * 폴더 목록 페이지 보여줌 (비동기식 요청)
	 * - 클라이언트가 삭제 후 최신 폴더 목록을 다시 요청할 때 사용
	 * 
	 * @return JSON 형태의 폴더 목록 데이터
	 */
	@GetMapping("/folders")
	@ResponseBody
	public List<ExamFolderDto> getFolderList(){
		return service.getFolderList();
	}
	
	/**
	 * 현재 폴더를 제외한 나머지 폴더 목록 조회 (비동기 요청)
	 * - 클라이언트가 특정 폴더 선택 시, 해당 폴더를 제외한 목록을 받아올 때 사용
	 * 
	 * @param excludeFolderId 제외한 폴더 ID
	 * 
	 * @return JSON 형태의 폴더 목록 데이터
	 */
	@GetMapping("/getFolderListExcluding")
	@ResponseBody
	public List<ExamFolderDto> getFolderListExcluding(@RequestParam int excludeFolderId){
		return service.getFolderListExcluding(excludeFolderId);
	}
	
	/**
	 * 선택된 시험지를 지정된 폴더로 이동시킴
	 * 
	 * @param folderId 이동 대상 폴더의 ID
	 * @param examIds 이동할 시험지 ID 리스트
	 * 
	 * @return true/false 이동 성공 결과
	 */
	@PatchMapping("/moveExamsToFolder")
	@ResponseBody
	public boolean moveExamsToFolder(@RequestBody MoveExamsToFolderDto dto) {
		return service.moveExamsToFolder(dto);
	}
	
	/**
	 * 특정 폴더에 있는 모든 시험 제목 가져옴
	 * 
	 * @param folderId
	 * 
	 * @return List<ExamTitleDto>
	 */
	@GetMapping("/examList")
	@ResponseBody
	public List<ExamTitleDto> getExamList(@RequestParam int folderId){
		return examService.getAllExamTitlesByFolderId(folderId);
	}

	/**
	 * 관리자 시험 검토 페이지
	 * 
	 * @param examId 시험지ID (예: 1)
	 * @param examTypeEng 시험지 영어 타입 (예: 'middle-geomjeong')
	 * @param examTypeKor 시험지 한글 타입 (예: '중졸 검정고시')
	 * @param examRound 시험지 회차 (예: '2025년도 제1회')
	 * @param examSubject 시험 과목 (예: '국어')
	 * 
	 * @return ExamPageDto 시험정보 Dto
	 * 		(시험 문제들, 시험지 한글 타입, 시험지 회차, 시험 과목, 선택지, 중복제거된 공통지문 Dto)
	 */
	@GetMapping("/showExamPage")
	public String showExamPage(
		@RequestParam int examId,
		@RequestParam String examTypeEng,
		@RequestParam String examTypeKor,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		Model model) 
	{
		/* 정답지인 경우 */
		if(examTypeEng.endsWith("Answer")) {
			List<ExamAnswerDto> answers = answerService.getAnswers(examId);
			ExamFolderDto folderDto = service.getFolderInfoByExamId(examId);
			
			model.addAttribute("answers", answers);
			model.addAttribute("folderDto", folderDto);
			
			return "/admin/answer_page";
		}
		
		/* 시험지인 경우 */
		List<ExamQuestionDto> questions = examService.getExamQuestionsByExamId(examId);
		if(questions == null || questions.isEmpty()) {
			throw new IllegalStateException("해당 시험에 대한 문제가 존재하지 않습니다.");
		}
		
		List<ExamChoiceDto> choices = examService.getExamChoices(examId);
		Set<ExamPageDto.ExamCommonpassageDto> distinctPassageDto = 
				examService.getCommonPassageInfo(examTypeEng, examRound, examSubject); // 공통지문 시작번호 추출
		ExamFolderDto folderDto = service.getFolderInfoByExamId(examId);
		
		ExamPageDto response = 
				new ExamPageDto(questions, examTypeKor, examRound, examSubject, choices, distinctPassageDto);
		model.addAttribute("examPageDto", response);
		model.addAttribute("folderDto", folderDto);
		
		return "/admin/exam_page";
	}
	
	@GetMapping("/getExamImagePath")
	@ResponseBody
	public Resource getExamImagePath(
		@RequestParam String examType,
		@RequestParam String examRound,
		@RequestParam String examSubject,
		@RequestParam String filename)
	{
		String path = 
				config.getImageUploadPath() +
				examType + "\\" +
				examRound + "\\" +
				examSubject + "\\" +
				filename;
		// C:\server_program\project\testmate\images\2025년도 제1회\국어
		File file = new File(path);
		if (file.exists()) {
			return new FileSystemResource(file); // file자체를 보내는 것은 X, HTTP 본문 응답으로 자동 변환해주는 API(FileSystemResource())를 이용해서 보내야 함
		} else {
			throw new RuntimeException("File not found: " + file.getAbsolutePath());
		}
	}

	/**
	 * 새 폴더 생성
	 * 
	 * @param folderDto 폴더명 정보 Dto
	 * @return 저장 성공 여부(true/false)
	 */
	@PostMapping("/saveFolder")
	@ResponseBody
	public boolean saveFolder(@RequestBody ExamFolderDto folderDto) {
		boolean isExists = isFolderNameExists(folderDto.getFolderName());
		
		// 삭제 여부와 무관하게 존재하는 폴더명인지 확인
		if(isExists) {
			// 존재하는 폴더명이라면 삭제 여부 확인
			boolean isDeleted = service.isDeleted(folderDto.getFolderName());
			if(isDeleted) {
				// 삭제된 폴더라면 isDeleted 값을 'N'로 업데이트
				return service.restoreFolder(folderDto.getFolderName());
			} else {
				// 이미 존재하는 폴더인 경우 아무 작업하지 않음
				return false;
			}
		} 
		
		// 폴더명이 존재하지 않는 경우 새로 등록
		return service.saveFolder(folderDto.getFolderName()); 
	}

	/**
	 * 폴더 존재 여부 확인
	 * 
	 * @param folderName
	 * @return true/false
	 */
	public boolean isFolderNameExists(String folderName) {
		return service.isFolderNameExists(folderName);
	}
	
	/**
	 * 폴더 삭제 요청
	 * 
	 * @param folderId
	 * @return true/false 
	 */
	@DeleteMapping("/deleteFolder/{folderId}")
	@ResponseBody
	public boolean deleteFolder(@PathVariable int folderId) {
		try {
			service.deleteFolder(folderId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@GetMapping("/createExamPage")
	public String createExamPage(Model model, int folderId) {
		List<ExamTypeDto> list = examService.getExamTypes();
		model.addAttribute("examtypes", list);
		return "/admin/exam_create_page";
	}
	
}
