package com.bc.fiduceo.reader.amsu_mhs.nat;

public enum INSTRUMENT_GROUP {
    GENERIC,
    AMSUA,
    ASCAT,
    ATOVS,
    AVHRR3,
    GOME,
    GRAS,
    HIRS4,
    IASI,
    MHS,
    SEM,
    ADCS,
    SBUV,
    DUMMY,
    IASI_L2,
    ARCHIVE;

    public static INSTRUMENT_GROUP fromByte(byte b) {
        switch (b) {
            case 0:
                return GENERIC;
            case 1:
                return AMSUA;
            case 2:
                return ASCAT;
            case 3:
                return ATOVS;
            case 4:
                return AVHRR3;
            case 5:
                return GOME;
            case 6:
                return GRAS;
            case 7:
                return HIRS4;
            case 8:
                return IASI;
            case 9:
                return MHS;
            case 10:
                return SEM;
            case 11:
                return ADCS;
            case 12:
                return SBUV;
            case 13:
                return DUMMY;
            case 15:
                return IASI_L2;
            case 99:
                return ARCHIVE;
            default:
                throw new IllegalArgumentException("Unknown instrument group: " + b);
        }
    }
}
