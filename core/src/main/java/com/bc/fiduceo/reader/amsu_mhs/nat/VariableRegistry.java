package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VariableRegistry {

    private Map<String, VariableDefinition> variables;

    public static VariableRegistry load(String resourceKey) {
        ObjectMapper mapper = new ObjectMapper();
        String path = resourceKey + "/variables.json";
        try (InputStream in = VariableRegistry.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            final VariableRegistry variableRegistry = mapper.readValue(in, VariableRegistry.class);
            variableRegistry.initialize();
            return variableRegistry;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load registry from: " + path, e);
        }
    }

    public void clear() {
        if (variables != null) {
            variables.clear();
        }
    }

    private void initialize() {
        final ArrayList<String> toBeDeleted = new ArrayList<>();
        final HashMap<String, VariableDefinition> toBeAdded = new HashMap<>();

        for (final String key: variables.keySet()) {
            if (key.contains("*")) {
                // expand to bands wise reading variable
                // and mark to be removed
                final VariableDefinition variableDefinition = variables.get(key);
                toBeAdded.putAll(expandWildcardVariable(key, variableDefinition));
                toBeDeleted.add(key);
            }
        }

        for (String key: toBeDeleted) {
            variables.remove(key);
        }
        variables.putAll(toBeAdded);
    }

    private Map<String, VariableDefinition> expandWildcardVariable(String key, VariableDefinition variableDefinition) {
        final Map<String, VariableDefinition> expanded = new HashMap<>();
        final int stride = variableDefinition.getStride();
        final int initialOffset = variableDefinition.getOffset();

        for (int offset = 0; offset < stride; offset++) {
            final VariableDefinition layerVariableDefinition = new VariableDefinition();
            layerVariableDefinition.setOffset(initialOffset + 4 * offset);
            layerVariableDefinition.setStride(stride);
            layerVariableDefinition.setScale_factor(variableDefinition.getScale_factor());
            layerVariableDefinition.setData_type(variableDefinition.getData_type());
            layerVariableDefinition.setUnits(variableDefinition.getUnits());
            layerVariableDefinition.setStandard_name(variableDefinition.getStandard_name());

            final String layerKey = buildLayerKey(key, offset);
            expanded.put(layerKey, layerVariableDefinition);
        }

        return expanded;
    }

    private String buildLayerKey(String key, int offset) {
        String bandNumber = String.format("%02d", offset + 1);
        return key.replace("*", bandNumber);
    }

    public Map<String, VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, VariableDefinition> variables) {
        this.variables = variables;
    }

    public VariableDefinition getVariableDef(String variableName) {
        VariableDefinition def = variables.get(variableName);
        if (def == null) {
            throw new IllegalArgumentException("Variable not defined: " + variableName);
        }
        return def;
    }
}
