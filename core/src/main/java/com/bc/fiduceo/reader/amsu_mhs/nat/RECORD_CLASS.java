package com.bc.fiduceo.reader.amsu_mhs.nat;

public enum RECORD_CLASS {
    RESERVED,
    MPHR,   // Main Product Header Record
    SPHR,   // Secondary Product Header Record
    IPR,    // Internal Pointer Record
    GEADR,  // Global External Auxiliary Data Record
    GIADR,  // Global Internal Auxiliary Data Record
    VEADR,  // Variable External Auxiliary Data Record
    VIADR,  // Variable Internal Auxiliary Data Record
    MDR;    // Measurement Data Record

    public static RECORD_CLASS fromByte(byte b) {
        switch (b) {
            case 0:
                return RESERVED;
            case 1:
                return MPHR;
            case 2:
                return SPHR;
            case 3:
                return IPR;
            case 4:
                return GEADR;
            case 5:
                return GIADR;
            case 6:
                return VEADR;
            case 7:
                return VIADR;
            case 8:
                return MDR;
            default:
                throw new IllegalArgumentException("Unknown record class: " + b);
        }
    }
}
