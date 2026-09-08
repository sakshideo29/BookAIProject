package com.bookai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class PdfTextExtractionService {

    /**
     * Extracts all text content from an uploaded PDF file.
     * Note: this only handles text-based PDFs. Scanned/image-only PDFs
     * will return empty or near-empty text since there's no OCR step here.
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file must not be empty");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalStateException("PDF is encrypted/password-protected; cannot extract text");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            log.info("Extracted {} characters from PDF '{}' ({} pages)",
                    text.length(), file.getOriginalFilename(), document.getNumberOfPages());

            return text;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }
}