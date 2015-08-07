package com.bc.fiduceo.core;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Date;

public class SatelliteObservation {

    private Date startTime;
    private Date stopTime;
    private Geometry geometry;
    private Sensor sensor;
    private NodeType nodeType;

    public SatelliteObservation() {
        nodeType = NodeType.UNDEFINED;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
