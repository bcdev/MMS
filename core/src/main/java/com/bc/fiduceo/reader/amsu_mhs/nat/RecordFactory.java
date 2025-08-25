package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class RecordFactory {

    public static List<Record> parseRecordsForIngestion(byte[] allBytes) {
        List<Record> records = new ArrayList<>();
        EnumSet<RECORD_CLASS> recordClassesNeededForIngestion = EnumSet.of(RECORD_CLASS.MPHR, RECORD_CLASS.MDR);
        int index = 0;

        while (index < allBytes.length) {
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
                return new MDR(header, payload);
            default:
                return new Record(header, payload);
        }
    }
}
