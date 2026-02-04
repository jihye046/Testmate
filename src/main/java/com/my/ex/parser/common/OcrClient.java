package com.my.ex.parser.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.protobuf.ByteString;

// Google Cloud Vision API
public class OcrClient {
	
	private static final String CREDENTIALS_FILE_PATH = "src/main/resources/config/testmate-ocr-key.json";
	
	// json키 인증 로직
	private static ImageAnnotatorClient createAnnotatorClient() throws IOException {
		// 1. JSON 키 파일의 스트림을 로드 (프로젝트 시작 경로 기준)
        FileInputStream serviceAccountStream = new FileInputStream(CREDENTIALS_FILE_PATH);
        
        // 2. 스트림에서 인증 정보 객체를 생성
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
        
        // 3. 인증 정보를 설정하여 클라이언트 객체를 생성하고 반환
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider(() -> credentials) // Credential Provider 설정
            .build();
            
        // settings 객체를 사용하여 클라이언트 생성
        return ImageAnnotatorClient.create(settings); 
	}
	
	/**
     * @param imagePath OCR을 수행할 이미지 파일의 로컬 경로
     * @return 추출된 전체 텍스트 문자열
     */
	// 주어진 이미지 경로로부터 OCR (DOCUMENT_TEXT_DETECTION)을 수행하고 전체 텍스트 반환 로직
	public static String extractTextFromImage(String imagePath) throws Exception {
		try(ImageAnnotatorClient vision = createAnnotatorClient()) {
			
			// 1. 이미지 파일 로드
			Path path = Path.of(imagePath);
			if(!Files.exists(path)) {
				throw new IllegalArgumentException("이미지 파일 경로가 유효하지 않습니다: " + imagePath);
			}
			
			// 파일을 바이트 스트링으로 변환
			ByteString imgBytes = ByteString.readFrom(Files.newInputStream(path));
			Image img = Image.newBuilder().setContent(imgBytes).build();
			
			// 2. OCR 기능 (DOCUMENT_TEXT_DETECTION) 요청 설정
			Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
			
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
					.addFeatures(feature)
					.setImage(img)
					.build();
			
			// API 요청 리스트 생성
			List<AnnotateImageRequest> requests = List.of(request);
			
			// 3. API 호출
			List<AnnotateImageResponse> response = vision.batchAnnotateImages(requests).getResponsesList();
			
			// 4. 결과 파싱 및 반환
			if(response.isEmpty() || !response.get(0).hasFullTextAnnotation()) {
				return "OCR 텍스트를 추출하지 못했습니다.";
			}
			
			// 추출된 전체 텍스트를 반환
			return response.get(0).getFullTextAnnotation().getText();
			
		} catch (Exception e) {
			throw new Exception("Vision API 클라이언트 생성 또는 호출 중 오류 발생: " + e.getMessage(), e);
		}
		
	}
	
	public static void main(String[] args) {
		String imagePath = "C:\\Users\\jihye\\Downloads\\ocr테스트.png";
		
		try {
			System.out.println("--- OCR 추출 시작 ---");
            System.out.println("요청 파일: " + imagePath);
			String extractedText = extractTextFromImage(imagePath);
			
			System.out.println("\n✅ 추출 완료된 텍스트:\n");
            System.out.println("==========================================");
            System.out.println(extractedText);
            System.out.println("==========================================");
		} catch (IllegalArgumentException e) {
			System.out.println("❌ 파일 경로 오류: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("❌ OCR 서비스 오류: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
