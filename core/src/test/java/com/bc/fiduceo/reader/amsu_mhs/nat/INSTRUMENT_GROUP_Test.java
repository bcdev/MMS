package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static com.bc.fiduceo.reader.amsu_mhs.nat.INSTRUMENT_GROUP.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class INSTRUMENT_GROUP_Test {

    @Test
    public void testFromByte() {
       assertEquals(GENERIC, INSTRUMENT_GROUP.fromByte((byte) 0));
       assertEquals(AMSUA, INSTRUMENT_GROUP.fromByte((byte) 1));
       assertEquals(ASCAT, INSTRUMENT_GROUP.fromByte((byte) 2));
       assertEquals(ATOVS, INSTRUMENT_GROUP.fromByte((byte) 3));
       assertEquals(AVHRR3, INSTRUMENT_GROUP.fromByte((byte) 4));
       assertEquals(GOME, INSTRUMENT_GROUP.fromByte((byte) 5));
       assertEquals(GRAS, INSTRUMENT_GROUP.fromByte((byte) 6));
       assertEquals(HIRS4, INSTRUMENT_GROUP.fromByte((byte) 7));
       assertEquals(IASI, INSTRUMENT_GROUP.fromByte((byte) 8));
       assertEquals(MHS, INSTRUMENT_GROUP.fromByte((byte) 9));
       assertEquals(SEM, INSTRUMENT_GROUP.fromByte((byte) 10));
       assertEquals(ADCS, INSTRUMENT_GROUP.fromByte((byte) 11));
       assertEquals(SBUV, INSTRUMENT_GROUP.fromByte((byte) 12));
       assertEquals(DUMMY, INSTRUMENT_GROUP.fromByte((byte) 13));
       assertEquals(IASI_L2, INSTRUMENT_GROUP.fromByte((byte) 15));
       assertEquals(ARCHIVE, INSTRUMENT_GROUP.fromByte((byte) 99));
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testFromByte_invalid() {
        try {
            INSTRUMENT_GROUP.fromByte((byte) -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected){
        }

        try {
            INSTRUMENT_GROUP.fromByte((byte) 14);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected){
        }

        try {
            INSTRUMENT_GROUP.fromByte((byte) 68);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected){
        }
    }

}
