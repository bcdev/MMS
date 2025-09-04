package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DATA_LAYOUT_Test {

    @Test
    public void testFromString() {
        assertEquals(DATA_LAYOUT.ARRAY, DATA_LAYOUT.fromString("ARRAY"));
        assertEquals(DATA_LAYOUT.VECTOR, DATA_LAYOUT.fromString("VECTOR"));
    }

    @Test
    public void testFromString_invalidArgument() {
        try {
            DATA_LAYOUT.fromString("HOLY_GRAIL");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testToString() {
        assertEquals("ARRAY", DATA_LAYOUT.toString(DATA_LAYOUT.ARRAY));
        assertEquals("VECTOR", DATA_LAYOUT.toString(DATA_LAYOUT.VECTOR));
    }
}
