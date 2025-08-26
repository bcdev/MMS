package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.AMSUA_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.MHS_L1B_Reader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class VariableRegistryTest {

    @Test
    public void test_load_success() {
        VariableRegistry mhsRegistry = VariableRegistry.load(MHS_L1B_Reader.RESOURCE_KEY);
        VariableRegistry amsuaRegistry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        assertNotNull(mhsRegistry);
        assertNotNull(amsuaRegistry);

        List<VariableDefinition> mhsVars = mhsRegistry.getVariables();
        List<VariableDefinition> amsuaVars = amsuaRegistry.getVariables();

        assertEquals(2, mhsVars.size());
        assertEquals(2, amsuaVars.size());

        VariableDefinition mhsVar1 = mhsVars.get(0);
        VariableDefinition amsuaVar1 = amsuaVars.get(0);

        assertEquals("latitude", mhsVar1.getName());
        assertEquals(DATA_TYPE.INTEGER4, mhsVar1.getData_type());
        assertEquals(3318, mhsVar1.getOffset());
        assertEquals(2, mhsVar1.getStride());
        assertEquals(10000, mhsVar1.getScale_factor());

        assertEquals("latitude", amsuaVar1.getName());
        assertEquals(DATA_TYPE.INTEGER4, amsuaVar1.getData_type());
        assertEquals(2082, amsuaVar1.getOffset());
        assertEquals(2, amsuaVar1.getStride());
        assertEquals(10000, amsuaVar1.getScale_factor());

        VariableDefinition mhsVar2 = mhsVars.get(1);
        VariableDefinition amsuaVar2 = amsuaVars.get(1);

        assertEquals("longitude", mhsVar2.getName());
        assertEquals(DATA_TYPE.INTEGER4, mhsVar2.getData_type());
        assertEquals(3322, mhsVar2.getOffset());
        assertEquals(2, mhsVar2.getStride());
        assertEquals(10000, mhsVar2.getScale_factor());

        assertEquals("longitude", amsuaVar2.getName());
        assertEquals(DATA_TYPE.INTEGER4, amsuaVar2.getData_type());
        assertEquals(2086, amsuaVar2.getOffset());
        assertEquals(2, amsuaVar2.getStride());
        assertEquals(10000, amsuaVar2.getScale_factor());
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
}