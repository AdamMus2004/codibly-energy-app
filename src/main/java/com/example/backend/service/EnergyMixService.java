package com.example.backend.service;

import com.example.backend.model.api.ApiDataInterval;
import com.example.backend.model.api.GenerationMix;
import com.example.backend.model.dto.OptimalChargingWindow;
import com.example.backend.model.util.EnergySource;
import com.example.backend.model.dto.DailyEnergyMix;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnergyMixService {
    private final CarbonIntensityApiService apiService;

    public EnergyMixService(CarbonIntensityApiService apiService) {
        this.apiService = apiService;
    }

    private double getCleanEnergyPercentageForInterval(ApiDataInterval interval) {
        return interval.getGenerationMix()
                .stream()
                .filter(mix -> mix.getEnergySource().isClean())
                .mapToDouble(GenerationMix::getPercentage)
                .sum();
    }
    private double calculateCleanEnergyPercentages(Map<String,Double> averagePercentage) {
        return averagePercentage.entrySet()
                .stream()
                .filter(entry -> EnergySource.fromApiName(entry.getKey()).isClean())
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }
    private Map<String,Double> calculateAveragePercentages(List<ApiDataInterval> dailyIntervals) {
        Map<String,List<Double>> percentageByFuel = dailyIntervals
                .stream()
                .flatMap(interval -> interval.getGenerationMix().stream())
                .collect(Collectors.groupingBy(
                        GenerationMix::getFuel,
                        Collectors.mapping(GenerationMix::getPercentage, Collectors.toList())
                ));
        return percentageByFuel.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                ));
    }

    public List<DailyEnergyMix> getDailyEnergyMix() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime from = nowUtc.withMinute(nowUtc.getMinute() < 30 ? 0 : 30).withSecond(0).withNano(0);
        LocalDateTime to = LocalDate.now(ZoneOffset.UTC).plusDays(3).atStartOfDay();

        List<ApiDataInterval> allData = apiService.getGenerationMix(from,to).getData();

        if (allData == null || allData.isEmpty()) {
            return List.of();
        }
        Map<LocalDate,List<ApiDataInterval>> dataByDay = allData
                .stream()
                .collect(Collectors.groupingBy(interval -> interval.getFrom().toLocalDate()));

        return dataByDay.entrySet()
                .stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<ApiDataInterval> dailyIntervals = entry.getValue();

                    Map<String,Double> averagePercentages = calculateAveragePercentages(dailyIntervals);
                    double cleanEnergyPercentage = calculateCleanEnergyPercentages(averagePercentages);

                    return DailyEnergyMix.builder()
                            .date(date)
                            .averageSourcePercentages(averagePercentages)
                            .cleanEnergyPercentage(cleanEnergyPercentage)
                            .build();
                })
                .sorted(Comparator.comparing(DailyEnergyMix::getDate))
                .collect(Collectors.toList());

    }
    public OptimalChargingWindow findOptimalChargingWindow(int chargingHours) {
        if (chargingHours < 1 || chargingHours > 6) {
            throw new IllegalStateException("Charging time must be between 1 and 6 hours.");
        }
        LocalDateTime startOfTomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay();
        LocalDateTime endOfForecast = LocalDate.now(ZoneOffset.UTC).plusDays(3).atStartOfDay();

        List<ApiDataInterval> allForecastData = apiService.getGenerationMix(startOfTomorrow,endOfForecast).getData();

        if (allForecastData == null || allForecastData.isEmpty()) {
            throw new IllegalStateException("Failed to retrieve forecast data for load optimization.");
        }

        int requiredIntervals = chargingHours * 2;

        if (allForecastData.size()<requiredIntervals) {
            throw new IllegalStateException("Too little forecast data.");
        }

        double maxCleanEnergy = -1.0;
        int optimalStartIndex = 1;

        for (int i =0; i<= allForecastData.size() - requiredIntervals; i++) {
            List<ApiDataInterval> currentWindow = allForecastData.subList(i,i+requiredIntervals);

            double currentCleanEnergy = currentWindow.stream()
                    .mapToDouble(this::getCleanEnergyPercentageForInterval)
                    .average()
                    .orElse(0.0);

            if (currentCleanEnergy>maxCleanEnergy) {
                maxCleanEnergy=currentCleanEnergy;
                optimalStartIndex = i;
            }
        }
        if (optimalStartIndex == -1) {
            throw new IllegalStateException("Could not find optimal loading window.");
        }

        ApiDataInterval startInterval = allForecastData.get(optimalStartIndex);
        ApiDataInterval endInterval = allForecastData.get(optimalStartIndex + requiredIntervals -1);

        return OptimalChargingWindow.builder()
                .startDateTime(startInterval.getFrom())
                .endDateTime(endInterval.getFrom().plusMinutes(30))

                .averageCleanEnergyPercentage(maxCleanEnergy)
                .build();
    }
}
