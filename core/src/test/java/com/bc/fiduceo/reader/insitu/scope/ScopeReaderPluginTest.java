package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for ScopeReaderPlugin - verifies plugin registration and reader creation.
 *
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
     *
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
        final String[] sensorKeys = plugin.getSupportedSensorKeys();

        // ASSERT: Verify we have exactly 6 keys
        assertNotNull("Sensor keys should not be null", sensorKeys);
        assertEquals("Should support 6 SCOPE sensor types", 6, sensorKeys.length);

        // ASSERT: Verify the specific keys (order matters - matches plugin definition)
        assertEquals("scope-coastal-doc", sensorKeys[0]);
        assertEquals("scope-doc", sensorKeys[1]);
        assertEquals("scope-phytoplankton", sensorKeys[2]);
        assertEquals("scope-pic", sensorKeys[3]);
        assertEquals("scope-poc", sensorKeys[4]);
        assertEquals("scope-pp", sensorKeys[5]);
    }

    /**
     * Test that the plugin declares INSITU as its data type.
     *
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
     *
     * The plugin should create a generic reader that detects
     * the specific SCOPE format from the filename.
     */
    @Test
    public void testCreateReader() {
        // ACT: Create a reader using the plugin
        final Reader reader = plugin.createReader(null);

        // ASSERT: Verify reader was created and is correct type
        assertNotNull("Reader should not be null", reader);
        assertTrue("Should create a ScopeGenericReader instance",
                   reader instanceof ScopeGenericReader);
    }
}