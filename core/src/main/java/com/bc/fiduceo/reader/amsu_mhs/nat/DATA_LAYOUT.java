package com.bc.fiduceo.reader.amsu_mhs.nat;

public enum DATA_LAYOUT {

    VECTOR,
    ARRAY;

    public static DATA_LAYOUT fromString(String dataLayout) {
        if (dataLayout.equals("ARRAY")) {
            return DATA_LAYOUT.ARRAY;
        } else if (dataLayout.equals("VECTOR")) {
            return DATA_LAYOUT.VECTOR;
        } else {
            throw new IllegalArgumentException("Unsupported data layout: " + dataLayout);
        }
    }

    public static String toString(DATA_LAYOUT dataLayout) {
        if (dataLayout == DATA_LAYOUT.VECTOR) {
            return "VECTOR";
        } else if (dataLayout == DATA_LAYOUT.ARRAY) {
            return "ARRAY";
        } else {
            throw new IllegalArgumentException("Unsupported data layout: " + dataLayout);
        }
    }
}
