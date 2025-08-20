package com.bc.fiduceo.reader.amsu_mhs.nat;

public class GENERIC_RECORD_HEADER {

    private final RECORD_CLASS recordClass;
    private final INSTRUMENT_GROUP instrumentGroup;

    GENERIC_RECORD_HEADER(RECORD_CLASS recordClass, INSTRUMENT_GROUP instrumentGroup) {
        this.recordClass = recordClass;
        this.instrumentGroup = instrumentGroup;
    }

    public static GENERIC_RECORD_HEADER parse(byte[] data) {
        final RECORD_CLASS recordClass = RECORD_CLASS.fromByte(data[0]);
        final INSTRUMENT_GROUP instrumentGroup = INSTRUMENT_GROUP.fromByte(data[1]);
        return new GENERIC_RECORD_HEADER(recordClass, instrumentGroup);
    }

    public RECORD_CLASS getRecordClass() {
        return recordClass;
    }

    public INSTRUMENT_GROUP getInstrumentGroup() {
        return instrumentGroup;
    }
}
