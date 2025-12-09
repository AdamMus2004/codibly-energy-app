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
import static org.mockito.Mockito.*;

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
    @Test
    void shouldFindOptimalChargingWindow() {
        LocalDateTime tomorowStart = LocalDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(0).withMinute(0);
        ApiDataInterval bad1 = createInterval(tomorowStart, 0.0);
        ApiDataInterval bad2 = createInterval(tomorowStart.plusMinutes(30), 0.0);
        ApiDataInterval bad3 = createInterval(tomorowStart.plusHours(2), 0.0);
        ApiDataInterval bad4 = createInterval(tomorowStart.plusHours(2).plusMinutes(30), 0.0);

        ApiDataInterval good1 = createInterval(tomorowStart.plusHours(1), 100.0);
        ApiDataInterval good2 = createInterval(tomorowStart.plusHours(1).plusMinutes(30),100.0);

        ApiResponse mockResponse = new ApiResponse();
        mockResponse.setData(List.of(bad1,bad2,bad3,bad4,good1,good2));

        when(carbonIntensityApiService.getGenerationMix(any(),any())).thenReturn(mockResponse);

        var window = energyMixService.findOptimalChargingWindow(1);

        assertEquals(tomorowStart.plusHours(1),window.getStartDateTime());
        assertEquals(100.0, window.getAverageCleanEnergyPercentage());

    }

    private GenerationMix createMix(String fuel, Double percentage) {
        GenerationMix generationMix= new GenerationMix();
        generationMix.setFuel(fuel);
        generationMix.setPercentage(percentage);
        return generationMix;
    }
    private ApiDataInterval createInterval(LocalDateTime time,double cleanPercentage){
        ApiDataInterval interval = new ApiDataInterval();
        interval.setFrom(time);
        interval.setGenerationMix(List.of(
                createMix("solar",cleanPercentage),
                createMix("gas",100.0-cleanPercentage)
        ));
        return interval;
    }
}
