package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static org.junit.Assert.*;

public class DATA_TYPETest {

    @Test
    public void test_fromString() {
        DATA_TYPE sByte = DATA_TYPE.fromString("byte");
        DATA_TYPE uByte = DATA_TYPE.fromString("u-byte");
        DATA_TYPE enumerated = DATA_TYPE.fromString("enumerated");
        DATA_TYPE bool = DATA_TYPE.fromString("boolean");
        DATA_TYPE int2 = DATA_TYPE.fromString("integer2");
        DATA_TYPE uInt2 = DATA_TYPE.fromString("u-integer2");
        DATA_TYPE int4 = DATA_TYPE.fromString("integer4");
        DATA_TYPE uInt4 = DATA_TYPE.fromString("u-integer4");
        DATA_TYPE int8 = DATA_TYPE.fromString("integer8");

        assertEquals(DATA_TYPE.BYTE, sByte);
        assertEquals(DATA_TYPE.U_BYTE, uByte);
        assertEquals(DATA_TYPE.ENUMERATED, enumerated);
        assertEquals(DATA_TYPE.BOOLEAN, bool);
        assertEquals(DATA_TYPE.INTEGER2, int2);
        assertEquals(DATA_TYPE.U_INTEGER2, uInt2);
        assertEquals(DATA_TYPE.INTEGER4, int4);
        assertEquals(DATA_TYPE.U_INTEGER4, uInt4);
        assertEquals(DATA_TYPE.INTEGER8, int8);
    }

    @Test
    public void test_fromString_failing() {
        try {
            DATA_TYPE dt = DATA_TYPE.fromString("xyz");
            fail("Exception expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Unknown data type: xyz", expected.getMessage());
        }
    }
}