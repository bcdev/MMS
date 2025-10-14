package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.After;
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
 * Integration tests for ScopeGenericReader - tests file I/O and delegation.
 * <p>
 * These tests verify that ScopeGenericReader correctly:
 * - Opens files and detects the correct reader type
 * - Delegates all operations to the detected reader
 * - Properly manages resources (open/close)
 */
@RunWith(IOTestRunner.class)
public class ScopeGenericReader_IO_Test {

    private ScopeGenericReader reader;

    @Before
    public void setUp() {
        reader = new ScopeGenericReader();
    }

    @After
    public void tearDown() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    @Test
    public void testDirectory() {
        File file = new File("/media");
        boolean isDirectory = file.isDirectory();
    }

    /**
     * Test that opening a PP file detects and creates the correct reader.
     */
    @Test
    public void testOpen_PPFile() throws IOException {
        final File testFile = getPPTestFile();

        // ACT: Open the file
        reader.open(testFile);

        // ASSERT: File should be opened successfully
        // We can verify by calling a method that requires an open file
        final AcquisitionInfo info = reader.read();
        assertNotNull("Should read acquisition info from PP file", info);
    }

    /**
     * Test that opening a DOC file detects and creates the correct reader.
     */
    @Test
    public void testOpen_DOCFile() throws IOException {
        final File testFile = getDOCTestFile();

        // ACT: Open the file
        reader.open(testFile);

        // ASSERT: File should be opened successfully
        final AcquisitionInfo info = reader.read();
        assertNotNull("Should read acquisition info from DOC file", info);
    }

    /**
     * Test that opening a CDOC file detects and creates the correct reader.
     */
    @Test
    public void testOpen_CDOCFile() throws IOException {
        final File testFile = getCDOCTestFile();

        // ACT: Open the file
        reader.open(testFile);

        // ASSERT: File should be opened successfully
        final AcquisitionInfo info = reader.read();
        assertNotNull("Should read acquisition info from CDOC file", info);
    }

    /**
     * Test the close() method properly closes the underlying reader.
     */
    @Test
    public void testClose() throws IOException {
        final File testFile = getPPTestFile();

        // ACT: Open and close
        reader.open(testFile);
        reader.close();

        // ASSERT: After close, actualReader should be null
        // We can verify this by trying to read (should fail)
        try {
            reader.read();
            fail("Should throw exception after close");
        } catch (NullPointerException e) {
            // Expected - actualReader is null
        }
    }

    /**
     * Test that close() is safe to call when no file is open.
     */
    @Test
    public void testClose_WhenNotOpen() throws IOException {
        // ACT & ASSERT: Should not throw exception
        reader.close();
        reader.close(); // Call twice to verify idempotency
    }

    /**
     * Test that read() delegates to the underlying reader.
     */
    @Test
    public void testRead() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Read acquisition info
            final AcquisitionInfo info = reader.read();

            // ASSERT: Should return valid info
            assertNotNull("AcquisitionInfo should not be null", info);
            assertNotNull("Sensing start should be set", info.getSensingStart());
            assertNotNull("Sensing stop should be set", info.getSensingStop());
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getProductSize() delegates correctly.
     */
    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Get product size
            final Dimension size = reader.getProductSize();

            // ASSERT: Should return valid dimensions
            assertNotNull("Product size should not be null", size);
            assertTrue("Width should be positive", size.getNx() > 0);
            assertTrue("Height should be positive", size.getNy() > 0);
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getVariables() delegates correctly.
     */
    @Test
    public void testGetVariables() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Get variables
            final List<Variable> variables = reader.getVariables();

            // ASSERT: Should return variable list
            assertNotNull("Variables list should not be null", variables);
            assertFalse("Should have at least one variable", variables.isEmpty());
        } finally {
            reader.close();
        }
    }

    /**
     * Test that readRaw() delegates correctly.
     */
    @Test
    public void testReadRaw() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Read raw data
            final Array array = reader.readRaw(0, 0, new Interval(1, 1), "longitude");

            // ASSERT: Should return array
            assertNotNull("Array should not be null", array);
            assertArrayEquals("Should be 1x1 array", new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    /**
     * Test that readScaled() delegates correctly.
     */
    @Test
    public void testReadScaled() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Read scaled data
            final Array array = reader.readScaled(0, 0, new Interval(1, 1), "longitude");

            // ASSERT: Should return array
            assertNotNull("Array should not be null", array);
            assertArrayEquals("Should be 1x1 array", new int[]{1, 1}, array.getShape());
        } finally {
            reader.close();
        }
    }

    /**
     * Test that readAcquisitionTime() delegates correctly.
     */
    @Test
    public void testReadAcquisitionTime() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Read acquisition time
            final ArrayInt.D2 timeArray = reader.readAcquisitionTime(0, 0, new Interval(1, 1));

            // ASSERT: Should return time array
            assertNotNull("Time array should not be null", timeArray);
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getPixelLocator() delegates correctly.
     * Note: SCOPE readers throw RuntimeException for getPixelLocator
     */
    @Test
    public void testGetPixelLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT & ASSERT: Should delegate and throw RuntimeException
            reader.getPixelLocator();
            fail("Should throw RuntimeException");
        } catch (RuntimeException expected) {
            // Expected behavior for SCOPE readers
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getSubScenePixelLocator() delegates correctly.
     */
    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT & ASSERT: Should delegate and throw RuntimeException
            reader.getSubScenePixelLocator(null);
            fail("Should throw RuntimeException");
        } catch (RuntimeException expected) {
            // Expected behavior for SCOPE readers
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getTimeLocator() delegates correctly.
     */
    @Test
    public void testGetTimeLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Get time locator
            final TimeLocator timeLocator = reader.getTimeLocator();

            // ASSERT: Should return valid time locator
            assertNotNull("TimeLocator should not be null", timeLocator);
        } finally {
            reader.close();
        }
    }

    /**
     * Test that extractYearMonthDayFromFilename() delegates correctly.
     */
    @Test
    public void testExtractYearMonthDayFromFilename() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Extract date from filename
            final int[] ymd = reader.extractYearMonthDayFromFilename(testFile.getName());

            // ASSERT: Should return valid date array
            assertNotNull("Date array should not be null", ymd);
            assertEquals("Should have 3 elements (year, month, day)", 3, ymd.length);
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getLongitudeVariableName() delegates correctly.
     */
    @Test
    public void testGetLongitudeVariableName() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Get longitude variable name
            final String lonName = reader.getLongitudeVariableName();

            // ASSERT: Should return valid name
            assertNotNull("Longitude variable name should not be null", lonName);
            assertEquals("Longitude variable should be 'longitude'", "longitude", lonName);
        } finally {
            reader.close();
        }
    }

    /**
     * Test that getLatitudeVariableName() delegates correctly.
     */
    @Test
    public void testGetLatitudeVariableName() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // ACT: Get latitude variable name
            final String latName = reader.getLatitudeVariableName();

            // ASSERT: Should return valid name
            assertNotNull("Latitude variable name should not be null", latName);
            assertEquals("Latitude variable should be 'latitude'", "latitude", latName);
        } finally {
            reader.close();
        }
    }

    // ========== Helper methods to get test files ==========

    private static File getPPTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp26", "SCOPE_WP26_PP_1958_2021.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getDOCTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp24", "SCOPE_WP24_DOC_1998_2021.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getCDOCTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp23", "SCOPE_WP23_CDOC_1997_2022.txt"}, false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
