package com.bc.fiduceo.reader.insitu.generic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.esa.snap.core.datamodel.ProductData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CsvFormatConfig {

    private String name;
    private String delimiter;
    private char commentChar;
    private String regex;
    private String longitudeName;
    private String latitudeName;
    private String timeName = "time";
    private String timeFormat;
    private List<String> timeVars;
    private boolean locationFromStationDatabase;
    private List<GenericVariable> variables;
    private StationDatabase stationDatabase;

    public static CsvFormatConfig loadConfig(String resourceKey) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        String path = resourceKey + "_config.json";

        try (InputStream in = CsvFormatConfig.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + path);
            }

            return mapper.readValue(in, CsvFormatConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from: " + path, e);
        }
    }

    public List<GenericVariable> getAllVariables() {
        List<GenericVariable> vars = new ArrayList<>();

        if (stationDatabase != null) {
            vars.addAll(stationDatabase.getVariables());
        }

        if (hasTimeVars()) {
            GenericVariable time = new GenericVariable(timeName, 'v',"int", ProductData.TYPE_INT64, -1.0, "seconds since 1970-01-01", null, "time", null);
            vars.add(time);

            for (GenericVariable var : variables) {
                if (!timeVars.contains(var.getName())) {
                    vars.add(var);
                }
            }
        } else {
            vars.addAll(variables);
        }

        return vars;
    }

    private boolean hasTimeVars() {
        return timeVars != null && !timeVars.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public char getCommentChar() {
        return commentChar;
    }

    public void setCommentChar(char commentChar) {
        this.commentChar = commentChar;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getLongitudeName() {
        return longitudeName;
    }

    public void setLongitudeName(String longitudeName) {
        this.longitudeName = longitudeName;
    }

    public String getLatitudeName() {
        return latitudeName;
    }

    public void setLatitudeName(String latitudeName) {
        this.latitudeName = latitudeName;
    }

    public String getTimeName() {
        return timeName;
    }

    public void setTimeName(String timeName) {
        this.timeName = timeName;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public List<String> getTimeVars() {
        return timeVars;
    }

    public void setTimeVars(List<String> timeVars) {
        this.timeVars = timeVars;
    }

    public boolean isLocationFromStationDatabase() {
        return locationFromStationDatabase;
    }

    public void setLocationFromStationDatabase(boolean locationFromStationDatabase) {
        this.locationFromStationDatabase = locationFromStationDatabase;
    }

    public List<GenericVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<GenericVariable> variables) {
        this.variables = variables;
    }

    public StationDatabase getStationDatabase() {
        return stationDatabase;
    }

    public void setStationDatabase(StationDatabase stationDatabase) {
        this.stationDatabase = stationDatabase;
    }
}
