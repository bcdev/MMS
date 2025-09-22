package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.VariableProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class MHS_L1B_Reader_IO_Test {

    private MHS_L1B_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        this.reader = new MHS_L1B_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_MetopC() throws IOException, ParseException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");

        try {
            this.reader.open(file);
            final AcquisitionInfo acquisitionInfo = this.reader.read();

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final Date expectedStart = sdf.parse("20250820060350Z");
            final Date expectedStop = sdf.parse("20250820074550Z");

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();

            assertEquals(expectedStart, acquisitionInfo.getSensingStart());
            assertEquals(expectedStop, acquisitionInfo.getSensingStop());
            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());

            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            Point[] coordinates0 = polygons.get(0).getCoordinates();
            assertEquals(135, coordinates0.length);
            assertEquals(84.0214, coordinates0[0].getLon(), 1e-8);
            assertEquals(12.696399999999999, coordinates0[48].getLon(), 1e-8);
            assertEquals(56.086600000000004, coordinates0[0].getLat(), 1e-8);
            assertEquals(-54.333, coordinates0[48].getLat(), 1e-8);

            Point[] coordinates1 = polygons.get(1).getCoordinates();
            assertEquals(135, coordinates1.length);
            assertEquals(-145.5978, coordinates1[0].getLon(), 1e-8);
            assertEquals(-145.0478, coordinates1[48].getLon(), 1e-8);
            assertEquals(-61.4848, coordinates1[0].getLat(), 1e-8);
            assertEquals(61.40250000000001, coordinates1[48].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TimeAxis timeAxis = timeAxes[0];
            Point[] coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(59, coordinates.length);
            assertEquals(65.3291, coordinates[1].getLon(), 1e-8);
            assertEquals(57.4655, coordinates[1].getLat(), 1e-8);

            timeAxis = timeAxes[1];
            coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(59, coordinates.length);
            assertEquals(-127.6211, coordinates[1].getLon(), 1e-8);
            assertEquals(-56.5496, coordinates[1].getLat(), 1e-8);

            assertEquals(2, timeAxes.length);
            Date time = timeAxes[0].getTime(coordinates0[0]);
            TestUtil.assertCorrectUTCDate(2025, 8, 20, 6, 3, 50, 0, time);
            time = timeAxes[1].getTime(coordinates1[0]);
            TestUtil.assertCorrectUTCDate(2025, 8, 20, 6, 54, 50, 0, time);

        } finally {
            this.reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            long time = timeLocator.getTimeFor(12, 0);
            TestUtil.assertCorrectUTCDate(2025, 8, 20, 6, 3, 50, 0, new Date(time));

            time = timeLocator.getTimeFor(12, 250);
            TestUtil.assertCorrectUTCDate(2025, 8, 20, 6, 14, 56, 957, new Date(time));

            time = timeLocator.getTimeFor(12, 2294);
            TestUtil.assertCorrectUTCDate(2025, 8, 20, 7, 45, 50, 0, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            Point2D geoLocation = pixelLocator.getGeoLocation(0, 0, null);
            assertEquals(84.0214, geoLocation.getX(), 1e-8);
            assertEquals(56.0866, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(6, 109, null);
            assertEquals(68.5426, geoLocation.getX(), 1e-8);
            assertEquals(41.5551, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(84.0214, 56.0866);
            assertEquals(1, pixelLocations.length);
            assertEquals(0, pixelLocations[0].getX(), 1e-8);
            assertEquals(0, pixelLocations[0].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(68.5426, 41.5551);
            assertEquals(1, pixelLocations.length);
            assertEquals(6, pixelLocations[0].getX(), 1e-8);
            assertEquals(109, pixelLocations[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            final PixelLocator subScenePixelLocator = reader.getSubScenePixelLocator(null);
            assertSame(pixelLocator, subScenePixelLocator);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(10, 48, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(777404.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(775187.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(776269.0, rawData.getFloat(idx), 1e-8);

            rawData = reader.readRaw(10, 48, new Interval(3, 3), "latitude");
            idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(510754.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(511890.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(512965.0, rawData.getFloat(idx), 1e-8);

            rawData = reader.readRaw(10, 48, new Interval(3, 3), "solar_azimuth_angle");
            idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(15514.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(15463.0, rawData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(15413.0, rawData.getFloat(idx), 1e-8);

            rawData = reader.readRaw(78, 558, new Interval(3, 3), "SURFACE_PROPERTIES");
            idx = rawData.getIndex();
            idx.set(1, 0);
            assertEquals(0, rawData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(1, rawData.getFloat(idx), 1e-8);
            idx.set(1, 2);
            assertEquals(1, rawData.getFloat(idx), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightBorder() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(89, 89, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(789870.0, rawData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(790777.0, rawData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-2.147483648E9, rawData.getFloat(idx), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_upperRightEdge() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(89, 2282, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(765371.0, rawData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(760786.0, rawData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-2.147483648E9, rawData.getFloat(idx), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(10, 48, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(0.07774040102958679, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(0.07751870155334473, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(0.07762689888477325, scaledData.getFloat(idx), 1e-8);

            scaledData = reader.readScaled(10, 48, new Interval(3, 3), "latitude");
            idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(51.07540130, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(51.18899917, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(51.29650115, scaledData.getFloat(idx), 1e-8);

            scaledData = reader.readScaled(10, 48, new Interval(3, 3), "solar_azimuth_angle");
            idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(155.13999938, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(154.63000488, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(154.13000488, scaledData.getFloat(idx), 1e-8);

            scaledData = reader.readScaled(78, 558, new Interval(3, 3), "SURFACE_PROPERTIES");
            idx = scaledData.getIndex();
            idx.set(1, 0);
            assertEquals(0, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(1, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 2);
            assertEquals(1, scaledData.getFloat(idx), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_rightBorder() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(89, 89, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(0.0789870023727417, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(0.07907769829034805, scaledData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-214.7483673095703, scaledData.getFloat(idx), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_upperRightEdge() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(89, 2282, new Interval(3, 3), "SCENE_RADIANCES_04");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(0.0765371024608612, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(0.07607860118150711, scaledData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-214.7483673095703, scaledData.getFloat(idx), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(90, productSize.getNx(), 1e-8);
            assertEquals(2295, productSize.getNy(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws IOException, InvalidRangeException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(15, variables.size());

            VariableProxy variable = (VariableProxy) variables.get(0);
            assertEquals("FOV_DATA QUALITY", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            List<Attribute> attributes = variable.getAttributes();
            Attribute attribute = attributes.get(0);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-2147483648, attribute.getNumericValue());
            attribute = attributes.get(1);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("quality_flag", attribute.getStringValue());

            variable = (VariableProxy) variables.get(1);
            assertEquals("solar_zenith_angle", variable.getFullName());
            assertEquals(DataType.SHORT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("degree", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(.01, attribute.getNumericValue());
            attribute = attributes.get(2);
            assertEquals("add_offset", attribute.getShortName());
            assertEquals(0.0, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(Short.MIN_VALUE, attribute.getNumericValue());
            attribute = attributes.get(4);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("solar_zenith_angle", attribute.getStringValue());

            variable = (VariableProxy) variables.get(5);
            assertEquals("latitude", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("degree", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(1.0E-4, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-2147483648, attribute.getNumericValue());

            variable = (VariableProxy) variables.get(7);
            assertEquals("SURFACE_PROPERTIES", variable.getFullName());
            assertEquals(DataType.BYTE, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(1);
            assertEquals("flag_meanings", attribute.getShortName());
            assertEquals("water mixed_coast land", attribute.getStringValue());
            attribute = attributes.get(2);
            assertEquals("flag_values", attribute.getShortName());
            assertEquals(0, attribute.getValues().getShort(0));
            assertEquals(1, attribute.getValues().getShort(1));
            assertEquals(2, attribute.getValues().getShort(2));

            variable = (VariableProxy) variables.get(8);
            assertEquals("TERRAIN_ELEVATION", variable.getFullName());
            assertEquals(DataType.SHORT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("m", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(Short.MIN_VALUE, attribute.getNumericValue());
            attribute = attributes.get(2);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("height_above_mean_sea_level", attribute.getStringValue());

            variable = (VariableProxy) variables.get(9);
            assertEquals("TIME_ATTITUDE", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("s", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(4294967295L, attribute.getNumericValue());
            attribute = attributes.get(2);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("time", attribute.getStringValue());

            variable = (VariableProxy) variables.get(10);
            assertEquals("SCENE_RADIANCES_02", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("mW/m2/sr/cm-1", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(1.0E-7, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-2147483648, attribute.getNumericValue());
            attribute = attributes.get(4);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("toa_radiance", attribute.getStringValue());

            variable = (VariableProxy) variables.get(13);
            assertEquals("SCENE_RADIANCES_05", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("mW/m2/sr/cm-1", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(1.0E-7, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-2147483648, attribute.getNumericValue());
            attribute = attributes.get(4);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("toa_radiance", attribute.getStringValue());

        } finally {
            reader.close();
        }
    }

    @Test
    public void getReadAcquisitionTime() throws InvalidRangeException, IOException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        try {
            reader.open(file);
            final int width = 3;
            final int height = 5;

            ArrayInt.D2 timeArray = reader.readAcquisitionTime(56, 2100, new Interval(width, height));
            assertEquals(width * height, timeArray.getSize());
            assertEquals(1755675427, timeArray.getInt(0));
            assertEquals(1755675427, timeArray.getInt(1));

            assertEquals(1755675430, timeArray.getInt(3));
            assertEquals(1755675430, timeArray.getInt(4));
        } finally {
            reader.close();
        }
    }

    private File createMhsMetopCPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-mc", "v10", "2025", "08", "20", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}