package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    private File createMhsMetopCPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-mc", "v10", "2025", "08", "20", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}