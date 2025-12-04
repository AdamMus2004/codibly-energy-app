package com.example.backend.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class DailyEnergyMix {
    private LocalDate date;
    private Map<String, Double> averageSourcePercentages;
    private Double cleanEnergyPercentage;
}
