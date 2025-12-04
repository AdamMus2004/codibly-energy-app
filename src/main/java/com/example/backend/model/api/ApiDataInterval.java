package com.example.backend.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiDataInterval {
    @JsonProperty("from")
    private LocalDateTime from;
    @JsonProperty("to")
    private LocalDateTime to;
    @JsonProperty("generationmix")
    private List<GenerationMix> generationMix;

}
