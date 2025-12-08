package com.example.backend.service;

import com.example.backend.model.api.ApiDataInterval;
import com.example.backend.model.api.ApiResponse;
import com.example.backend.model.api.GenerationMix;
import com.example.backend.model.dto.DailyEnergyMix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnergyMixServiceTest {
    @Mock
    private CarbonIntensityApiService carbonIntensityApiService;

    @InjectMocks
    private EnergyMixService energyMixService;

    @Test
    void shouldCalculateDailyAverageCorrectly(){
        LocalDateTime today = LocalDateTime.now(ZoneOffset.UTC).withHour(12).withMinute(0);

        ApiDataInterval interval1 = new ApiDataInterval();
        interval1.setFrom(today);
        interval1.setGenerationMix(List.of(
                createMix("solar", 10.0),
                createMix("gas", 90.0)
        ));

        ApiDataInterval interval2 = new ApiDataInterval();
        interval2.setFrom(today.plusMinutes(30));
        interval2.setGenerationMix(List.of(
                createMix("solar", 30.0),
                createMix("gas", 70.0)
        ));

        ApiResponse mockResponse = new ApiResponse();
        mockResponse.setData(List.of(interval1,interval2));
        when(carbonIntensityApiService.getGenerationMix(any(),any())).thenReturn(mockResponse);

        List<DailyEnergyMix> result = energyMixService.getDailyEnergyMix();
        assertNotNull(result);
        assertEquals(1,result.size());
        DailyEnergyMix dailyEnergyMix = result.get(0);
        assertEquals(20.0,dailyEnergyMix.getAverageSourcePercentages().get("solar"));
        assertEquals(80.0,dailyEnergyMix.getAverageSourcePercentages().get("gas"));
        assertEquals(20.0, dailyEnergyMix.getCleanEnergyPercentage());

    }
    @Test
    void shouldThrowExceptionForInvalidDuration() {
        assertThrows(IllegalStateException.class, () -> {
            energyMixService.findOptimalChargingWindow(0);
        });
        assertThrows(IllegalStateException.class,()->{
            energyMixService.findOptimalChargingWindow(7);
        });
    }
    private GenerationMix createMix(String fuel, Double percentage) {
        GenerationMix generationMix= new GenerationMix();
        generationMix.setFuel(fuel);
        generationMix.setPercentage(percentage);
        return generationMix;
    }
}
