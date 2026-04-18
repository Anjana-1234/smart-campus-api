package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /sensors/{id}/readings — get all readings for a sensor
    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Sensor not found"))
                    .build();
        }
        List<SensorReading> history = DataStore.readings
                .getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    // POST /sensors/{id}/readings — add a new reading
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Sensor not found"))
                    .build();
        }

        // Block readings for MAINTENANCE sensors — gives 403
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            return Response.status(403)
                    .entity(Map.of(
                        "error", "Forbidden",
                        "message", "Sensor " + sensorId
                            + " is under maintenance. Cannot accept readings."
                    ))
                    .build();
        }

        // Auto-generate ID and timestamp
        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // Save the reading
        DataStore.readings
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // Update parent sensor's currentValue — Part 4.2 requirement
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).entity(reading).build();
    }
}