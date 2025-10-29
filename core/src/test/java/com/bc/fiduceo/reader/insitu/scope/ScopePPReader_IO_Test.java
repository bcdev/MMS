package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
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

@RunWith(IOTestRunner.class)
public class ScopePPReader_IO_Test {

    private ScopePPReader reader;

    @Before
    public void setUp() {
        reader = new ScopePPReader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
            assertNotNull(info);
            assertNotNull(info.getSensingStart());
            assertNotNull(info.getSensingStop());
            assertEquals(NodeType.UNDEFINED, info.getNodeType());
            assertNull(info.getBoundingGeometry());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final Dimension productSize = reader.getProductSize();

            assertNotNull(productSize);
            assertEquals("product_size", productSize.getName());
            assertEquals(1, productSize.getNx());
            assertTrue(productSize.getNy() > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final List<Variable> variables = reader.getVariables();

            assertNotNull(variables);
            assertTrue(variables.size() >= 5);

            boolean hasLongitude = false, hasLatitude = false, hasTime = false;
            boolean hasPP = false, hasDatabase = false;
            for (Variable var : variables) {
                String name = var.getShortName();
                if ("longitude".equals(name)) hasLongitude = true;
                if ("latitude".equals(name)) hasLatitude = true;
                if ("time".equals(name)) hasTime = true;
                if ("PP".equals(name)) hasPP = true;
                if ("database".equals(name)) hasDatabase = true;
            }

            assertTrue("Should have longitude variable", hasLongitude);
            assertTrue("Should have latitude variable", hasLatitude);
            assertTrue("Should have time variable", hasTime);
            assertTrue("Should have PP variable", hasPP);
            assertTrue("Should have database variable", hasDatabase);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "longitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaledNull() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "something");

            assertNull(array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_PP() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "PP");

            assertNotNull("Should read PP variable", array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_Database() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "database");

            assertNotNull("Should read database variable", array);
            assertArrayEquals(new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            reader.getPixelLocator();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File testFile = getPPTestFile();

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
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            long time = timeLocator.getTimeFor(0, 0);
            assertTrue("Time should be a valid unix timestamp", time > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_Caching() throws IOException {
        final File testFile = getPPTestFile();

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
    public void testReadScaled_3x3Window() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final Array array = reader.readScaled(0, 0, new Interval(3, 3), "latitude");

            assertNotNull(array);
            assertArrayEquals(new int[]{3, 3}, array.getShape());

            float centerValue = array.getFloat(4);
            assertFalse("Center value should not be NaN", Float.isNaN(centerValue));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(0, 0, new Interval(1, 1));

            assertNotNull(acquisitionTime);
            assertArrayEquals(new int[]{1, 1}, acquisitionTime.getShape());

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
        int[] ymd = reader.extractYearMonthDayFromFilename("SCOPE_WP26_PP_1958_2021.txt");

        assertEquals(3, ymd.length);
        assertEquals(1958, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testGetRegEx() {
        assertEquals("SCOPE_WP26_PP.*\\.txt", reader.getRegEx());
        assertTrue("SCOPE_WP26_PP_1958_2021.txt".matches(reader.getRegEx()));
    }

    @Test
    public void testParseLine_ValidLine() {
        final String validLine = "1234567890 -45.5 23.7 150.3 DATABASE_NAME";
        final PpRecord record = reader.parseLine(validLine);

        assertNotNull("Should parse valid line", record);
        assertEquals(1234567890, record.utc);
        assertEquals(-45.5f, record.longitude, 0.001f);
        assertEquals(23.7f, record.latitude, 0.001f);
        assertEquals(150.3f, record.pp, 0.001f);
        assertEquals("DATABASE_NAME", record.database);
    }

    @Test
    public void testParseLine_InvalidLineTooFewTokens() {
        final String invalidLine = "1234567890 -45.5 23.7";
        final PpRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line with too few tokens", record);
    }

    @Test
    public void testParseLine_InvalidLineParseException() {
        final String invalidLine = "not_a_number invalid data here more invalid";
        final PpRecord record = reader.parseLine(invalidLine);

        assertNull("Should return null for line that causes parsing exception", record);
    }

    @Test
    public void testParseLine_NegativeTimestamp() {
        final String lineWithNegativeTime = "-100 -45.5 23.7 150.3 DATABASE_NAME";
        final PpRecord record = reader.parseLine(lineWithNegativeTime);

        assertNull("Should return null for negative timestamp (pre-1970)", record);
    }

    @Test
    public void testParseLine_ExtraWhitespace() {
        final String lineWithExtraSpaces = "  1234567890   -45.5    23.7   150.3   DATABASE_NAME  ";
        final PpRecord record = reader.parseLine(lineWithExtraSpaces);

        assertNotNull("Should parse line with extra whitespace", record);
        assertEquals(1234567890, record.utc);
        assertEquals(-45.5f, record.longitude, 0.001f);
    }

    private static File getPPTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu","wp26", "SCOPE_WP26_PP_1958_2021.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}