package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;

public class IPR extends Record {

    public IPR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }
}
