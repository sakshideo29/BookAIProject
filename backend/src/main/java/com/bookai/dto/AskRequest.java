package com.bookai.dto;

import lombok.Data;

@Data
public class AskRequest {
    private String query;
    private Integer k = 5;
}
