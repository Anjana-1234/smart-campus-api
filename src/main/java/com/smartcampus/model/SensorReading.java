package com.smartcampus.model;

public class SensorReading {

    private String id;
    private long timestamp;
    private double value;
    private String unit;

    public SensorReading() {}

    public SensorReading(String id, long timestamp, double value, String unit) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
        this.unit = unit;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}