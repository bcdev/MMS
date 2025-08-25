package com.bc.fiduceo.reader.amsu_mhs.nat;

import java.nio.ByteBuffer;

public class GENERIC_RECORD_HEADER {

    private final RECORD_CLASS recordClass;
    private final INSTRUMENT_GROUP instrumentGroup;
    private final int recordSize;
    private final byte recordSubClass;
    private final byte recordSubClassVersion;

    GENERIC_RECORD_HEADER(RECORD_CLASS recordClass, INSTRUMENT_GROUP instrumentGroup, byte recordSubClass, byte recordSubClassVersion, int recordSize) {
        this.recordClass = recordClass;
        this.instrumentGroup = instrumentGroup;
        this.recordSize = recordSize;
        this.recordSubClass = recordSubClass;
        this.recordSubClassVersion = recordSubClassVersion;
    }

    public static GENERIC_RECORD_HEADER parse(byte[] data) {
        final RECORD_CLASS recordClass = RECORD_CLASS.fromByte(data[0]);
        final INSTRUMENT_GROUP instrumentGroup = INSTRUMENT_GROUP.fromByte(data[1]);
        final byte recordSubClass = data[2];
        final byte recordSubClassVersion = data[3];

        final ByteBuffer buffer = ByteBuffer.wrap(data,4,4);
        final int recordSize = buffer.getInt();
        return new GENERIC_RECORD_HEADER(recordClass, instrumentGroup, recordSubClass,recordSubClassVersion, recordSize);
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

    public byte getRecordSubClass() {
        return recordSubClass;
    }

    public byte getRecordSubClassVersion() {
        return recordSubClassVersion;
    }
}
