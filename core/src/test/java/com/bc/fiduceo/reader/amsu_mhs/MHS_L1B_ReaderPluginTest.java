package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class MHS_L1B_ReaderPluginTest {

    private MHS_L1B_ReaderPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new MHS_L1B_ReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"mhs-ma", "mhs-mb", "mhs-mc"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof MHS_L1B_Reader);
    }
}