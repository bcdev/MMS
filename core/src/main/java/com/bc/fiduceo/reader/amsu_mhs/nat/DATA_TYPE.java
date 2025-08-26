package com.bc.fiduceo.reader.amsu_mhs.nat;

public enum DATA_TYPE {
    BYTE(1),
    U_BYTE(1),
    ENUMERATED(1),
    BOOLEAN(1),
    INTEGER2(2),
    U_INTEGER2(2),
    INTEGER4(4),
    U_INTEGER4(4),
    INTEGER8(8);

    private final int size;

    DATA_TYPE(int size) { this.size = size; }

    public int getSize() { return size; }

    public static DATA_TYPE fromString(String value) {
        switch (value.toLowerCase()) {
            case "byte": return BYTE;
            case "u-byte": return U_BYTE;
            case "enumerated": return ENUMERATED;
            case "boolean": return BOOLEAN;
            case "integer2": return INTEGER2;
            case "u-integer2": return U_INTEGER2;
            case "integer4": return INTEGER4;
            case "u-integer4": return U_INTEGER4;
            case "integer8": return INTEGER8;
            default:
                throw new IllegalArgumentException("Unknown data type: " + value);
        }
    }
}
