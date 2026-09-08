package com.bookai.dto;

import lombok.Data;

@Data
public class IngestRequest {
    private String source = "manual";
    private String text;
    private Integer chunkSize = 500;
}
