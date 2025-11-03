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
 * Integration tests for ScopeDOCReader using real data files.
 * <p>
 * Tests the complete reader functionality with actual SCOPE DOC files.
 * Follows the same pattern as NdbcCWReader_IO_Test.
 */
@RunWith(IOTestRunner.class)
public class ScopeDOCReader_IO_Test {

    private ScopeDOCReader reader;

    @Before
    public void setUp() {
        reader = new ScopeDOCReader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

        try {
            reader.open(testFile);

            final List<Variable> variables = reader.getVariables();

            assertNotNull(variables);
            assertTrue("Should have at least 4 variables", variables.size() >= 4);

            // Check for standard variables
            boolean hasLongitude = false;
            boolean hasLatitude = false;
            boolean hasTime = false;
            boolean hasDOC = false;

            for (Variable var : variables) {
                String name = var.getShortName();
                if ("longitude".equals(name)) hasLongitude = true;
                if ("latitude".equals(name)) hasLatitude = true;
                if ("time".equals(name)) hasTime = true;
                if ("DOC".equals(name)) hasDOC = true;
            }

            assertTrue("Should have longitude variable", hasLongitude);
            assertTrue("Should have latitude variable", hasLatitude);
            assertTrue("Should have time variable", hasTime);
            assertTrue("Should have DOC variable", hasDOC);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_DOC() throws Exception {
        final File testFile = getDOCTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "DOC");

            assertNotNull("Should read DOC variable", array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        final File testFile = getDOCTestFile();

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
        int[] ymd = reader.extractYearMonthDayFromFilename("SCOPE_WP24_DOC_1998_2021.txt");

        assertEquals(3, ymd.length);
        assertEquals(1998, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testGetRegEx() {
        final String regex = reader.getRegEx();

        assertEquals("SCOPE_WP24.*\\.txt", regex);

        // Test pattern matching
        assertTrue("SCOPE_WP24_DOC_1998_2021.txt".matches(regex));
        assertFalse("SCOPE_WP23_CDOC_1997_2022.txt".matches(regex));
        assertFalse("random_file.txt".matches(regex));
    }

    @Test
    public void testParseLine_ValidLine() {
        final String validLine = "1234567890 -45.5 23.7 12.3";
        final DocRecord record = reader.parseLine(validLine);

        assertNotNull("Should parse valid line", record);
        assertEquals(1234567890, record.utc);
        assertEquals(-45.5f, record.longitude, 0.001f);
        assertEquals(23.7f, record.latitude, 0.001f);
        assertEquals(12.3f, record.doc, 0.001f);
    }

    @Test
    public void testParseLine_InvalidLineTooFewTokens() {
        final String invalidLine = "1234567890 -45.5";
        final DocRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line with too few tokens", record);
    }

    @Test
    public void testParseLine_InvalidLineParseException() {
        final String invalidLine = "not_a_number invalid data";
        final DocRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line that causes parsing exception", record);
    }

    private static File getDOCTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
                new String[]{"insitu", "wp24", "SCOPE_WP24_DOC_1998_2021.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}