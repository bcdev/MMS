package com.bc.fiduceo.reader.scope_sat;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ScopeSatReaderPluginTest {

    private ScopeSatReaderPlugin plugin;

    @Before
    public void setUp() {
        plugin = new ScopeSatReaderPlugin();
    }

    @Test
    public void testGetSupportedSensorKeys() {
        final String[] expected = {"scope-sat-fco2", "scope-sat-dic", "scope-sat-coastal-doc", "scope-sat-doc",
                "scope-sat-phytoplankton", "scope-sat-pp", "scope-sat-pic", "scope-sat-poc"};
        final String[] keys = plugin.getSupportedSensorKeys();

        assertArrayEquals(expected, keys);
    }

    @Test
    public void testGetDataType() {
        assertEquals(DataType.POLAR_ORBITING_SATELLITE, plugin.getDataType());
    }

    @Test
    public void testCreateReader() {
        final Reader reader = plugin.createReader(new ReaderContext());
        assertNotNull(reader);
        assertTrue(reader instanceof ScopeSatReader);
    }
}
