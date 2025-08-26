package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;

public class GEADR extends Record {

    public GEADR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }
}
