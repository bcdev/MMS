package com.bc.fiduceo.reader.scope_sat;

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
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for ScopeSatTimeSeriesReader.
 *
 * Tests the time series reader which handles:
 * - wp21 (Fugacity of CO2)
 * - wp22 (Dissolved Inorganic Carbon with depth)
 *
 * This test uses wp21 (fCO2) as a representative time series dataset.
 */
@RunWith(IOTestRunner.class)
public class ScopeSatTimeSeriesReader_IO_Test {

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
        final File testFile = getFCO2TestFile();

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
        final File testFile = getFCO2TestFile();

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
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);
            final List<Variable> variables = reader.getVariables();

            assertNotNull(variables);
            assertTrue("Should have variables", variables.size() > 0);

            boolean hasLongitude = false;
            boolean hasLatitude = false;
            boolean hasTime = false;
            for (Variable var : variables) {
                String name = var.getShortName();
                if ("longitude".equals(name)) hasLongitude = true;
                if ("latitude".equals(name)) hasLatitude = true;
                if ("time".equals(name)) hasTime = true;
            }

            assertTrue("Should have longitude variable", hasLongitude);
            assertTrue("Should have latitude variable", hasLatitude);
            assertTrue("Should have time variable", hasTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_Coordinates() throws Exception {
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);

            final Dimension size = reader.getProductSize();
            final int centerX = size.getNx() / 2;
            final int centerY = size.getNy() / 2;

            final Array lonArray = reader.readScaled(centerX, centerY, new Interval(3, 3), "longitude");
            final Array latArray = reader.readScaled(centerX, centerY, new Interval(3, 3), "latitude");

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
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);
            assertNotNull("PixelLocator should not be null", reader.getPixelLocator());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File testFile = getFCO2TestFile();

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
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);

            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull("TimeLocator should not be null", timeLocator);

            long time = timeLocator.getTimeFor(0, 0);
            assertTrue("Time should be a valid unix timestamp", time > 0);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws Exception {
        final File testFile = getFCO2TestFile();

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
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);
            assertEquals("longitude", reader.getLongitudeVariableName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetLatitudeVariableName() throws IOException {
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);
            assertEquals("latitude", reader.getLatitudeVariableName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename() {
        final File testFile;
        try {
            testFile = getFCO2TestFile();
            reader.open(testFile);

            int[] ymd = reader.extractYearMonthDayFromFilename(testFile.getName());

            assertEquals(3, ymd.length);
            assertTrue("Year should be valid", ymd[0] > 1900 && ymd[0] < 2100);
            assertTrue("Month should be 1-12", ymd[1] >= 1 && ymd[1] <= 12);
            assertTrue("Day should be valid", ymd[2] >= 1 && ymd[2] <= 31);
        } catch (IOException e) {
            fail("Failed to extract date from filename: " + e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Test
    public void testGetRegEx() throws IOException {
        final File testFile = getFCO2TestFile();

        try {
            reader.open(testFile);
            final String regex = reader.getRegEx();
            assertNotNull(regex);

            final String filename = "UExP-FNN-U_physics_carbonatesystem_ESASCOPE_v5.nc";
            assertTrue("Filename should match regex", filename.matches(regex));
        } finally {
            reader.close();
        }
    }

    private static File getFCO2TestFile() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"satellite", "scope-merge", "wp21", "UExP-FNN-U_physics_carbonatesystem_ESASCOPE_v5.nc"},
            false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
