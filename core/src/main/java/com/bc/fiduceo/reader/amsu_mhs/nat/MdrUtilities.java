package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;

import java.util.ArrayList;
import java.util.List;

public class MdrUtilities {

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
