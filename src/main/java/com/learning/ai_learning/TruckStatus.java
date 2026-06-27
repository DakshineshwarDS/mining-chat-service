package com.learning.ai_learning;

public record TruckStatus(
        String truckId,
        String status,
        double payloadTonnes,
        int speedKmh,
        int fuelPercent,
        String location
) {
}
