package com.bc.fiduceo.reader.insitu.generic;

import java.util.List;

public class StationDatabase {

    private String primaryId;
    private String secondaryId;
    private List<GenericVariable> variables;
    private List<List<Object>> stations;


    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(String secondaryId) {
        this.secondaryId = secondaryId;
    }

    public List<GenericVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<GenericVariable> variables) {
        this.variables = variables;
    }

    public List<List<Object>> getStations() {
        return stations;
    }

    public void setStations(List<List<Object>> stations) {
        this.stations = stations;
    }

    public GenericRecord extractRecord(String primaryIdValue, String secondaryIdValue) {
        int primaryIndex = -1;
        int secondaryIndex = -1;
        for (int ii = 0; ii < variables.size(); ii++) {
            final GenericVariable var = variables.get(ii);
            if (var.getName().equals(primaryId)) {
                primaryIndex = ii;
            }
            if (var.getName().equals(secondaryId)) {
                secondaryIndex = ii;
            }
        }

        if (primaryIndex == -1) {
            throw new IllegalStateException("Station database does not contain variable '" + primaryId + "'.");
        }
        if (secondaryId != null && secondaryIndex == -1) {
            throw new IllegalStateException("Station database does not contain variable '" + secondaryId + "'.");
        }

        List<Object> desiredStation = extractStation(primaryIndex, primaryIdValue, secondaryIndex, secondaryIdValue);

        if (desiredStation == null) {
            throw new IllegalStateException("Station database does not contain site/station with ids '" + primaryId + "'/'" + secondaryId + ".");
        }

        GenericRecord resultRecord = new GenericRecord();
        for (int ii = 0; ii < desiredStation.size(); ii++) {
            GenericVariable var = variables.get(ii);
            Object value = desiredStation.get(ii);

            String token = (value == null) ? null : String.valueOf(value);
            Object parsedValue = GenericCsvHelper.parseValue(token, var.getType());

            resultRecord.put(var.getName(), parsedValue);
        }

        return resultRecord;
    }

    private List<Object> extractStation(int primaryIndex, String primaryIdValue, int secondaryIndex, String secondaryIdValue) {
        List<Object> desiredStation = null;
        for (int ii = 0; ii < stations.size(); ii++) {
            final List<Object> station = stations.get(ii);
            boolean primaryFound = false;
            boolean secondaryFound = false;

            String siteId = (String) station.get(primaryIndex);
            String stationId = null;
            if (secondaryId != null) {
                stationId = (String) station.get(secondaryIndex);
            } else {
                secondaryFound = true;
            }

            if (siteId != null && siteId.equals(primaryIdValue)) {
                primaryFound = true;
            }
            if (secondaryId != null && stationId != null && stationId.equals(secondaryIdValue)) {
                secondaryFound = true;
            }

            if (primaryFound && secondaryFound) {
                desiredStation = station;
                break;
            }
        }
        return desiredStation;
    }

}
