/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.storage.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Navindu Basnayake
 */
public class SensorReadingResource {

    private String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor with ID " + sensorId +
                                   " not found.").build();
        }
        List<SensorReading> readings = DataStore.sensorReadings.get(sensorId);
        return Response.ok(readings != null ? readings :
                           new ArrayList<>()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor with ID " + sensorId +
                                   " not found.").build();
        }
        if ("MAINTENANCE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor " + sensorId +
                " is under MAINTENANCE and cannot accept readings."
            );
        }
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading = new SensorReading(reading.getValue());
        }
        DataStore.sensorReadings
            .computeIfAbsent(sensorId, k -> new ArrayList<>())
            .add(reading);

        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED)
                       .entity(reading).build();
    }
}

