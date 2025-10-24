package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.VariableProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

@RunWith(IOTestRunner.class)
public class GenericCsvReader_NDBC_SM_IO_Test {

    private GenericCsvReader reader;

    @Before
    public void setUp() {
        reader = new GenericCsvReader();
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            final AcquisitionInfo info = reader.read();

            TestUtil.assertCorrectUTCDate(2017, 1, 1, 0, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2017, 10, 19, 1, 0, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            final TimeLocator timeLocator = reader.getTimeLocator();

            assertNotNull(timeLocator);
            assertEquals(1483228800000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1496464200000L, timeLocator.getTimeFor(0, 22000));
            assertEquals(1508374800000L, timeLocator.getTimeFor(0, 41833));

        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename_GBOV_success() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            int[] ymd = reader.extractYearMonthDayFromFilename(testFile.getAbsolutePath());

            assertEquals(3, ymd.length);
            assertEquals(2017, ymd[0]);
            assertEquals(1, ymd[1]);
            assertEquals(1, ymd[2]);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws InvalidRangeException,IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 2004, new Interval(1, 1), "id");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("42088", array.getObject(0));

            array = reader.readRaw(7, 4, new Interval(1, 1), "type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(1, array.getByte(0));

            array = reader.readRaw(8, 5, new Interval(1, 1), "latitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(11.301f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 5, new Interval(1, 1), "longitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(-60.521f, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "anemometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(3.35f, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "air_temp_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(9.96921E36F, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "barometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(9.96921E36F, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "sst_depth");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(9.96921E36F, array.getFloat(0), 1e-8);


            array = reader.readRaw(11, 8, new Interval(1, 1), "WDIR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals((short) 83, array.getShort(0));

            array = reader.readRaw(11, 8, new Interval(1, 1), "WSPD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(6.0f, array.getFloat(0), 1e-8);

            array = reader.readRaw(12, 5, new Interval(1, 1), "GST");
            assertEquals(8.3f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "WVHT");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "DPD");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "APD");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 8, new Interval(1, 1), "MWD");
            assertEquals((short) 999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "PRES");
            assertEquals(1015.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "ATMP");
            assertEquals(27.2f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "WTMP");
            assertEquals(999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "DEWP");
            assertEquals(20.2f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "VIS");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "TIDE");
            assertEquals(99.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(7, 0, new Interval(1, 1), "time");
            assertEquals(DataType.INT, array.getDataType());
            assertEquals(1483228800, array.getInt(0));

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File testFile = getSM();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(13, 41833, new Interval(1, 1));
            NCTestUtils.assertValueAt(1508374800, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);

            List<Variable> vars = reader.getVariables();

            assertNotNull(vars);
            assertFalse(vars.isEmpty());
            assertEquals(22, vars.size());

            VariableProxy var = (VariableProxy) vars.get(0);
            assertEquals("id", var.getFullName());
            assertEquals(DataType.STRING, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(1);
            assertEquals("latitude", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(2);
            assertEquals("longitude", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(3);
            assertEquals("type", var.getFullName());
            assertEquals(DataType.BYTE, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(4);
            assertEquals("anemometer_height", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(5);
            assertEquals("air_temp_height", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(6);
            assertEquals("barometer_height", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(7);
            assertEquals("sst_depth", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(8);
            assertEquals("time", var.getFullName());
            assertEquals(DataType.INT, var.getDataType());
            assertEquals(3, var.getAttributes().size());


            var = (VariableProxy) vars.get(9);
            assertEquals("WDIR", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(10);
            assertEquals("WSPD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(11);
            assertEquals("GST", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(12);
            assertEquals("WVHT", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(13);
            assertEquals("DPD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(14);
            assertEquals("APD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(15);
            assertEquals("MWD", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(16);
            assertEquals("PRES", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(17);
            assertEquals("ATMP", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(18);
            assertEquals("WTMP", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(19);
            assertEquals("DEWP", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(20);
            assertEquals("VIS", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(21);
            assertEquals("TIDE", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());


        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            Dimension productSize = reader.getProductSize();
            assertEquals(41834, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void test_getRegex_NDBC_SM() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            assertEquals("\\\\w{5}h\\\\d{4}\\.txt", reader.getRegEx());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testCoordinateVariableNames() throws IOException {
        final File testFile = getSM();

        try {
            reader.open(testFile);
            String longitudeVariableName = reader.getLongitudeVariableName();
            String latitudeVariableName = reader.getLatitudeVariableName();
            assertEquals("longitude", longitudeVariableName);
            assertEquals("latitude", latitudeVariableName);
        } finally {
            reader.close();
        }
    }

    private static File getSM() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-sm-cb", "v1", "2017", "42088h2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
