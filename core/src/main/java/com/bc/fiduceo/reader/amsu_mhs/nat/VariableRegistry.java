package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VariableRegistry {

    private List<VariableDefinition> variables;

    public static VariableRegistry load(String resourceKey) {
        ObjectMapper mapper = new ObjectMapper();
        String path = resourceKey + "/variables.json";
        try (InputStream in = VariableRegistry.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return mapper.readValue(in, VariableRegistry.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load registry from: " + path, e);
        }
    }

    public List<VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableDefinition> variables) {
        this.variables = variables;
    }
}
