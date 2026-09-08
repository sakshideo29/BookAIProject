package com.bookai.dto;

import lombok.Data;

@Data
public class QueryRequest {
    private String query;
    private Integer k = 5;
}
