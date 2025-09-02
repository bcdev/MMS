package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

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

    @Test
    public void testGetRegEx() {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        final String regEx = reader.getRegEx();
        assertEquals("AMSA_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ABCD_xxx_1C_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1B_M04_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName()  {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName()  {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        assertEquals("latitude", reader.getLatitudeVariableName());
    }
}
