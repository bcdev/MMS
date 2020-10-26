package com.bc.fiduceo.reader.slstr;

import org.junit.Before;
import org.junit.Test;

import static com.bc.fiduceo.reader.slstr.VariableType.Type.*;
import static org.junit.Assert.*;

public class VariableFactoryTest {

    private VariableFactory variableFactory;

    @Before
    public void setUp() {
        variableFactory = new VariableFactory();
    }

    @Test
    public void testIsValidName() {
        assertTrue(variableFactory.isValidName("S2_exception_an"));
        assertTrue(variableFactory.isValidName("S8_BT_in"));

        assertFalse(variableFactory.isValidName("Heffalump"));
        assertFalse(variableFactory.isValidName("time_cn"));
    }

    @Test
    public void testGetVariableType() {
        VariableType type = variableFactory.getVariableType("longitude_tx");
        assertEquals(NADIR_500m, type.getType());
        assertTrue(type.isTiePoint());

        type = variableFactory.getVariableType("solar_azimuth_tn");
        assertEquals(NADIR_500m, type.getType());
        assertTrue(type.isTiePoint());

        type = variableFactory.getVariableType("solar_zenith_to");
        assertTrue(type.isTiePoint());
        assertEquals(NADIR_500m, type.getType());

        type = variableFactory.getVariableType("S7_BT_in");
        assertEquals(NADIR_1km, type.getType());
        assertFalse(type.isTiePoint());

        type = variableFactory.getVariableType("S9_exception_in");
        assertEquals(NADIR_1km, type.getType());
        assertFalse(type.isTiePoint());

        type = variableFactory.getVariableType("S4_radiance_ao");
        assertEquals(OBLIQUE_500m, type.getType());
        assertFalse(type.isTiePoint());

        type = variableFactory.getVariableType("S4_exception_ao");
        assertEquals(OBLIQUE_500m, type.getType());
        assertFalse(type.isTiePoint());

        type = variableFactory.getVariableType("S9_BT_io");
        assertEquals(OBLIQUE_1km, type.getType());
        assertFalse(type.isTiePoint());

        type = variableFactory.getVariableType("bayes_io");
        assertEquals(OBLIQUE_1km, type.getType());
        assertFalse(type.isTiePoint());
    }

    @Test
    public void testGetVariableType_invalidName() {
        try {
            variableFactory.getVariableType("Trump");
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIsFlagVariable() {
        assertTrue(variableFactory.isFlagVariable("S6_exception_an"));
        assertTrue(variableFactory.isFlagVariable("S7_exception_io"));
        assertTrue(variableFactory.isFlagVariable("pointing_in"));

        assertFalse(variableFactory.isFlagVariable("solar_zenith_to"));
        assertFalse(variableFactory.isFlagVariable("S8_BT_in"));
    }

    @Test
    public void testIsTiePointVariable() {
        assertTrue(variableFactory.isTiePointVariable("latitude_tx"));
        assertTrue(variableFactory.isTiePointVariable("solar_azimuth_to"));

        assertFalse(variableFactory.isTiePointVariable("S4_exception_ao"));
        assertFalse(variableFactory.isTiePointVariable("S5_radiance_ao"));
    }
}
