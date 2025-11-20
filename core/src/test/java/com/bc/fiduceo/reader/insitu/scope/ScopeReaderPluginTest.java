package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for ScopeReaderPlugin - verifies plugin registration and reader creation.
 * <p>
 * This is Layer 1 testing: Plugin/Factory behavior
 * - Tests sensor key registration
 * - Tests data type declaration
 * - Tests reader instantiation
 */
public class ScopeReaderPluginTest {

    private ScopeReaderPlugin plugin;

    @Before
    public void setUp() {
        // ARRANGE: Create the plugin instance before each test
        plugin = new ScopeReaderPlugin();
    }

    /**
     * Test that the plugin reports all 6 supported SCOPE sensor keys.
     * <p>
     * SCOPE has 6 data types:
     * - scope-coastal-doc (WP23 - Coastal DOC)
     * - scope-doc (WP24 - Open ocean DOC)
     * - scope-phytoplankton (WP25 - Phytoplankton carbon)
     * - scope-pic (Particulate Inorganic Carbon)
     * - scope-poc (Particulate Organic Carbon)
     * - scope-pp (WP26 - Primary Production)
     */
    @Test
    public void testGetSupportedSensorKeys() {
        // ACT: Get the sensor keys from the plugin
        final List<String> sensorKeys =
                Arrays.asList(plugin.getSupportedSensorKeys());

        // ASSERT: Verify we have exactly 6 keys
        assertNotNull("Sensor keys should not be null", sensorKeys);
        assertEquals("Should support 10 SCOPE sensor types", 10,
                sensorKeys.size());

        // ASSERT: Verify the specific keys
        assertTrue(sensorKeys.contains("scope-coastal-doc"));
        assertTrue(sensorKeys.contains("scope-doc"));
        assertTrue(sensorKeys.contains("scope-phytoplankton"));
        assertTrue(sensorKeys.contains("scope-pic"));
        assertTrue(sensorKeys.contains("scope-poc"));
        assertTrue(sensorKeys.contains("scope-pp"));
        assertTrue(sensorKeys.contains("scope-ta"));
        assertTrue(sensorKeys.contains("scope-fco2"));
        assertTrue(sensorKeys.contains("scope-dic"));
        assertTrue(sensorKeys.contains("scope-pH"));
    }

    /**
     * Test that the plugin declares INSITU as its data type.
     * <p>
     * SCOPE readers handle in-situ oceanographic measurements,
     * not satellite/polar orbiting data.
     */
    @Test
    public void testGetDataType() {
        // ACT: Get the data type from the plugin
        final DataType dataType = plugin.getDataType();

        // ASSERT: Verify it's INSITU type
        assertNotNull("Data type should not be null", dataType);
        assertEquals("SCOPE data should be INSITU type", DataType.INSITU, dataType);
    }

    /**
     * Test that the plugin creates a ScopeGenericReader instance.
     * <p>
     * The plugin should create a generic reader that detects
     * the specific SCOPE format from the filename.
     */
    @Test
    public void testCreateReader() {
        // ACT: Create a reader using the plugin
        final Reader reader = plugin.createReader(new ReaderContext());

        // ASSERT: Verify reader was created and is correct type
        assertNotNull("Reader should not be null", reader);
        assertTrue("Should create a ScopeGenericReader instance", reader instanceof ScopeGenericReader);
    }
}