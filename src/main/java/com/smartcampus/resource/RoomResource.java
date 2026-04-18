package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.model.Room;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // GET /rooms — return all rooms
    @GET
    public Response getAllRooms() {
        return Response.ok(
            new ArrayList<>(DataStore.rooms.values())
        ).build();
    }

    // POST /rooms — create a new room
    @POST
    public Response createRoom(Room room) {
        // Validate required fields
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                    .entity(Map.of("error", "Room ID is required"))
                    .build();
        }
        // Check duplicate
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409)
                    .entity(Map.of("error", "Room with this ID already exists"))
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);

        // 201 Created + Location header (rubric requires this in video!)
        return Response.status(201)
                .entity(room)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .build();
    }

    // GET /rooms/{id} — get one room by ID
    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = DataStore.rooms.get(id);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Room not found"))
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE /rooms/{id} — delete a room
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = DataStore.rooms.get(id);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("error", "Room not found"))
                    .build();
        }
        // Cannot delete if sensors are still assigned — Part 2.2
        if (!room.getSensorIds().isEmpty()) {
            return Response.status(409)
                    .entity(Map.of(
                        "error", "Conflict",
                        "message", "Cannot delete room. It still has "
                            + room.getSensorIds().size()
                            + " active sensor(s). Remove sensors first."
                    ))
                    .build();
        }
        DataStore.rooms.remove(id);
        return Response.noContent().build(); // 204
    }
}