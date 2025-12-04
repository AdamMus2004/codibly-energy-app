package com.example.backend.model.util;

public enum EnergySource {
    BIOMASS(true),
    NUCLEAR(true),
    HYDRO(true),
    WIND(true),
    SOLAR(true),
    GAS(false),
    COAL(false),
    IMPORTS(false),
    OIL(false),
    OTHER(false),
    UNKNOWN(false);

    private final boolean isClean;

    EnergySource(boolean isClean) {
        this.isClean = isClean;
    }
    public static EnergySource fromApiName(String apiName) {
        try {
            return EnergySource.valueOf(apiName.toUpperCase());
        } catch (IllegalArgumentException e) {
            if ("other".equalsIgnoreCase(apiName)) {
                return OTHER;
            }
            return UNKNOWN;
        }
    }
}
