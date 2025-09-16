package com.bc.fiduceo.reader.amsu_mhs.nat;

public class EPS_Constants {

    public static final int GENERIC_RECORD_HEADER_SIZE = 20;

    public static final int MHS_FOV_COUNT = 90;
    public static final int MHS_L1B_EARTH_LOCATIONS_OFFSET = 3318;
    public static final int MHS_EARTH_LOCATIONS_TOTAL_BYTE_SIZE = 720;
    public static final int MHS_EARTH_LOCATIONS_SCALE_FACTOR = 10000;

    public static final int AMSUA_FOV_COUNT = 30;

    public static String SENSING_START_KEY = "SENSING_START";
    public static String SENSING_STOP_KEY = "SENSING_END";

    public static String LON_VAR_NAME = "longitude";
    public static String LAT_VAR_NAME = "latitude";
}
