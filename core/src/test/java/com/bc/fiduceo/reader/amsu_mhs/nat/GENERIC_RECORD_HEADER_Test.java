package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GENERIC_RECORD_HEADER_Test {

    @Test
    public void testParse() {
        final byte[] data = {1, 9, 0, 0, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER genericHeader = GENERIC_RECORD_HEADER.parse(data);

        assertNotNull(genericHeader);
        assertEquals(RECORD_CLASS.MPHR, genericHeader.getRecordClass());
        assertEquals(INSTRUMENT_GROUP.MHS, genericHeader.getInstrumentGroup());
        assertEquals(100, genericHeader.getRecordSize());
    }
}
