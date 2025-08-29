package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.AMSUA_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class EpsVariableCacheTest {

    EpsVariableCache cache;

    @Before
    public void setUp() throws Exception {
        Path path = Paths.get(getClass().getResource("/com/bc/fiduceo/reader/testFile_amsua_l1b.nat").toURI());
        final byte[] rawDataBuffer = Files.readAllBytes(path);
        VariableRegistry registry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        cache = new EpsVariableCache(rawDataBuffer, registry, EPS_Constants.AMSUA_FOV_COUNT);
    }

    @After
    public void tearDown() throws Exception {
        cache.clear();
        cache = null;
    }

    @Test
    public void test_getMphr() {
        MPHR mphr = cache.getMPHR();

        assertNotNull(mphr);
        assertEquals(RECORD_CLASS.MPHR, mphr.getHeader().getRecordClass());
        assertEquals(3307, mphr.getPayload().length);
    }

    @Test
    public void test_getMdrs() {
        List<MDR> mdrs = cache.getMdrs();

        assertNotNull(mdrs);
        assertFalse(mdrs.isEmpty());
        assertEquals(765, mdrs.size());
    }

    @Test
    public void test_getRaw_longitude() {
        Array longitude = cache.getRaw("longitude");

        assertNotNull(longitude);
        assertEquals(2, longitude.getRank());
        assertEquals(cache.getMdrs().size(), longitude.getShape()[0]);
        assertEquals(EPS_Constants.AMSUA_FOV_COUNT, longitude.getShape()[1]);
        double first = longitude.getDouble(0);
        assertEquals(-1680057, first, 0.001);
    }
}