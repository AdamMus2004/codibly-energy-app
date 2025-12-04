package com.example.backend.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ApiResponse {
    @JsonProperty("data")
    private List<ApiDataInterval> data;
}
