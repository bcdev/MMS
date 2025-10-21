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

import static com.bc.fiduceo.geometry.GeometryFactory.Type.S2;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AMSUA_L1B_Reader_IO_Test {

    private AMSUA_L1B_Reader reader;

    @Before
    public void setUp() {
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(S2));
        reader = new AMSUA_L1B_Reader(readerContext);
    }

    @Test
    public void testReadAcquisitionInfo_MetopA() throws IOException, ParseException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            final Date expectedStart = sdf.parse("20160101234924Z");
            final Date expectedStop = sdf.parse("20160102013124Z");

            assertEquals(expectedStart, acquisitionInfo.getSensingStart());
            assertEquals(expectedStop, acquisitionInfo.getSensingStop());
            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertTrue(boundingGeometry instanceof MultiPolygon);
            final MultiPolygon multiPolygon = (MultiPolygon) boundingGeometry;
            final List<Polygon> polygons = multiPolygon.getPolygons();
            assertEquals(2, polygons.size());

            final Point[] coordinates0 = polygons.get(0).getCoordinates();
            assertEquals(51, coordinates0.length);
            assertEquals(-168.0057, coordinates0[0].getLon(), 1e-8);
            assertEquals(65.5792, coordinates0[0].getLat(), 1e-8);
            assertEquals(169.72060000000005, coordinates0[48].getLon(), 1e-8);
            assertEquals(50.1226, coordinates0[48].getLat(), 1e-8);

            Point[] coordinates1 = polygons.get(1).getCoordinates();
            assertEquals(51, coordinates1.length);
            assertEquals(-51.4854, coordinates1[0].getLon(), 1e-8);
            assertEquals(-73.0652, coordinates1[0].getLat(), 1e-8);
            assertEquals(-52.7916, coordinates1[48].getLon(), 1e-8);
            assertEquals(-54.673, coordinates1[48].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            TimeAxis timeAxis = timeAxes[0];
            Point[] coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(21, coordinates.length);
            assertEquals(161.1958, coordinates[1].getLon(), 1e-8);
            assertEquals(62.6422, coordinates[1].getLat(), 1e-8);

            timeAxis = timeAxes[1];
            coordinates = timeAxis.getGeometry().getCoordinates();
            assertEquals(21, coordinates.length);
            assertEquals(-31.1957, coordinates[1].getLon(), 1e-8);
            assertEquals(-61.7517, coordinates[1].getLat(), 1e-8);

            Date time = timeAxes[0].getTime(coordinates0[0]);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 23, 49, 24, 0, time);
            time = timeAxes[1].getTime(coordinates1[1]);
            TestUtil.assertCorrectUTCDate(2016, 1, 2, 0, 40, 24, 0, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");

        try {
            reader.open(file);

            final TimeLocator timeLocator = reader.getTimeLocator();
            long time = timeLocator.getTimeFor(3, 0);
            TestUtil.assertCorrectUTCDate(2016, 1, 1, 23, 49, 24, 0, new Date(time));

            time = timeLocator.getTimeFor(4, 250);
            TestUtil.assertCorrectUTCDate(2016, 1, 2, 0, 22, 46, 618, new Date(time));

            time = timeLocator.getTimeFor(5, 764);
            TestUtil.assertCorrectUTCDate(2016, 1, 2, 1, 31, 24, 0, new Date(time));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetPixelLocator() throws IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");

        try {
            reader.open(file);

            final PixelLocator pixelLocator = reader.getPixelLocator();
            Point2D geoLocation = pixelLocator.getGeoLocation(0, 0, null);
            assertEquals(-168.0057, geoLocation.getX(), 1e-8);
            assertEquals(65.5792, geoLocation.getY(), 1e-8);

            geoLocation = pixelLocator.getGeoLocation(5, 108, null);
            assertEquals(150.1052, geoLocation.getX(), 1e-8);
            assertEquals(20.8303, geoLocation.getY(), 1e-8);

            Point2D[] pixelLocations = pixelLocator.getPixelLocation(-168.0057000002, 65.5792);
            assertEquals(1, pixelLocations.length);
            assertEquals(0, pixelLocations[0].getX(), 1e-8);
            assertEquals(0, pixelLocations[0].getY(), 1e-8);

            pixelLocations = pixelLocator.getPixelLocation(150.1052, 20.8303);
            assertEquals(1, pixelLocations.length);
            assertEquals(5, pixelLocations[0].getX(), 1e-8);
            assertEquals(108, pixelLocations[0].getY(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetSubScenePixelLocator() throws IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
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
    public void testGetProductSize() throws IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            final Dimension productSize = reader.getProductSize();
            assertEquals(30, productSize.getNx(), 1e-8);
            assertEquals(765, productSize.getNy(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(1, 1, new Interval(3, 3), "SCENE_RADIANCE_01");
            Index idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(11880, rawData.getInt(idx));
            idx.set(0, 1);
            assertEquals(12634, rawData.getInt(idx));
            idx.set(0, 2);
            assertEquals(12883, rawData.getInt(idx));

            rawData = reader.readRaw(2, 2, new Interval(3, 3), "solar_zenith_angle");
            idx = rawData.getIndex();
            idx.set(1, 0);
            assertEquals(8883, rawData.getInt(idx));
            idx.set(1, 1);
            assertEquals(8947, rawData.getInt(idx));
            idx.set(1, 2);
            assertEquals(9002, rawData.getInt(idx));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topBorder() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(1, 0, new Interval(3, 3), "SCENE_RADIANCE_02");
            Index idx = rawData.getIndex();
            idx.set(0, 0);
            assertEquals(-2147483648, rawData.getInt(idx));
            idx.set(1, 1);
            assertEquals(21796, rawData.getInt(idx));
            idx.set(2, 2);
            assertEquals(21208, rawData.getInt(idx));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_upperRightEdge() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array rawData = reader.readRaw(29, 0, new Interval(3, 3), "SURFACE_PROPERTIES");
            Index idx = rawData.getIndex();
            idx.set(0, 2);
            assertEquals(-32768, rawData.getInt(idx));
            idx.set(1, 2);
            assertEquals(-32768, rawData.getInt(idx));
            idx.set(0, 1);
            assertEquals(-32768, rawData.getInt(idx));
            idx.set(1, 1);
            assertEquals(1, rawData.getInt(idx));
            idx.set(2, 1);
            assertEquals(1, rawData.getInt(idx));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(3, 3, new Interval(3, 3), "SCENE_RADIANCE_03");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(0.005504000000655651, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 1);
            assertEquals(0.005510400049388409, scaledData.getFloat(idx), 1e-8);
            idx.set(0, 2);
            assertEquals(0.005386400036513805, scaledData.getFloat(idx), 1e-8);

            scaledData = reader.readScaled(4, 4, new Interval(3, 3), "satellite_azimuth_angle");
            idx = scaledData.getIndex();
            idx.set(1, 0);
            assertEquals(-50.310001373291016, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(-51.689998626708984, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 2);
            assertEquals(-52.97999954223633, scaledData.getFloat(idx), 1e-8);

            // and check for a variable without scale factor tb 2025-09-17
            scaledData = reader.readScaled(4, 4, new Interval(3, 3), "TERRAIN_ELEVATION");
            idx = scaledData.getIndex();
            idx.set(1, 0);
            assertEquals(244, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(305, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 2);
            assertEquals(457, scaledData.getFloat(idx), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_rightBorder() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(29, 5, new Interval(3, 3), "SCENE_RADIANCE_04");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(0.006011600140482187, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(0.005992699880152941, scaledData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-214.7483673095703, scaledData.getFloat(idx), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_lowerRightEdge() throws IOException, InvalidRangeException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            Array scaledData = reader.readScaled(29, 764, new Interval(3, 3), "satellite_zenith_angle");
            Index idx = scaledData.getIndex();
            idx.set(0, 0);
            assertEquals(53.040000915527344, scaledData.getFloat(idx), 1e-8);
            idx.set(1, 1);
            assertEquals(57.59000015258789, scaledData.getFloat(idx), 1e-8);
            idx.set(2, 2);
            assertEquals(-327.67999267578125, scaledData.getFloat(idx), 1e-8);
        } finally {
            reader.close();
        }
    }


    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(24, variables.size());

            VariableProxy variable = (VariableProxy) variables.get(0);
            assertEquals("solar_zenith_angle", variable.getFullName());
            assertEquals(DataType.SHORT, variable.getDataType());
            List<Attribute> attributes = variable.getAttributes();
            Attribute attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("degree", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(0.01, attribute.getNumericValue());
            attribute = attributes.get(2);
            assertEquals("add_offset", attribute.getShortName());
            assertEquals(0.0, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-32768, attribute.getNumericValue().intValue());
            attribute = attributes.get(4);
            assertEquals("standard_name", attribute.getShortName());
            assertEquals("solar_zenith_angle", attribute.getStringValue());

            variable = (VariableProxy) variables.get(5);
            assertEquals("longitude", variable.getFullName());
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

            variable = (VariableProxy) variables.get(6);
            assertEquals("SURFACE_PROPERTIES", variable.getFullName());
            assertEquals(DataType.SHORT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(1);
            assertEquals("flag_meanings", attribute.getShortName());
            assertEquals("water mixed_coast land", attribute.getStringValue());
            attribute = attributes.get(2);
            assertEquals("flag_values", attribute.getShortName());
            assertEquals(0, attribute.getValues().getShort(0));
            assertEquals(1, attribute.getValues().getShort(1));
            assertEquals(2, attribute.getValues().getShort(2));

            variable = (VariableProxy) variables.get(10);
            assertEquals("SCENE_RADIANCE_07", variable.getFullName());
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

            variable = (VariableProxy) variables.get(15);
            assertEquals("SCENE_RADIANCE_14", variable.getFullName());
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

            variable = (VariableProxy) variables.get(20);
            assertEquals("SCENE_RADIANCE_02", variable.getFullName());
            assertEquals(DataType.INT, variable.getDataType());
            attributes = variable.getAttributes();
            attribute = attributes.get(0);
            assertEquals("units", attribute.getShortName());
            assertEquals("mW/m2/sr/cm-1", attribute.getStringValue());
            attribute = attributes.get(1);
            assertEquals("scale_factor", attribute.getShortName());
            assertEquals(1.0E-7, attribute.getNumericValue());
            attribute = attributes.get(2);
            assertEquals("add_offset", attribute.getShortName());
            assertEquals(0.0, attribute.getNumericValue());
            attribute = attributes.get(3);
            assertEquals("_FillValue", attribute.getShortName());
            assertEquals(-2147483648, attribute.getNumericValue());

            variable = (VariableProxy) variables.get(23);
            assertEquals("SCENE_RADIANCE_11", variable.getFullName());
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
    public void testReadAcquisitionTime() throws InvalidRangeException, IOException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        try {
            reader.open(file);

            ArrayInt.D2 timeArray = reader.readAcquisitionTime(14, 189, new Interval(3, 5));
                assertEquals(1451693662, timeArray.getInt(0));
                assertEquals(1451693662, timeArray.getInt(1));

                assertEquals(1451693670, timeArray.getInt(3));
                assertEquals(1451693670, timeArray.getInt(4));

        } finally {
            reader.close();
        }
    }
    private File createAmsuaMetopAPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsua-ma-l1b", "v8A", "2016", "01", "01", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
