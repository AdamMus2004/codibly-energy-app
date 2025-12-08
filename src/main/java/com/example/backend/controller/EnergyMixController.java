package com.example.backend.controller;

import com.example.backend.model.dto.DailyEnergyMix;
import com.example.backend.model.dto.OptimalChargingWindow;
import com.example.backend.service.EnergyMixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energy-mix")
public class EnergyMixController {
    private final EnergyMixService energyMixService;

    public EnergyMixController(EnergyMixService energyMixService) {
        this.energyMixService = energyMixService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyEnergyMix>> getDailyMixSummary() {
        List<DailyEnergyMix> summary = energyMixService.getDailyEnergyMix();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/optimal-window")
    public ResponseEntity<OptimalChargingWindow> getOptimalChargingWindow(@RequestParam(name = "hours") int hours) {
        try {
            OptimalChargingWindow window = energyMixService.findOptimalChargingWindow(hours);
            return ResponseEntity.ok(window);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).build();
        }
    }

}
