package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class VariableCacheTest {

    private VariableCache variableCache;

    @Before
    public void setUp() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5RootDir = new File(testDataDirectory, "era-5" + File.separator + "v1");
        assertTrue(era5RootDir.isDirectory());

        final Configuration config = new Configuration();
        config.setNWPAuxDir(era5RootDir.getAbsolutePath());
        final Era5Archive era5Archive = new Era5Archive(config, Era5Collection.ERA_5);

        variableCache = new VariableCache(era5Archive, 3);
    }

    @After
    public void tearDown() throws IOException {
        variableCache.close();
    }

    @Test
    public void testGet() throws IOException {
        Variable variable;

        variable = variableCache.get("an_ml_lnsp", 1212145200);
        assertEquals("time latitude longitude", variable.getDimensionsString());
        assertEquals("Logarithm of surface pressure", NetCDFUtils.getAttributeString(variable, "long_name", null));

        int[] shape = variable.getShape();
        assertArrayEquals(new int[]{1, 721, 1440}, shape);
        assertEquals(3, shape.length);
        assertEquals(DataType.SHORT,  variable.getDataType());

        variable = variableCache.get("an_ml_q", 1212145200);
        assertEquals("time level latitude longitude", variable.getDimensionsString());
        assertEquals(3.786489628510026E-7, NetCDFUtils.getAttributeFloat(variable, "scale_factor", Float.NaN), 1e-8);

        shape = variable.getShape();
        assertArrayEquals(new int[]{1, 137, 721, 1440}, shape);
        assertEquals(4, shape.length);
        assertEquals(DataType.SHORT, variable.getDataType());
    }

    @Test
    public void testCallGetTwice() throws IOException {
        final Variable variable_1 = variableCache.get("an_ml_o3", 1212400800);

        final Variable variable_2 = variableCache.get("an_ml_o3", 1212400800);

        assertSame(variable_1, variable_2);
    }

    @Test
    public void testCallGetTwice_closeInbetween() throws IOException {
        final Variable variable_1 = variableCache.get("an_sfc_t2m", 1212145200);

        variableCache.close();

        final Variable variable_2 = variableCache.get("an_sfc_t2m", 1212145200);

        assertNotSame(variable_1, variable_2);
    }

    @Test
    public void testGet_removeFunctionalityOnFullCache() throws IOException, InterruptedException {
        final int secondsOfOneHour = 60 * 60;
        final int era5TimeStamp_start = 959533200; // timestamp for 17:00 2000-05-28
        int era5TimeStamp = era5TimeStamp_start;

        Variable cached_u10 = variableCache.get("an_sfc_u10", era5TimeStamp);

        era5TimeStamp += secondsOfOneHour;
        variableCache.get("an_sfc_u10", era5TimeStamp);  // one hour later

        era5TimeStamp += secondsOfOneHour;
        variableCache.get("an_sfc_u10", era5TimeStamp);  // one hour later

        // now the cache is full tb 2020-11-25
        era5TimeStamp += secondsOfOneHour;
        variableCache.get("an_sfc_u10", era5TimeStamp);  // one hour later

        // now the first u10 variable is removed from cache and opening it again must result in a new object tb 2020-11-25
        Variable cached_u10_new = variableCache.get("an_sfc_u10", era5TimeStamp_start);

        assertNotSame(cached_u10, cached_u10_new);
    }
}
