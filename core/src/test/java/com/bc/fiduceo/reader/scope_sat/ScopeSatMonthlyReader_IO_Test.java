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
 * This test uses wp26 (PP) April 2016 as a representative monthly dataset.
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
            System.out.println("Sensing start: " + sensingStart);
            System.out.println("Sensing stop: " + sensingStop);
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
            final com.bc.fiduceo.location.PixelLocator pixelLocator = reader.getPixelLocator();
            assertNotNull("PixelLocator should not be null", pixelLocator);

            // Test pixel -> geo (corner pixel)
            java.awt.geom.Point2D geoLocation = pixelLocator.getGeoLocation(0.5, 0.5, null);
            assertEquals(-179.979166666667, geoLocation.getX(), 1e-5);  // Tolerance for float precision
            assertEquals(-89.9791666666667, geoLocation.getY(), 1e-5);

            // Test geo -> pixel (roundtrip with actual values)
            java.awt.geom.Point2D[] pixelLocations = pixelLocator.getPixelLocation(geoLocation.getX(), geoLocation.getY());
            assertEquals(1, pixelLocations.length);
            assertEquals(0.5, pixelLocations[0].getX(), 1e-8);
            assertEquals(0.5, pixelLocations[0].getY(), 1e-8);

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
    public void testGetTimeLocator() throws IOException {
        final File testFile = getPPTestFile();

        try {
            reader.open(testFile);
            final com.bc.fiduceo.reader.time.TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull("TimeLocator should not be null", timeLocator);

            // For monthly data, time should be constant across all pixels
            final long time1 = timeLocator.getTimeFor(0, 0);
            final long time2 = timeLocator.getTimeFor(100, 100);
            assertEquals("Time should be constant for all pixels", time1, time2);

            // Time should be in April 2016 (around middle of month)
            assertTrue("Time should be positive", time1 > 0);
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
            final String filename = "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_201604-fv6.0.out.nc";
            int[] ymd = reader.extractYearMonthDayFromFilename(filename);

            assertEquals(3, ymd.length);
            assertEquals(2016, ymd[0]);
            assertEquals(4, ymd[1]);
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

            final String filename = "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_201604-fv6.0.out.nc";
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
            new String[]{"scope-merge", "wp26", "2016", "04", "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_201604-fv6.0.out.nc"},
            false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
