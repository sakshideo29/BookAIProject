package com.bookai.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DocumentChunk {
    private UUID id;
    private String source; // source file identifier
    private int chunkIndex;
    private String content;
    private String contentHash; // dedupe hash
    private Map<String, Object> metadata;
    private double[] embedding;
}
