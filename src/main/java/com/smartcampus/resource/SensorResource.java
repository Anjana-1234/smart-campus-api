package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // GET /sensors?type=CO2 — get all, optional filter by type
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());

        if (type != null && !type.isEmpty()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }

    // POST /sensors — register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getRoomId() == null
                || !DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new com.smartcampus.exception.LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist."
            );
        }
        DataStore.sensors.put(sensor.getId(), sensor);
        DataStore.rooms.get(sensor.getRoomId())
            .getSensorIds().add(sensor.getId());
        DataStore.readings.put(sensor.getId(), new ArrayList<>());
        return Response.status(201).entity(sensor).build();
}

    // GET /sensors/{id} — get one sensor
    @GET
    @Path("/{id}")
    public Response getSensor(@PathParam("id") String id) {
        Sensor sensor = DataStore.sensors.get(id);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Sensor not found"))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator — Part 4 (key pattern for marks!)
    @Path("/{id}/readings")
    public SensorReadingResource getReadings(
            @PathParam("id") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}