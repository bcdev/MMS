package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.reader.amsu_mhs.AMSUA_L1B_Reader;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class EpsVariableCache_IO_Test {

    EpsVariableCache cache;

    @Before
    public void setUp() throws Exception {
        final Path path = Paths.get(getClass().getResource("/com/bc/fiduceo/reader/testFile_amsua_l1b.nat").toURI());
        final byte[] rawDataBuffer = Files.readAllBytes(path);
        final VariableRegistry registry = VariableRegistry.load(AMSUA_L1B_Reader.RESOURCE_KEY);

        cache = new EpsVariableCache(rawDataBuffer, registry, EPS_Constants.AMSUA_FOV_COUNT);
    }

    @After
    public void tearDown() throws Exception {
        cache.clear();
        cache = null;
    }

    @Test
    public void test_getMphr() {
        final MPHR mphr = cache.getMPHR();

        assertNotNull(mphr);
        assertEquals(RECORD_CLASS.MPHR, mphr.getHeader().getRecordClass());
        assertEquals(3307, mphr.getPayload().length);
    }

    @Test
    public void test_getMdrs() {
        final List<MDR> mdrs = cache.getMdrs();

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

        // @todo 2 tb/tb check which data type is it originally
        assertEquals(-1680057, longitude.getInt(0));
        assertEquals(1771737, longitude.getInt(10));
        assertEquals(1646190, longitude.getInt(20));
    }

    @Test
    public void test_getSceneRadiance_02() {
        Array radiance = cache.getRaw("SCENE_RADIANCE_02");

        assertNotNull(radiance);
        assertEquals(2, radiance.getRank());

        final int height = radiance.getShape()[0];
        final int width = radiance.getShape()[1];
        assertEquals(cache.getMdrs().size(), height);
        assertEquals(EPS_Constants.AMSUA_FOV_COUNT, width);

        // @todo 2 tb/tb check which data type is it originally
        assertEquals(21796, radiance.getInt(1));
        assertEquals(22334, radiance.getInt(11));
        assertEquals(22465, radiance.getInt(21));
    }

    @Test
    public void test_getScaled_latitude() {
        Array latitude = cache.getScaled("latitude");

        assertNotNull(latitude);
        assertEquals(2, latitude.getRank());

        final int height = latitude.getShape()[0];
        final int width = latitude.getShape()[1];
        assertEquals(cache.getMdrs().size(), height);
        assertEquals(EPS_Constants.AMSUA_FOV_COUNT, width);

        assertEquals(65.5792, latitude.getDouble(0), 1e-8);
        assertEquals(70.2679, latitude.getDouble(10), 1e-8);
        assertEquals(72.2871, latitude.getDouble(20), 1e-8);
    }
}