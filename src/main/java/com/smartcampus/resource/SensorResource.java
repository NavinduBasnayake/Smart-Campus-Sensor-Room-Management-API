/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.storage.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Navindu Basnayake
 */
@Path("/sensors")
public class SensorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(DataStore.sensors.values());
        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : allSensors) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return filtered;
        }
        return allSensors;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot create sensor: Room with ID '" +
                sensor.getRoomId() + "' does not exist."
            );
        }
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("Sensor with ID " + sensor.getId() +
                                   " already exists.").build();
        }
        DataStore.sensors.put(sensor.getId(), sensor);

        Room room = DataStore.rooms.get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        DataStore.sensorReadings.put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED)
                       .entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor with ID " + sensorId +
                                   " not found.").build();
        }
        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
