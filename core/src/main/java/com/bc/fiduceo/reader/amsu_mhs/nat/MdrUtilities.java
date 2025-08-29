package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;

import java.util.ArrayList;
import java.util.List;

public class MdrUtilities {

    public static MPHR getMphr(List<Record> records) {
        return (MPHR) records.get(0);
    }

    public static List<MDR> getMdrList(List<Record> records) {
        List<MDR> mdrs = new ArrayList<>();

        for (Record record : records) {
            GENERIC_RECORD_HEADER header = record.getHeader();
            RECORD_CLASS recordClass = header.getRecordClass();

            if (recordClass == RECORD_CLASS.MDR) {
                mdrs.add((MDR) record);
            }
        }
        return mdrs;
    }
}
