package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;

import java.util.List;
import java.util.stream.Collectors;

public class MdrUtilities {

    public static List<MDR> getMdrList(List<Record> records) {
        return records.subList(1, records.size())
                .stream()
                .map(r -> (MDR) r)
                .collect(Collectors.toList());
    }
}
