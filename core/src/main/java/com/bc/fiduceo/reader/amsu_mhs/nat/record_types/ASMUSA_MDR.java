package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;

public class ASMUSA_MDR extends MDR {

    public ASMUSA_MDR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }
}
