package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.Assert.*;

public class MPHRTest {

    @Test
    public void testExtractStartAndEndTime() throws IOException {
        String times = "SENSING_START                 = 20250820060350Z\n" +
                       "SENSING_END                   = 20250820074550Z\n";
        byte[] payload = createDummyPayload(times);
        MPHR mphr = new MPHR(null, payload);

        Date sensingStart = mphr.getDate("SENSING_START");
        Date sensingEnd = mphr.getDate("SENSING_END");

        assertNotNull(sensingStart);
        assertNotNull(sensingEnd);
        assertTrue(sensingStart.before(sensingEnd));
    }

    @Test
    public void testParseError() {
        String times = "SENSING_START                 = 20250820060350X\n" +
                       "SENSING_END                   = 20250820074550X\n";
        byte[] payload = createDummyPayload(times);
        MPHR mphr = new MPHR(null, payload);

        Exception ex = assertThrows(IOException.class, () -> mphr.getDate("SENSING_START"));
        assertEquals("Could not parse SENSING_START time: 20250820060350X", ex.getMessage());
    }

    @Test
    public void testInvalidPayload() {
        byte[] payload = createDummyPayload("");
        MPHR mphr = new MPHR(null, payload);

        Exception ex = assertThrows(IOException.class, () -> mphr.getDate("SENSING_START"));
        assertEquals("SENSING_START not found in MPHR payload", ex.getMessage());
    }

    private byte[] createDummyPayload(String times) {
        String payloadText =
                "INSTRUMENT_MODEL              =   1\n" +
                        "PRODUCT_TYPE                  = xxx\n" +
                        "PROCESSING_LEVEL              = 1B\n" +
                        "SPACECRAFT_ID                 = M03\n" +
                         times +
                        "SENSING_START_THEORETICAL     = 20250820060300Z\n" +
                        "SENSING_END_THEORETICAL       = 20250820074500Z\n" +
                        "PROCESSING_CENTRE             = CGS1\n" +
                        "PROCESSOR_MAJOR_VERSION       =     1\n" +
                        "PROCESSOR_MINOR_VERSION       =     0\n" +
                        "FORMAT_MAJOR_VERSION          =    10\n" +
                        "FORMAT_MINOR_VERSION          =     0\n" +
                        "PROCESSING_TIME_START         = 20250820074043Z\n" +
                        "PROCESSING_TIME_END           = 20250820092115Z\n" +
                        "PROCESSING_MODE               = N\n" +
                        "DISPOSITION_MODE              = O\n" +
                        "RECEIVING_GROUND_STATION      = SVL\n" +
                        "RECEIVE_TIME_START            = 20250820073741Z\n" +
                        "RECEIVE_TIME_END              = 20250820091905Z\n";

        return payloadText.getBytes(StandardCharsets.US_ASCII);
    }
}