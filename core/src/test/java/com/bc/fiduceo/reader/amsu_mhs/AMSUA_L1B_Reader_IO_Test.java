package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
            final Date expectedStop  = sdf.parse("20160102013124Z");

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

    private File createAmsuaMetopAPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsua-ma", "v8A", "2016", "01", "01", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
