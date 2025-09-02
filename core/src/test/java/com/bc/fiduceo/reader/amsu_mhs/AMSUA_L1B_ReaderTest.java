package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import org.junit.Test;

import static org.junit.Assert.fail;

public class AMSUA_L1B_ReaderTest {

    @Test
    public void testEnsureMdrVersionSupported() {
        final byte[] data = {8, 1, 2, 3, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
        } catch (IllegalStateException e) {
            fail("no exception expected");
        }
    }

    @Test
    public void testEnsureMdrVersionSupported_wrongVersion() {
        // ----------------------- ! ----------------
        final byte[] data = {8, 1, 3, 3, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEnsureMdrVersionSupported_wrongSubVersion() {
        // -------------------------- ! -------------
        final byte[] data = {8, 1, 2, 5, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }
}
