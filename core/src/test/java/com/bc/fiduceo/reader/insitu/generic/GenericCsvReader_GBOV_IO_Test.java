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
public class GenericCsvReader_GBOV_IO_Test {

    private GenericCsvReader reader;

    @Before
    public void setUp() {
        reader = new GenericCsvReader(GenericCsvHelper.RESOURCE_KEY_GBOV);
    }

    @Test
    public void testReadAcquisitionInfo() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            final AcquisitionInfo info = reader.read();

            TestUtil.assertCorrectUTCDate(2016, 6, 1, 0, 0, 0, 0, info.getSensingStart());
            TestUtil.assertCorrectUTCDate(2016, 6, 28, 0, 0, 0, 0, info.getSensingStop());

            assertEquals(NodeType.UNDEFINED, info.getNodeType());

            assertNull(info.getBoundingGeometry());
            assertNull(info.getTimeAxes());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testPixelLocatorNotImplemented() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            reader.getPixelLocator();
            fail();
        } catch (RuntimeException ignore) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testSubscenePixelLocatorNotImplemented() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            reader.getSubScenePixelLocator(null);
            fail();
        } catch (RuntimeException ignore) {
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            final TimeLocator timeLocator = reader.getTimeLocator();

            assertNotNull(timeLocator);
            assertEquals(1464739200000L, timeLocator.getTimeFor(0, 0));
            assertEquals(1465862400000L, timeLocator.getTimeFor(0, 1));
            assertEquals(1467072000000L, timeLocator.getTimeFor(0, 2));

        } finally {
            reader.close();
        }
    }

    @Test
    public void testExtractYearMonthDayFromFilename_GBOV_success() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            int[] ymd = reader.extractYearMonthDayFromFilename(testFile.getAbsolutePath());

            assertEquals(3, ymd.length);
            assertEquals(2016, ymd[0]);
            assertEquals(6, ymd[1]);
            assertEquals(1, ymd[2]);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw() throws InvalidRangeException,IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);

            Array array = reader.readRaw(7, 0, new Interval(1, 1), "TIME_IS");
            assertEquals(DataType.INT, array.getDataType());
            assertEquals(1464739200, array.getInt(0));

            array = reader.readRaw(7, 2004, new Interval(1, 1), "site");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("Bartlett Experimental Forest", array.getObject(0));

            array = reader.readRaw(7, 2004, new Interval(1, 1), "station");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("BART_047", array.getObject(0));

            array = reader.readRaw(7, 2004, new Interval(1, 1), "IGBP_class");
            assertEquals(DataType.STRING, array.getDataType());
            assertEquals("Mixed Forest", array.getObject(0));

            array = reader.readRaw(12, 5, new Interval(1, 1), "elevation");
            assertEquals(232, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "Lat_IS");
            assertEquals(44.063901f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 5, new Interval(1, 1), "Lon_IS");
            assertEquals(-71.287308f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());


            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_down");
            assertEquals(0.617f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_down_err");
            assertEquals(0.025f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_total");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_total_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_up");
            assertEquals(0.959f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "FIPAR_up_err");
            assertEquals(0.012f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "Clumping_Miller");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "Clumping_Miller_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "Clumping_Warren");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "Clumping_Warren_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Miller_down");
            assertEquals(1.52f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Miller_down_err");
            assertEquals(0.05f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Miller_up");
            assertEquals(6.14f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Miller_up_err");
            assertEquals(0.15f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Warren_down");
            assertEquals(1.2f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Warren_down_err");
            assertEquals(.06f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Warren_up");
            assertEquals(4.61f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_Warren_up_err");
            assertEquals(.15f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_down");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_total_Miller");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAI_total_Warren");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Miller_down");
            assertEquals(1.41f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Miller_down_err");
            assertEquals(.04f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Miller_up");
            assertEquals(4.95f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Miller_up_err");
            assertEquals(.13f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Warren_down");
            assertEquals(1.1f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Warren_down_err");
            assertEquals(.04f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Warren_up");
            assertEquals(3.63f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LAIe_Warren_up_err");
            assertEquals(.14f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAI_Miller");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAI_Miller_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAI_Warren");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAI_Warren_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAIe_Miller");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAIe_Miller_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAIe_Warren");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "PAIe_Warren_err");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Miller_down");
            assertEquals(.93f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Miller_down_err");
            assertEquals(.04f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Miller_up");
            assertEquals(.805f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Miller_up_err");
            assertEquals(.029f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Warren_down");
            assertEquals(.92f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Warren_down_err");
            assertEquals(.06f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Warren_up");
            assertEquals(.79f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "clumping_Warren_up_err");
            assertEquals(.04f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LSE");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LSE_STD");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LSR");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LSR_STD");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LST");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "LST_STD");
            assertEquals(-999.f, array.getFloat(0), 1e-8);
            assertEquals(DataType.FLOAT, array.getDataType());

            array = reader.readRaw(12, 0, new Interval(1, 1), "SM_5");
            assertEquals(-999., array.getDouble(0), 1e-8);
            assertEquals(DataType.DOUBLE, array.getDataType());


            array = reader.readRaw(12, 2, new Interval(1, 1), "RM6_down_flag");
            assertEquals((short) 8, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "RM6_up_flag");
            assertEquals((short) 8, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "RM7_down_flag");
            assertEquals((short) 8, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "RM7_up_flag");
            assertEquals((short) 8, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "QC_LSE");
            assertEquals((short) -999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "QC_LSR");
            assertEquals((short) -999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "QC_LST");
            assertEquals((short) -999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());

            array = reader.readRaw(12, 2, new Interval(1, 1), "QC_SM_5");
            assertEquals((short) -999, array.getShort(0));
            assertEquals(DataType.SHORT, array.getDataType());


        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_noVarFound() throws InvalidRangeException,IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);

            Array array = reader.readScaled(7, 0, new Interval(1, 1), "XX");
            assertNull(array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime() throws IOException, InvalidRangeException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(13, 2, new Interval(1, 1));
            NCTestUtils.assertValueAt(1467072000, 0, 0, acquisitionTime);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables() throws InvalidRangeException, IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);

            List<Variable> vars = reader.getVariables();

            assertNotNull(vars);
            assertFalse(vars.isEmpty());
            assertEquals(67, vars.size());

            StringVariable stringVar = (StringVariable) vars.get(0);
            assertEquals("site", stringVar.getShortName());
            assertEquals(DataType.STRING, stringVar.getDataType());
            assertEquals(2, stringVar.getAttributes().size());

            stringVar = (StringVariable) vars.get(1);
            assertEquals("station", stringVar.getShortName());
            assertEquals(DataType.STRING, stringVar.getDataType());
            assertEquals(3, stringVar.getAttributes().size());

            VariableProxy var = (VariableProxy) vars.get(2);
            assertEquals("elevation", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            stringVar = (StringVariable) vars.get(3);
            assertEquals("IGBP_class", stringVar.getShortName());
            assertEquals(DataType.STRING, stringVar.getDataType());
            assertEquals(2, stringVar.getAttributes().size());

            var = (VariableProxy) vars.get(4);
            assertEquals("Lat_IS", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(5);
            assertEquals("Lon_IS", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());


            var = (VariableProxy) vars.get(6);
            assertEquals("TIME_IS", var.getFullName());
            assertEquals(DataType.INT, var.getDataType());
            assertEquals(3, var.getAttributes().size());


            var = (VariableProxy) vars.get(7);
            assertEquals("FIPAR_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(8);
            assertEquals("FIPAR_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(9);
            assertEquals("FIPAR_total", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(10);
            assertEquals("FIPAR_total_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(11);
            assertEquals("FIPAR_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(12);
            assertEquals("FIPAR_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(13);
            assertEquals("RM6_down_flag", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(14);
            assertEquals("RM6_up_flag", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());


            var = (VariableProxy) vars.get(15);
            assertEquals("Clumping_Miller", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(16);
            assertEquals("Clumping_Miller_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(17);
            assertEquals("Clumping_Warren", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(18);
            assertEquals("Clumping_Warren_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(19);
            assertEquals("LAI_Miller_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(20);
            assertEquals("LAI_Miller_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(21);
            assertEquals("LAI_Miller_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(22);
            assertEquals("LAI_Miller_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(23);
            assertEquals("LAI_Warren_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(24);
            assertEquals("LAI_Warren_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(25);
            assertEquals("LAI_Warren_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(26);
            assertEquals("LAI_Warren_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(27);
            assertEquals("LAI_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(28);
            assertEquals("LAI_total_Miller", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(29);
            assertEquals("LAI_total_Warren", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(30);
            assertEquals("LAIe_Miller_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(31);
            assertEquals("LAIe_Miller_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(32);
            assertEquals("LAIe_Miller_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(33);
            assertEquals("LAIe_Miller_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(34);
            assertEquals("LAIe_Warren_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(35);
            assertEquals("LAIe_Warren_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(36);
            assertEquals("LAIe_Warren_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(37);
            assertEquals("LAIe_Warren_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(38);
            assertEquals("PAI_Miller", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(39);
            assertEquals("PAI_Miller_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(40);
            assertEquals("PAI_Warren", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(41);
            assertEquals("PAI_Warren_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(42);
            assertEquals("PAIe_Miller", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(43);
            assertEquals("PAIe_Miller_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(44);
            assertEquals("PAIe_Warren", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(45);
            assertEquals("PAIe_Warren_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(46);
            assertEquals("clumping_Miller_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(47);
            assertEquals("clumping_Miller_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(48);
            assertEquals("clumping_Miller_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(49);
            assertEquals("clumping_Miller_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(50);
            assertEquals("clumping_Warren_down", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(51);
            assertEquals("clumping_Warren_down_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(52);
            assertEquals("clumping_Warren_up", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(53);
            assertEquals("clumping_Warren_up_err", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(54);
            assertEquals("RM7_down_flag", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(55);
            assertEquals("RM7_up_flag", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(4, var.getAttributes().size());


            var = (VariableProxy) vars.get(56);
            assertEquals("LSE", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(57);
            assertEquals("LSE_STD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(58);
            assertEquals("LSR", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(59);
            assertEquals("LSR_STD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(60);
            assertEquals("QC_LSE", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(61);
            assertEquals("QC_LSR", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());


            var = (VariableProxy) vars.get(62);
            assertEquals("LST", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(4, var.getAttributes().size());

            var = (VariableProxy) vars.get(63);
            assertEquals("LST_STD", var.getFullName());
            assertEquals(DataType.FLOAT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(64);
            assertEquals("QC_LST", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());


            var = (VariableProxy) vars.get(65);
            assertEquals("QC_SM_5", var.getFullName());
            assertEquals(DataType.SHORT, var.getDataType());
            assertEquals(3, var.getAttributes().size());

            var = (VariableProxy) vars.get(66);
            assertEquals("SM_5", var.getFullName());
            assertEquals(DataType.DOUBLE, var.getDataType());
            assertEquals(4, var.getAttributes().size());


        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetProductSize() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            Dimension productSize = reader.getProductSize();
            assertEquals(3, productSize.getNy());
        } finally {
            reader.close();
        }
    }

    @Test
    public void test_getRegex_GBOV() {
        assertEquals("^GBOV__(.*?)__(.*?)__([0-9]{8}T[0-9]{6}Z)__([0-9]{8}T[0-9]{6}Z)\\.csv$", reader.getRegEx());
    }

    @Test
    public void testCoordinateVariableNames() throws IOException {
        final File testFile = getGBOV();

        try {
            reader.open(testFile);
            String longitudeVariableName = reader.getLongitudeVariableName();
            String latitudeVariableName = reader.getLatitudeVariableName();
            assertEquals("Lon_IS", longitudeVariableName);
            assertEquals("Lat_IS", latitudeVariableName);
        } finally {
            reader.close();
        }
    }

    private static File getGBOV() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "gbov", "v1", "2016", "06", "GBOV__Bartlett--Experimental--Forest__BART_047__20160601T000000Z__20160628T000000Z.csv"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
