package com.bc.fiduceo.reader.scope_sat;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for ScopeSatMonthlyReader.
 *
 * Tests the monthly composite reader which handles:
 * - wp23 (Coastal DOC)
 * - wp24 (DOC)
 * - wp25 (Phytoplankton Carbon)
 * - wp26 (Primary Production)
 * - wpPIC (Particulate Inorganic Carbon)
 * - wpPOC (Particulate Organic Carbon)
 *
 * This test uses wp26 (PP) as a representative monthly dataset.
 */
@RunWith(IOTestRunner.class)
public class ScopeSatMonthlyReader_IO_Test {

    private ScopeSatGenericReader reader;
    private ReaderContext readerContext;

    @Before
    public void setUp() {
        readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        reader = new ScopeSatGenericReader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final AcquisitionInfo info = reader.read();
            assertNotNull(info);

            final Date sensingStart = info.getSensingStart();
            final Date sensingStop = info.getSensingStop();
            assertNotNull("Sensing start should not be null", sensingStart);
            assertNotNull("Sensing stop should not be null", sensingStop);
            assertTrue("Sensing stop should be after start", sensingStop.after(sensingStart));

            assertEquals(NodeType.UNDEFINED, info.getNodeType());
            assertNotNull("Bounding geometry should not be null", info.getBoundingGeometry());
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
            assertEquals("size", productSize.getName());
            assertTrue("Width should be > 0", productSize.getNx() > 0);
            assertTrue("Height should be > 0", productSize.getNy() > 0);
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
            assertTrue("Should have variables", variables.size() > 0);

            boolean hasLon = false;
            boolean hasLat = false;
            for (Variable var : variables) {
                String name = var.getShortName();
                if ("lon".equals(name)) hasLon = true;
                if ("lat".equals(name)) hasLat = true;
            }

            assertTrue("Should have lon variable", hasLon);
            assertTrue("Should have lat variable", hasLat);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_Coordinates() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final Dimension size = reader.getProductSize();
            final int centerX = size.getNx() / 2;
            final int centerY = size.getNy() / 2;

            final Array lonArray = reader.readScaled(centerX, centerY, new Interval(3, 3), "lon");
            final Array latArray = reader.readScaled(centerX, centerY, new Interval(3, 3), "lat");

            assertNotNull("Longitude array should not be null", lonArray);
            assertNotNull("Latitude array should not be null", latArray);
            assertArrayEquals(new int[]{3, 3}, lonArray.getShape());
            assertArrayEquals(new int[]{3, 3}, latArray.getShape());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            assertNotNull("PixelLocator should not be null", reader.getPixelLocator());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final com.bc.fiduceo.geometry.Polygon sceneGeometry = readerContext.getGeometryFactory().createPolygon(
                java.util.Arrays.asList(
                    readerContext.getGeometryFactory().createPoint(-10, -10),
                    readerContext.getGeometryFactory().createPoint(-10, 10),
                    readerContext.getGeometryFactory().createPoint(10, 10),
                    readerContext.getGeometryFactory().createPoint(10, -10),
                    readerContext.getGeometryFactory().createPoint(-10, -10)
                )
            );
            assertNotNull("SubScene PixelLocator should not be null", reader.getSubScenePixelLocator(sceneGeometry));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_ThrowsException() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            reader.getTimeLocator();
            fail("Should throw RuntimeException for monthly data");
        } catch (RuntimeException expected) {
            assertTrue("Exception message should mention TimeLocator",
                       expected.getMessage().contains("TimeLocator"));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws Exception {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            final Dimension size = reader.getProductSize();
            final int centerX = size.getNx() / 2;
            final int centerY = size.getNy() / 2;

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(centerX, centerY, new Interval(3, 3));

            assertNotNull(acquisitionTime);
            assertArrayEquals(new int[]{3, 3}, acquisitionTime.getShape());

            int time = acquisitionTime.get(1, 1);
            assertTrue("Time should be positive", time > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetLongitudeVariableName() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            assertEquals("lon", reader.getLongitudeVariableName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetLatitudeVariableName() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            assertEquals("lat", reader.getLatitudeVariableName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final String filename = "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_200405-fv6.0.out.nc";
            int[] ymd = reader.extractYearMonthDayFromFilename(filename);

            assertEquals(3, ymd.length);
            assertEquals(2004, ymd[0]);
            assertEquals(5, ymd[1]);
            assertEquals(1, ymd[2]);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetRegEx() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final String regex = reader.getRegEx();
            assertNotNull(regex);

            final String filename = "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_200405-fv6.0.out.nc";
            assertTrue("Filename should match regex", filename.matches(regex));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDay_EdgeCases() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);

            // Test with underscore separator (wp26 style)
            int[] ymd1 = reader.extractYearMonthDayFromFilename("SCOPE_NCEO_PP_ESA-OC-L3S_MERGED_199801_fv6.nc");
            assertEquals(1998, ymd1[0]);
            assertEquals(1, ymd1[1]);
            assertEquals(1, ymd1[2]);

            // Test with hyphen separator (most common)
            int[] ymd2 = reader.extractYearMonthDayFromFilename("file_202412-v1.nc");
            assertEquals(2024, ymd2[0]);
            assertEquals(12, ymd2[1]);
            assertEquals(1, ymd2[2]);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDay_InvalidFilename() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            reader.extractYearMonthDayFromFilename("invalid_filename.nc");
            fail("Should throw IllegalArgumentException for invalid filename");
        } catch (IllegalArgumentException expected) {
            assertTrue("Exception message should mention pattern",
                       expected.getMessage().contains("pattern"));
        } finally {
            reader.close();
        }
    }

    private static File getPPTestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"satellite", "scope-merge", "wp26", "2004", "05", "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_200405-fv6.0.out.nc"},
            false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
