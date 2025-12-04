package com.example.backend.model.api;

import com.example.backend.model.util.EnergySource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenerationMix {
    @JsonProperty("fuel")
    private String fuel;
    @JsonProperty("perc")
    private Double percentage;
    private EnergySource energySource;

    public void setFuel(String fuel) {
        this.fuel = fuel;
        this.energySource = EnergySource.fromApiName(fuel);
    }
}
