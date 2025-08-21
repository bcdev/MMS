package com.bc.fiduceo.reader.amsu_mhs.nat;

public class Record {

    private final GENERIC_RECORD_HEADER header;
    private final byte[] payload;

    public Record(GENERIC_RECORD_HEADER header, byte[] payload) {
        this.header = header;
        this.payload = payload;
    }

    public GENERIC_RECORD_HEADER getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }
}
