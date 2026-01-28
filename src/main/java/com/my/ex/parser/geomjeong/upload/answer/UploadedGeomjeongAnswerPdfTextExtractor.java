package com.my.ex.parser.geomjeong.upload.answer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadedGeomjeongAnswerPdfTextExtractor {
	
	public String extract(MultipartFile file) throws IOException {
		try (InputStream is = file.getInputStream();
			PDDocument document = PDDocument.load(is)) {
			
			PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);
            return text.trim();
		} 
	}
}
