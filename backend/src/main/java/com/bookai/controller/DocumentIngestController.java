package com.bookai.controller;

import com.bookai.dto.IngestRequest;
import com.bookai.service.IngestionService;
import com.bookai.service.PdfTextExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/docs", "/docs"})
@RequiredArgsConstructor
public class DocumentIngestController {

    private final IngestionService ingestionService;
    private final PdfTextExtractionService pdfTextExtractionService;

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody IngestRequest request) {
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text must not be empty"));
        }

        String source = request.getSource() == null || request.getSource().isBlank() ? "manual" : request.getSource();
        int chunkSize = request.getChunkSize() == null || request.getChunkSize() <= 0 ? 500 : request.getChunkSize();

        int stored = ingestionService.chunkEmbedAndStore(request.getText(), source, chunkSize);

        Map<String, Object> response = new HashMap<>();
        response.put("source", source);
        response.put("chunks", stored);
        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/ingest-pdf", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> ingestPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "chunkSize", required = false) Integer chunkSize) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "file must not be empty"));
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean looksLikePdf = (contentType != null && contentType.equals("application/pdf"))
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));
        if (!looksLikePdf) {
            return ResponseEntity.badRequest().body(Map.of("error", "file must be a PDF (application/pdf)"));
        }

        String resolvedSource = (source == null || source.isBlank())
                ? file.getOriginalFilename()
                : source;
        int resolvedChunkSize = (chunkSize == null || chunkSize <= 0) ? 500 : chunkSize;

        String text = pdfTextExtractionService.extractText(file);

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No extractable text found in PDF (it may be a scanned/image-only PDF requiring OCR)"
            ));
        }

        int stored = ingestionService.chunkEmbedAndStore(text, resolvedSource, resolvedChunkSize);

        Map<String, Object> response = new HashMap<>();
        response.put("source", resolvedSource);
        response.put("chunks", stored);
        response.put("extractedChars", text.length());
        response.put("status", "ok");
        return ResponseEntity.ok(response);
    }
}