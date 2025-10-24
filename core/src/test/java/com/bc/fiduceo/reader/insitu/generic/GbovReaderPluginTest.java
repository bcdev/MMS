package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class GbovReaderPluginTest {

    private GbovReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new GbovReaderPlugin();
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(null);
        assertNotNull(reader);
        assertTrue(reader instanceof GenericCsvReader);
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"gbov"};

        final String[] sensorKeys = plugin.getSupportedSensorKeys();
        assertArrayEquals(expected, sensorKeys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.INSITU, plugin.getDataType());
    }
}