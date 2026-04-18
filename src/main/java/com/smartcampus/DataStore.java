package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // All rooms stored here — key is room ID
    public static final Map<String, Room> rooms
            = new ConcurrentHashMap<>();

    // All sensors stored here — key is sensor ID
    public static final Map<String, Sensor> sensors
            = new ConcurrentHashMap<>();

    // All readings stored here — key is sensor ID
    public static final Map<String, List<SensorReading>> readings
            = new ConcurrentHashMap<>();
}