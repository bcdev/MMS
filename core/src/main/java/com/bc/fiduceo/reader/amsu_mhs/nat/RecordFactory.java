package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.ASMUSA_MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.bc.fiduceo.reader.amsu_mhs.nat.INSTRUMENT_GROUP.AMSUA;

public class RecordFactory {

    public static List<Record> parseRecordsForIngestion(byte[] allBytes) {
        List<Record> records = new ArrayList<>();
        EnumSet<RECORD_CLASS> recordClassesNeededForIngestion = EnumSet.of(RECORD_CLASS.MPHR, RECORD_CLASS.MDR);
        int index = 0;

        while (index < allBytes.length) {
            // @todo 2 tb/* introduce named constant? 2025-08-22
            byte[] headerBytes = new byte[20];
            System.arraycopy(allBytes, index, headerBytes, 0, 20);

            GENERIC_RECORD_HEADER header = GENERIC_RECORD_HEADER.parse(headerBytes);
            int recordSize = header.getRecordSize();
            if (!recordClassesNeededForIngestion.contains(header.getRecordClass())) {
                index += recordSize;
                continue;
            }

            byte[] payload = new byte[recordSize];
            System.arraycopy(allBytes, index, payload, 0, recordSize);

            Record record = createRecord(header, payload);
            records.add(record);

            index += recordSize;
        }

        return records;
    }

    public static Record createRecord(GENERIC_RECORD_HEADER header, byte[] payload) {
        switch (header.getRecordClass()) {
            case MPHR:
                return new MPHR(header, payload);
            case MDR:
                return createMDR(header, payload);
            default:
                return new Record(header, payload);
        }
    }

    private static MDR createMDR(GENERIC_RECORD_HEADER header, byte[] payload) {
        if (header.getInstrumentGroup() == AMSUA) {
            return new ASMUSA_MDR(header, payload);
        }

        return new MDR(header, payload);
    }
}
