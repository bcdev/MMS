package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.Assert.*;

public class MPHRTest {

    @Test
    public void testExtractStartAndEndTime() throws IOException {
        final byte[] payLoad = new byte[3307];
        String sensingStart = "SENSING_START                 = 20250820060350Z";
        String sensingEnd = "SENSING_END                   = 20250820074550Z";

        System.arraycopy(sensingStart.getBytes(), 0, payLoad, 700, sensingStart.getBytes().length);
        System.arraycopy(sensingEnd.getBytes(), 0, payLoad, 748, sensingStart.getBytes().length);
        MPHR mphr = new MPHR(null, payLoad);

        Date sensingStartDate = mphr.getDate("SENSING_START");
        Date sensingEndDate = mphr.getDate("SENSING_END");

        assertEquals(1755669830000L, sensingStartDate.getTime());
        assertEquals(1755675950000L, sensingEndDate.getTime());
        assertTrue(sensingStartDate.before(sensingEndDate));
    }

    @Test
    public void testParseError() {
        final byte[] payLoad = new byte[3307];
        String sensingStart = "SENSING_START                 = 20250820060350X";
        String sensingEnd = "SENSING_END                   = 20250820074550X";

        System.arraycopy(sensingStart.getBytes(), 0, payLoad, 700, sensingStart.getBytes().length);
        System.arraycopy(sensingEnd.getBytes(), 0, payLoad, 748, sensingStart.getBytes().length);
        MPHR mphr = new MPHR(null, payLoad);

        Exception ex = assertThrows(IOException.class, () -> mphr.getDate("SENSING_START"));
        assertEquals("Could not parse time: 20250820060350X", ex.getMessage());
    }

    @Test
    public void testGetDate_invalidKey() {
        final byte[] payLoad = new byte[3307];
        MPHR mphr = new MPHR(null, payLoad);

        try {
            mphr.getDate("FANCY_DATE");
            fail("IllegalStateException expected");
        } catch (IllegalStateException | IOException expected) {
        }
    }

    @Test
    public void testInvalidPayload() {
        final byte[] payLoad = new byte[3307];
        MPHR mphr = new MPHR(null, payLoad);

        Exception ex = assertThrows(IllegalStateException.class, () -> mphr.getDate("SENSING_START"));
        assertEquals("Invalid attribute formatting: ", ex.getMessage());
    }

    @Test
    public void testGetProductName() {
        final byte[] payLoad = new byte[3307];
        final String productName = "PRODUCT_NAME                  = The_thing_how_we_call_it_version7";
        // @todo 2 tb/tb speaking constants! 2025-08-25
        byte[] productNameBytes = productName.getBytes();
        System.arraycopy(productNameBytes, 0, payLoad, 20, productNameBytes.length);
        final MPHR mphr = new MPHR(null, payLoad);

        assertEquals("The_thing_how_we_call_it_version7", mphr.getProductName());
    }
}