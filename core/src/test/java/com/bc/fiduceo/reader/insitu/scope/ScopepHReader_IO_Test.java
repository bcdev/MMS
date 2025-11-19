package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for ScopepHReader using real data files.
 * <p>
 * Tests the complete reader functionality with actual SCOPE pH files.
 * Follows the same pattern as ScopeDOCReader_IO_Test.
 */
@RunWith(IOTestRunner.class)
public class ScopepHReader_IO_Test {

    private ScopepHReader reader;

    @Before
    public void setUp() {
        reader = new ScopepHReader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
            assertNotNull(info);
            assertNotNull("Sensing start should not be null", info.getSensingStart());
            assertNotNull("Sensing stop should not be null", info.getSensingStop());
            assertEquals(NodeType.UNDEFINED, info.getNodeType());
            assertNull(info.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);
            final Dimension productSize = reader.getProductSize();

            assertNotNull(productSize);
            assertEquals("product_size", productSize.getName());
            assertEquals(1, productSize.getNx());
            assertTrue("Should have at least one record", productSize.getNy() > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            final List<Variable> variables = reader.getVariables();

            assertNotNull(variables);
            assertTrue("Should have at least 8 variables", variables.size() >= 8);

            // Check for standard variables
            boolean hasLongitude = false;
            boolean hasLatitude = false;
            boolean hasTime = false;
            boolean hasInSituPH = false;
            boolean hasInSituPHStd = false;
            boolean hasUExpFnnUPH = false;
            boolean hasUExpFnnUUncertainty = false;
            boolean hasRegion = false;

            for (Variable var : variables) {
                String name = var.getShortName();
                if ("longitude".equals(name)) hasLongitude = true;
                if ("latitude".equals(name)) hasLatitude = true;
                if ("time".equals(name)) hasTime = true;
                if ("in_situ_pH".equals(name)) hasInSituPH = true;
                if ("in_situ_pH_std".equals(name)) hasInSituPHStd = true;
                if ("UExP_FNN_U_pH".equals(name)) hasUExpFnnUPH = true;
                if ("UExP_FNN_U_uncertainty".equals(name)) hasUExpFnnUUncertainty = true;
                if ("region".equals(name)) hasRegion = true;
            }

            assertTrue("Should have longitude variable", hasLongitude);
            assertTrue("Should have latitude variable", hasLatitude);
            assertTrue("Should have time variable", hasTime);
            assertTrue("Should have in_situ_pH variable", hasInSituPH);
            assertTrue("Should have in_situ_pH_std variable", hasInSituPHStd);
            assertTrue("Should have UExP_FNN_U_pH variable", hasUExpFnnUPH);
            assertTrue("Should have UExP_FNN_U_uncertainty variable", hasUExpFnnUUncertainty);
            assertTrue("Should have region variable", hasRegion);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_InSituPH() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "in_situ_pH");

            assertNotNull("Should read in_situ_pH variable", array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);
            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            // Expected for insitu data
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);
            reader.getSubScenePixelLocator(null);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            // Expected for insitu data
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            // Get time for first record
            long time = timeLocator.getTimeFor(0, 0);
            assertTrue("Time should be a valid unix timestamp", time > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Caching() throws IOException {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            // Call getTimeLocator twice - should return cached instance
            final TimeLocator timeLocator1 = reader.getTimeLocator();
            final TimeLocator timeLocator2 = reader.getTimeLocator();

            assertNotNull("First call should return TimeLocator", timeLocator1);
            assertNotNull("Second call should return TimeLocator", timeLocator2);
            assertSame("Should return same cached instance", timeLocator1, timeLocator2);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            // Read longitude at first record
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "longitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaledNull() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "something");

            assertNull(array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_3x3Window() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            // Read with 3x3 window - should get fill values except center
            final Array array = reader.readScaled(0, 0, new Interval(3, 3), "latitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{3, 3}, array.getShape());

            // Center should have data, surrounding should be fill values
            float centerValue = array.getFloat(4); // index [1,1] in 3x3
            assertFalse("Center value should not be NaN", Float.isNaN(centerValue));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws Exception {
        final File testFile = getpHTestFile();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(0, 0, new Interval(1, 1));

            assertNotNull(acquisitionTime);
            assertArrayEquals(new int[]{1, 1}, acquisitionTime.getShape());

            // Time should be valid unix timestamp
            int time = acquisitionTime.get(0, 0);
            assertTrue("Time should be positive", time > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetLongitudeVariableName() {
        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName() {
        assertEquals("latitude", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        int[] ymd = reader.extractYearMonthDayFromFilename("SCOPE_WP21_pH_1985_2021.txt");

        assertEquals(3, ymd.length);
        assertEquals(1985, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testGetRegEx() {
        final String regex = reader.getRegEx();

        assertEquals("SCOPE_WP21.*\\.txt", regex);

        // Test pattern matching
        assertTrue("SCOPE_WP21_pH_1985_2021.txt".matches(regex));
        assertFalse("SCOPE_WP24_DOC_1998_2021.txt".matches(regex));
        assertFalse("random_file.txt".matches(regex));
    }

    @Test
    public void testParseLine_ValidLine() {
        final String validLine = "477273600 57.500 -29.500 8.112 0.000 8.105 0.035 0.000";
        final pHRecord record = reader.parseLine(validLine);

        assertNotNull("Should parse valid line", record);
        assertEquals(477273600, record.utc);
        assertEquals(57.500f, record.longitude, 0.001f);
        assertEquals(-29.500f, record.latitude, 0.001f);
        assertEquals(8.112f, record.inSituPH, 0.001f);
        assertEquals(0.000f, record.inSituPHStd, 0.001f);
        assertEquals(8.105f, record.uExpFnnUPH, 0.001f);
        assertEquals(0.035f, record.uExpFnnUUncertainty, 0.001f);
        assertEquals(0.000f, record.region, 0.001f);
    }

    @Test
    public void testParseLine_InvalidLineTooFewTokens() {
        final String invalidLine = "477273600 57.500 -29.500";
        final pHRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line with too few tokens", record);
    }

    @Test
    public void testParseLine_InvalidLineParseException() {
        final String invalidLine = "not_a_number invalid data";
        final pHRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line that causes parsing exception", record);
    }

    private static File getpHTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
                new String[]{"insitu", "wp21", "SCOPE_WP21_pH_1985_2021.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}