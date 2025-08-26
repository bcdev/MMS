package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.*;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.reader.amsu_mhs.nat.INSTRUMENT_GROUP.AMSUA;

public class RecordFactory {

    public static List<Record> parseRecords(byte[] allBytes) {
        List<Record> records = new ArrayList<>();
        int index = 0;

        while (index < allBytes.length) {
            byte[] headerBytes = new byte[EPS_Constants.GENERIC_RECORD_HEADER_SIZE];
            System.arraycopy(allBytes, index, headerBytes, 0, EPS_Constants.GENERIC_RECORD_HEADER_SIZE);

            GENERIC_RECORD_HEADER header = GENERIC_RECORD_HEADER.parse(headerBytes);
            int recordSize = header.getRecordSize();
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
            case SPHR:
                return new SPHR(header, payload);
            case IPR:
                return new IPR(header, payload);
            case GEADR:
                return new GEADR(header, payload);
            case GIADR:
                return new GIADR(header, payload);
            case VEADR:
                return new VEADR(header, payload);
            case VIADR:
                return new VIADR(header, payload);
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
