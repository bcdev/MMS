package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;

public class VEADR extends Record {

    public VEADR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }
}
