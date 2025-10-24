package com.bc.fiduceo.reader.insitu.generic;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericRecord {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public void put(String variableName, Object value) {
        values.put(variableName, value);
    }

    public Object get(String variableName) {
        return values.get(variableName);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
