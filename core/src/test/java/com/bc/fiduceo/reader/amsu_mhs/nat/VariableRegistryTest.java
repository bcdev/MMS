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

        assertEquals("integer4", mhsVar1.getData_type());
        assertEquals(3318, mhsVar1.getOffset());
        assertEquals(2, mhsVar1.getStride());
        assertEquals(.0001, mhsVar1.getScale_factor(), 1e-10);

        VariableDefinition mhsVar2 = mhsVars.get("longitude");

        assertEquals("integer4", mhsVar2.getData_type());
        assertEquals(3322, mhsVar2.getOffset());
        assertEquals(2, mhsVar2.getStride());
        assertEquals(.0001, mhsVar2.getScale_factor(), 1e-10);
    }

    @Test
    public void test_load_contents_amsua_l1b() {
        VariableRegistry amsuaRegistry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        assertNotNull(amsuaRegistry);

        Map<String, VariableDefinition> amsuaVars = amsuaRegistry.getVariables();

        assertEquals(21, amsuaVars.size());

        final VariableDefinition sceneRadiance01 = amsuaVars.get("SCENE_RADIANCE_01");
        assertEquals("integer4", sceneRadiance01.getData_type());
        assertEquals(22, sceneRadiance01.getOffset());
        assertEquals(15, sceneRadiance01.getStride());
        assertEquals(.0000001, sceneRadiance01.getScale_factor(), 1e-10);
        assertEquals("mW/m2/sr/cm-1", sceneRadiance01.getUnits());

        final VariableDefinition sceneRadiance15 = amsuaVars.get("SCENE_RADIANCE_15");
        assertEquals("integer4", sceneRadiance15.getData_type());
        assertEquals(78, sceneRadiance15.getOffset());
        assertEquals(15, sceneRadiance15.getStride());
        assertEquals(.0000001, sceneRadiance15.getScale_factor(), 1e-10);
        assertEquals("mW/m2/sr/cm-1", sceneRadiance15.getUnits());

        final VariableDefinition solarZenith = amsuaVars.get("solar_zenith_angle");
        assertEquals("integer2", solarZenith.getData_type());
        assertEquals(1842, solarZenith.getOffset());
        assertEquals(4, solarZenith.getStride());
        assertEquals(.01, solarZenith.getScale_factor(), 1e-10);
        assertEquals("degree", solarZenith.getUnits());

        final VariableDefinition satelliteZenith = amsuaVars.get("satellite_zenith_angle");
        assertEquals("integer2", satelliteZenith.getData_type());
        assertEquals(1844, satelliteZenith.getOffset());
        assertEquals(4, satelliteZenith.getStride());
        assertEquals(.01, satelliteZenith.getScale_factor(), 1e-10);
        assertEquals("degree", satelliteZenith.getUnits());

        final VariableDefinition solarAzimuth = amsuaVars.get("solar_azimuth_angle");
        assertEquals("integer2", solarAzimuth.getData_type());
        assertEquals(1846, solarAzimuth.getOffset());
        assertEquals(4, solarAzimuth.getStride());
        assertEquals(.01, solarAzimuth.getScale_factor(), 1e-10);
        assertEquals("degree", solarAzimuth.getUnits());

        final VariableDefinition satelliteAzimuth = amsuaVars.get("satellite_azimuth_angle");
        assertEquals("integer2", satelliteAzimuth.getData_type());
        assertEquals(1848, satelliteAzimuth.getOffset());
        assertEquals(4, satelliteAzimuth.getStride());
        assertEquals(.01, satelliteAzimuth.getScale_factor(), 1e-10);
        assertEquals("degree", satelliteAzimuth.getUnits());

        final VariableDefinition latitude = amsuaVars.get("latitude");
        assertEquals("integer4", latitude.getData_type());
        assertEquals(2082, latitude.getOffset());
        assertEquals(2, latitude.getStride());
        assertEquals(.0001, latitude.getScale_factor(), 1e-10);
        assertEquals("degree", latitude.getUnits());

        final VariableDefinition longitude = amsuaVars.get("longitude");
        assertEquals("integer4", longitude.getData_type());
        assertEquals(2086, longitude.getOffset());
        assertEquals(2, longitude.getStride());
        assertEquals(.0001, longitude.getScale_factor(), 1e-10);
        assertEquals("degree", longitude.getUnits());
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

        try {
            VariableDefinition lon = mhsRegistry.getVariableDef("SCENE_RADIANCE_16");
            fail("Expected exception");
        } catch (IllegalArgumentException expected) {
            assertEquals("Variable not defined: SCENE_RADIANCE_16", expected.getMessage());
        }
    }
}