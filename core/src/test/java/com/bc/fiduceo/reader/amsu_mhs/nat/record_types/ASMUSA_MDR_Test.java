package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import org.apache.commons.lang3.Conversion;
import org.junit.Test;

public class ASMUSA_MDR_Test {



    @Test
    public void testGetVariable() {
        final byte[] payload = new byte[3464];
        payload[0] = (byte) 8;  // MDR
        payload[1] = (byte) 1;  // AMSUA
        payload[4] = (byte) 20;  // RECORD_SIZE

        int writePointer = 2082; // start of EARTH_LOCATION
        for (int i = 0; i < 30; i++) {
            int offset = i * 8; // two times four bytes [latitude,longitude]
            Conversion.intToByteArray(i, 0, payload, writePointer + offset, 4);
            Conversion.intToByteArray(100 + i, 0, payload, writePointer + offset + 4, 4);
        }

         final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(payload);

        final ASMUSA_MDR asmsuaMdr = new ASMUSA_MDR(recordHeader, payload);
        final int[] rawData = asmsuaMdr.parseVariable("longitude");
    }
}
