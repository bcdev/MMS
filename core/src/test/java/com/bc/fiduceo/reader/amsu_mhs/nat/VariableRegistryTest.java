package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.AMSUA_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.MHS_L1B_Reader;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;


public class VariableRegistryTest {

    @Test
    public void test_load_success() {
        VariableRegistry mhsRegistry = VariableRegistry.load(MHS_L1B_Reader.RESOURCE_KEY);
        VariableRegistry amsuaRegistry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        assertNotNull(mhsRegistry);
        assertNotNull(amsuaRegistry);

        Map<String, VariableDefinition> mhsVars = mhsRegistry.getVariables();
        Map<String, VariableDefinition> amsuaVars = amsuaRegistry.getVariables();

        assertFalse(mhsVars.isEmpty());
        assertFalse(amsuaVars.isEmpty());
    }

    @Test
    public void test_load_error() {
        try {
            VariableRegistry.load("xyz");
            fail("Expected exception");
        } catch (RuntimeException expected) {
            assertEquals("Resource not found: xyz/variables.json", expected.getMessage());
        }
    }

    @Test
    public void test_load_contents_mhs_l1b() {
        VariableRegistry mhsRegistry = VariableRegistry.load(MHS_L1B_Reader.RESOURCE_KEY);

        assertNotNull(mhsRegistry);

        Map<String, VariableDefinition> mhsVars = mhsRegistry.getVariables();

        assertEquals(2, mhsVars.size());

        VariableDefinition mhsVar1 = mhsVars.get("latitude");

        assertEquals(ProductData.TYPE_INT32, mhsVar1.getData_type());
        assertEquals(3318, mhsVar1.getOffset());
        assertEquals(2, mhsVar1.getStride());
        assertEquals(.0001, mhsVar1.getScale_factor(), 1e-10);

        VariableDefinition mhsVar2 = mhsVars.get("longitude");

        assertEquals(ProductData.TYPE_INT32, mhsVar2.getData_type());
        assertEquals(3322, mhsVar2.getOffset());
        assertEquals(2, mhsVar2.getStride());
        assertEquals(.0001, mhsVar2.getScale_factor(), 1e-10);
    }

    @Test
    public void test_load_contents_amsua_l1b() {
        VariableRegistry amsuaRegistry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        assertNotNull(amsuaRegistry);

        Map<String, VariableDefinition> amsuaVars = amsuaRegistry.getVariables();

        assertEquals(2, amsuaVars.size());

        VariableDefinition amsuaVar1 = amsuaVars.get("latitude");

        assertEquals(ProductData.TYPE_INT32, amsuaVar1.getData_type());
        assertEquals(2082, amsuaVar1.getOffset());
        assertEquals(2, amsuaVar1.getStride());
        assertEquals(.0001, amsuaVar1.getScale_factor(), 1e-10);

        VariableDefinition amsuaVar2 = amsuaVars.get("longitude");

        assertEquals(ProductData.TYPE_INT32, amsuaVar2.getData_type());
        assertEquals(2086, amsuaVar2.getOffset());
        assertEquals(2, amsuaVar2.getStride());
        assertEquals(.0001, amsuaVar2.getScale_factor(), 1e-10);
    }

    @Test
    public void test_getVariableDef_success() {
        VariableRegistry mhsRegistry = VariableRegistry.load(MHS_L1B_Reader.RESOURCE_KEY);
        assertNotNull(mhsRegistry);

        VariableDefinition lon = mhsRegistry.getVariableDef("longitude");
        assertNotNull(lon);
        assertEquals(3322, lon.getOffset());
    }

    @Test
    public void test_getVariableDef_error() {
        VariableRegistry mhsRegistry = VariableRegistry.load(MHS_L1B_Reader.RESOURCE_KEY);
        assertNotNull(mhsRegistry);

        try {
            VariableDefinition lon = mhsRegistry.getVariableDef("not_existing_variable");
            fail("Expected exception");
        } catch (IllegalArgumentException expected) {
            assertEquals("Variable not defined: not_existing_variable", expected.getMessage());
        }

    }
}