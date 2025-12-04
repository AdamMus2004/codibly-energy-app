package com.example.backend.service;

import com.example.backend.model.api.ApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CarbonIntensityApiService {
    private static final String API_BASE_URL = "https://api.carbonintensity.org.uk";

    private final RestTemplate restTemplate;

    public CarbonIntensityApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ApiResponse getGenerationMix(LocalDateTime fromDateTime,LocalDateTime toDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'");
        String from = fromDateTime.format(formatter);
        String to = toDateTime.format(formatter);

        String url = String.format("%s/generation/%s/%s",API_BASE_URL,from,to);

        try {
            return restTemplate.getForObject(url, ApiResponse.class);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ApiResponse();
        }
    }
}
