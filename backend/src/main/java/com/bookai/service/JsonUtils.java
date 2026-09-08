package com.bookai.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper M = new ObjectMapper();

    public static String toJson(Object o) {
        try {
            return M.writeValueAsString(o == null ? new Object() : o);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}
