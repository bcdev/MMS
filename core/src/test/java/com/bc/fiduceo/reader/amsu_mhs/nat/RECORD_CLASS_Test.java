package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static com.bc.fiduceo.reader.amsu_mhs.nat.RECORD_CLASS.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RECORD_CLASS_Test {

    @Test
    public void testFromByte() {
        assertEquals(RESERVED, RECORD_CLASS.fromByte((byte) 0));
        assertEquals(MPHR, RECORD_CLASS.fromByte((byte) 1));
        assertEquals(SPHR, RECORD_CLASS.fromByte((byte) 2));
        assertEquals(IPR, RECORD_CLASS.fromByte((byte) 3));
        assertEquals(GEADR, RECORD_CLASS.fromByte((byte) 4));
        assertEquals(GIADR, RECORD_CLASS.fromByte((byte) 5));
        assertEquals(VEADR, RECORD_CLASS.fromByte((byte) 6));
        assertEquals(VIADR, RECORD_CLASS.fromByte((byte) 7));
        assertEquals(MDR, RECORD_CLASS.fromByte((byte) 8));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testFromByte_invalid() {
        try {
            RECORD_CLASS.fromByte((byte) -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected){
        }

        try {
            RECORD_CLASS.fromByte((byte) 9);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

}
