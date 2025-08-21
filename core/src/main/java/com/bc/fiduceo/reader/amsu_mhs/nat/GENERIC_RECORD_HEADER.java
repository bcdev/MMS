package com.bc.fiduceo.reader.amsu_mhs.nat;

import java.nio.ByteBuffer;

public class GENERIC_RECORD_HEADER {

    private final RECORD_CLASS recordClass;
    private final INSTRUMENT_GROUP instrumentGroup;
    private final int recordSize;

    GENERIC_RECORD_HEADER(RECORD_CLASS recordClass, INSTRUMENT_GROUP instrumentGroup, int recordSize) {
        this.recordClass = recordClass;
        this.instrumentGroup = instrumentGroup;
        this.recordSize = recordSize;
    }

    public static GENERIC_RECORD_HEADER parse(byte[] data) {
        final RECORD_CLASS recordClass = RECORD_CLASS.fromByte(data[0]);
        final INSTRUMENT_GROUP instrumentGroup = INSTRUMENT_GROUP.fromByte(data[1]);

        final ByteBuffer buffer = ByteBuffer.wrap(data,4,4);
        final int recordSize = buffer.getInt();
        return new GENERIC_RECORD_HEADER(recordClass, instrumentGroup, recordSize);
    }

    public RECORD_CLASS getRecordClass() {
        return recordClass;
    }

    public INSTRUMENT_GROUP getInstrumentGroup() {
        return instrumentGroup;
    }

    public int getRecordSize() {
        return recordSize;
    }
}
