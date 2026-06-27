package com.learning.ai_learning;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TruckDataService {

    private static final Map<String, TruckStatus> FLEET_DATA = Map.of(
            "CAT-793-007", new TruckStatus("CAT-793-007", "ACTIVE", 45.2, 35, 67, "Haul Route A"),
            "CAT-793-008", new TruckStatus("CAT-793-008", "MAINTENANCE", 0.0, 0, 45, "Workshop Bay 3"),
            "CAT-785-001", new TruckStatus("CAT-785-001", "IDLE", 0.0, 0, 89, "Loading Zone 2"),
            "CAT-789-003", new TruckStatus("CAT-789-003", "ACTIVE", 38.7, 28, 52, "Haul Route B")
    );
    private static final Logger log = LogManager.getLogger(TruckDataService.class);

    @Tool(description = "Get the current real-time status of a mining truck by its ID. " +
            "Returns status, payload, speed, fuel level and current location.")
    public TruckStatus getTruckStatus(String truckId) {
        log.info("Tool called: getTruckStatus({})", truckId);
        TruckStatus status = FLEET_DATA.get(truckId.toUpperCase());
        if (status == null) {
            return new TruckStatus(truckId, "NOT_FOUND", 0, 0, 0, "Unknown");
        }
        return status;
    }

    @Tool(description = "Get fuel level percentage for a specific mining truck. " +
            "Use this when operator asks about fuel status or needs refuelling decision.")
    public String getFuelLevel(String truckId) {
        log.info("Tool called: getFuelLevel({})", truckId);
        TruckStatus status = FLEET_DATA.get(truckId.toUpperCase());
        if (status == null) return "Truck " + truckId + " not found in fleet.";
        String alert = status.fuelPercent() < 50 ? " - REFUEL RECOMMENDED" : " - Fuel OK";
        return "Truck " + truckId + " fuel level: " + status.fuelPercent() + "%" + alert;
    }

    @Tool(description = "Create a maintenance ticket for a mining truck that needs repair or service. " +
            "Use when operator reports a problem or requests maintenance.")
    public String createMaintenanceTicket(String truckId, String issue, String priority) {
        log.info("Tool called: createMaintenanceTicket({}, {}, {})", truckId, issue, priority);
        String ticketId = "TKT-" + System.currentTimeMillis() % 10000;
        return String.format(
                "Maintenance ticket %s created for truck %s. Issue: %s. Priority: %s. " +
                        "Assigned to Workshop Team. Expected resolution: 4 hours.",
                ticketId, truckId, issue, priority
        );
    }

    @Tool(description = "Get a summary of all active trucks in the fleet and their current status.")
    public String getFleetSummary() {
        log.info("Tool called: getFleetSummary()");
        long active = FLEET_DATA.values().stream()
                .filter(t -> "ACTIVE".equals(t.status())).count();
        long idle = FLEET_DATA.values().stream()
                .filter(t -> "IDLE".equals(t.status())).count();
        long maintenance = FLEET_DATA.values().stream()
                .filter(t -> "MAINTENANCE".equals(t.status())).count();
        return String.format(
                "Fleet Summary: %d Active, %d Idle, %d In Maintenance. Total: %d trucks.",
                active, idle, maintenance, FLEET_DATA.size()
        );
    }
}
