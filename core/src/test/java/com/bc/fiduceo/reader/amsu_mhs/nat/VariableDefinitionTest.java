package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableDefinitionTest {

    @Test
    public void testConstruction() {
        final VariableDefinition definition = new VariableDefinition();
        assertEquals(1.0, definition.getScale_factor(), 1e-8);
        assertEquals("", definition.getUnits());
        assertEquals("ARRAY", definition.getData_layout());
    }
}
