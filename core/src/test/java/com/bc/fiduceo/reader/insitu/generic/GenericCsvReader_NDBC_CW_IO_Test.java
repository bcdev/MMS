package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.netcdf.StringVariable;
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

@RunWith(IOTestRunner.class)
public class GenericCsvReader_NDBC_CW_IO_Test {

    private GenericCsvReader reader;

    @Before
    public void setUp() {
        reader = new GenericCsvReader(GenericCsvHelper.RESOURCE_KEY_NDBC_CW);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);
            final AcquisitionInfo info = reader.read();

            TestUtil.assertCorrectUTCDate(2016, 5, 31, 23, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 12, 31, 22, 50, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);
            final TimeLocator timeLocator = reader.getTimeLocator();

            assertNotNull(timeLocator);
            assertEquals(1464735600000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1473843600000L, timeLocator.getTimeFor(0, 15000));
            assertEquals(1483224600000L, timeLocator.getTimeFor(0, 30515));

        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename_GBOV_success() throws IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);
            int[] ymd = reader.extractYearMonthDayFromFilename(testFile.getAbsolutePath());

            assertEquals(3, ymd.length);
            assertEquals(2016, ymd[0]);
            assertEquals(1, ymd[1]);
            assertEquals(1, ymd[2]);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws InvalidRangeException,IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 2004, new Interval(1, 1), "id");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("42002", array.getObject(0));

            array = reader.readRaw(7, 4, new Interval(1, 1), "station_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(0, array.getByte(0));

            array = reader.readRaw(7, 4, new Interval(1, 1), "measurement_type");
            assertEquals(DataType.BYTE, array.getDataType());
            assertEquals(0, array.getByte(0));

            array = reader.readRaw(8, 5, new Interval(1, 1), "latitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(26.055f, array.getFloat(0), 1e-8);

            array = reader.readRaw(8, 5, new Interval(1, 1), "longitude");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(-93.646f, array.getFloat(0), 1e-8);

            array = reader.readRaw(9, 6, new Interval(1, 1), "anemometer_height");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(3.8f, array.getFloat(0), 1e-8);


            array = reader.readRaw(11, 8, new Interval(1, 1), "WDIR");
            assertEquals(DataType.SHORT, array.getDataType());
            assertEquals((short) 999, array.getShort(0));

            array = reader.readRaw(11, 8, new Interval(1, 1), "WSPD");
            assertEquals(DataType.FLOAT, array.getDataType());
            assertEquals(7.2f, array.getFloat(0), 1e-8);

            array = reader.readRaw(12, 9, new Interval(1, 1), "GDR");
            assertEquals((short) 999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "GST");
            assertEquals(7.6f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 23, new Interval(1, 1), "GTIME");
            assertEquals((short) 246, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(7, 0, new Interval(1, 1), "time");
            assertEquals(DataType.INT, array.getDataType());
            assertEquals(1464735600, array.getInt(0));

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File testFile = getCW();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(13, 30515, new Interval(1, 1));
            NCTestUtils.assertValueAt(1483224600, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);

            List<Variable> vars = reader.getVariables();

            assertNotNull(vars);
            assertFalse(vars.isEmpty());
            assertEquals(12, vars.size());

            StringVariable stringVar = (StringVariable) vars.get(0);
            assertEquals("id", stringVar.getShortName());
            assertEquals(DataType.STRING, stringVar.getDataType());
            assertEquals(3, stringVar.getAttributes().size());

            VariableProxy var = (VariableProxy) vars.get(1);
            assertEquals("latitude", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(2);
            assertEquals("longitude", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(3);
            assertEquals("station_type", var.getFullName());
            assertEquals(DataType.BYTE, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(4);
            assertEquals("measurement_type", var.getFullName());
            assertEquals(DataType.BYTE, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(5);
            assertEquals("anemometer_height", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(6);
            assertEquals("time", var.getFullName());
            assertEquals(DataType.INT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(7);
            assertEquals("WDIR", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(8);
            assertEquals("WSPD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(9);
            assertEquals("GDR", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(10);
            assertEquals("GST", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(11);
            assertEquals("GTIME", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);
            Dimension productSize = reader.getProductSize();
            assertEquals(30516, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void test_getRegex_NDBC_CW() throws IOException {
        final File testFile = getCW();

        try {
            reader.open(testFile);
            assertEquals("\\w{5}c\\d{4}.txt", reader.getRegEx());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testCoordinateVariableNames() throws IOException {
        final File testFile = getCW();

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

    private static File getCW() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-cw-ob", "v1", "2016", "42002c2016.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
