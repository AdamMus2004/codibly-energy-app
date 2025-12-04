package com.example.backend.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OptimalChargingWindow {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double averageCleanEnergyPercentage;

}
