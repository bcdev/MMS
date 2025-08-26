package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.ReaderContext;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MHS_L1B_ReaderTest {

    private MHS_L1B_Reader reader;

    @Before
    public void setUp() {
        reader = new MHS_L1B_Reader(new ReaderContext());
    }

    @Test
    public void testGetRegEx() {
        final String regEx = reader.getRegEx();
        assertEquals("MHSx_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ABCD_xxx_1C_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1C_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1B_M04_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());
    }
}