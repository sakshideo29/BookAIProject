package com.bookai.controller;

import com.bookai.dto.AskRequest;
import com.bookai.dto.AskResponse;
import com.bookai.dto.DocumentChunk;
import com.bookai.dto.QueryRequest;
import com.bookai.service.RagAnswerService;
import com.bookai.service.RetrieverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RetrieverService retrieverService;
    private final RagAnswerService ragAnswerService;

    @PostMapping("/query")
    public ResponseEntity<List<DocumentChunk>> query(@RequestBody QueryRequest request) {
        int k = request.getK() == null ? 5 : request.getK();
        List<DocumentChunk> results = retrieverService.retrieve(request.getQuery(), k);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) {
        int k = request.getK() == null ? 5 : request.getK();
        String answer = ragAnswerService.answer(request.getQuery(), k);
        return ResponseEntity.ok(new AskResponse(request.getQuery(), answer));
    }
}