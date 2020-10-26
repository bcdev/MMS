package com.bc.fiduceo.reader.slstr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.bc.fiduceo.reader.slstr.VariableType.Type.*;

class VariableFactory {

    private final HashMap<String, String> nadir1kmNames;
    private final HashMap<String, String> nadir500mNames;
    private final HashMap<String, String> oblique1kmNames;
    private final HashMap<String, String> oblique500mNames;
    private final List<String> flagNames;
    private final List<String> tiePointNames;

    VariableFactory() {
        nadir500mNames = new HashMap<>();
        nadir500mNames.put("latitude_tx", "SLSTR_GEODETIC_TX_Data");
        nadir500mNames.put("longitude_tx", "SLSTR_GEODETIC_TX_Data");
        nadir500mNames.put("sat_azimuth_tn", "SLSTR_GEOMETRY_TN_Data");
        nadir500mNames.put("sat_zenith_tn", "SLSTR_GEOMETRY_TN_Data");
        nadir500mNames.put("solar_azimuth_tn", "SLSTR_GEOMETRY_TN_Data");
        nadir500mNames.put("solar_zenith_tn", "SLSTR_GEOMETRY_TN_Data");
        nadir500mNames.put("sat_azimuth_to", "SLSTR_GEOMETRY_TO_Data");
        nadir500mNames.put("sat_zenith_to", "SLSTR_GEOMETRY_TO_Data");
        nadir500mNames.put("solar_azimuth_to", "SLSTR_GEOMETRY_TO_Data");
        nadir500mNames.put("solar_zenith_to", "SLSTR_GEOMETRY_TO_Data");
        nadir500mNames.put("S1_radiance_an", "SLSTR_S1_RAD_AN_Data");
        nadir500mNames.put("S2_radiance_an", "SLSTR_S2_RAD_AN_Data");
        nadir500mNames.put("S3_radiance_an", "SLSTR_S3_RAD_AN_Data");
        nadir500mNames.put("S4_radiance_an", "SLSTR_S4_RAD_AN_Data");
        nadir500mNames.put("S5_radiance_an", "SLSTR_S5_RAD_AN_Data");
        nadir500mNames.put("S6_radiance_an", "SLSTR_S6_RAD_AN_Data");
        nadir500mNames.put("S1_exception_an", "SLSTR_S1_RAD_AN_Data");
        nadir500mNames.put("S2_exception_an", "SLSTR_S2_RAD_AN_Data");
        nadir500mNames.put("S3_exception_an", "SLSTR_S3_RAD_AN_Data");
        nadir500mNames.put("S4_exception_an", "SLSTR_S4_RAD_AN_Data");
        nadir500mNames.put("S5_exception_an", "SLSTR_S5_RAD_AN_Data");
        nadir500mNames.put("S6_exception_an", "SLSTR_S6_RAD_AN_Data");

        nadir1kmNames = new HashMap<>();
        nadir1kmNames.put("S7_BT_in", "SLSTR_S7_BT_IN_Data");
        nadir1kmNames.put("S8_BT_in", "SLSTR_S8_BT_IN_Data");
        nadir1kmNames.put("S9_BT_in", "SLSTR_S9_BT_IN_Data");
        nadir1kmNames.put("S7_exception_in", "SLSTR_S7_BT_IN_Data");
        nadir1kmNames.put("S8_exception_in", "SLSTR_S8_BT_IN_Data");
        nadir1kmNames.put("S9_exception_in", "SLSTR_S9_BT_IN_Data");
        nadir1kmNames.put("confidence_in", "SLSTR_FLAGS_IN_Data");
        nadir1kmNames.put("pointing_in", "SLSTR_FLAGS_IN_Data");
        nadir1kmNames.put("bayes_in", "SLSTR_FLAGS_IN_Data");
        nadir1kmNames.put("cloud_in", "SLSTR_FLAGS_IN_Data");

        oblique500mNames = new HashMap<>();
        oblique500mNames.put("S1_radiance_ao", "SLSTR_S1_RAD_AO_Data");
        oblique500mNames.put("S2_radiance_ao", "SLSTR_S2_RAD_AO_Data");
        oblique500mNames.put("S3_radiance_ao", "SLSTR_S3_RAD_AO_Data");
        oblique500mNames.put("S4_radiance_ao", "SLSTR_S4_RAD_AO_Data");
        oblique500mNames.put("S5_radiance_ao", "SLSTR_S5_RAD_AO_Data");
        oblique500mNames.put("S6_radiance_ao", "SLSTR_S6_RAD_AO_Data");
        oblique500mNames.put("S1_exception_ao", "SLSTR_S1_RAD_AO_Data");
        oblique500mNames.put("S2_exception_ao", "SLSTR_S2_RAD_AO_Data");
        oblique500mNames.put("S3_exception_ao", "SLSTR_S3_RAD_AO_Data");
        oblique500mNames.put("S4_exception_ao", "SLSTR_S4_RAD_AO_Data");
        oblique500mNames.put("S5_exception_ao", "SLSTR_S5_RAD_AO_Data");
        oblique500mNames.put("S6_exception_ao", "SLSTR_S6_RAD_AO_Data");

        oblique1kmNames = new HashMap<>();
        oblique1kmNames.put("S7_BT_io", "SLSTR_S7_BT_IO_Data");
        oblique1kmNames.put("S8_BT_io", "SLSTR_S8_BT_IO_Data");
        oblique1kmNames.put("S9_BT_io", "SLSTR_S9_BT_IO_Data");
        oblique1kmNames.put("S7_exception_io", "SLSTR_S7_BT_IO_Data");
        oblique1kmNames.put("S8_exception_io", "SLSTR_S7_BT_IO_Data");
        oblique1kmNames.put("S9_exception_io", "SLSTR_S7_BT_IO_Data");
        oblique1kmNames.put("bayes_io", "SLSTR_FLAGS_IO_Data");
        oblique1kmNames.put("cloud_io", "SLSTR_FLAGS_IO_Data");

        flagNames = new ArrayList<>();
        flagNames.add("S1_exception_an");
        flagNames.add("S2_exception_an");
        flagNames.add("S3_exception_an");
        flagNames.add("S4_exception_an");
        flagNames.add("S5_exception_an");
        flagNames.add("S6_exception_an");
        flagNames.add("S7_exception_in");
        flagNames.add("S8_exception_in");
        flagNames.add("S9_exception_in");
        flagNames.add("confidence_in");
        flagNames.add("pointing_in");
        flagNames.add("bayes_in");
        flagNames.add("cloud_in");
        flagNames.add("S1_exception_ao");
        flagNames.add("S2_exception_ao");
        flagNames.add("S3_exception_ao");
        flagNames.add("S4_exception_ao");
        flagNames.add("S5_exception_ao");
        flagNames.add("S6_exception_ao");
        flagNames.add("S7_exception_io");
        flagNames.add("S8_exception_io");
        flagNames.add("S9_exception_io");
        flagNames.add("bayes_io");
        flagNames.add("cloud_io");

        tiePointNames = new ArrayList<>();
        tiePointNames.add("latitude_tx");
        tiePointNames.add("longitude_tx");
        tiePointNames.add("sat_azimuth_tn");
        tiePointNames.add("sat_zenith_tn");
        tiePointNames.add("solar_azimuth_tn");
        tiePointNames.add("solar_zenith_tn");
        tiePointNames.add("sat_azimuth_to");
        tiePointNames.add("sat_zenith_to");
        tiePointNames.add("solar_azimuth_to");
        tiePointNames.add("solar_zenith_to");
    }

    boolean isValidName(String variableName) {
        return nadir500mNames.keySet().contains(variableName) ||
                nadir1kmNames.keySet().contains(variableName) ||
                oblique500mNames.keySet().contains(variableName) ||
                oblique1kmNames.keySet().contains(variableName);
    }

    VariableType getVariableType(String variableName) {
        final VariableType variableType;
        if (nadir500mNames.keySet().contains(variableName)) {
             variableType  = new VariableType(NADIR_500m);
        } else if (nadir1kmNames.keySet().contains(variableName)) {
            return new VariableType(NADIR_1km);
        } else if (oblique500mNames.keySet().contains(variableName)) {
            return new VariableType(OBLIQUE_500m);
        } else if (oblique1kmNames.keySet().contains(variableName)) {
            return new VariableType(OBLIQUE_1km);
        } else {
            throw new RuntimeException("Requested variable not supported: " + variableName);
        }

        variableType.setTiePoint(isTiePointVariable(variableName));
        return variableType;
    }

    boolean isFlagVariable(String variableName) {
        return flagNames.contains(variableName);
    }

    boolean isTiePointVariable(String variableName) {
        return tiePointNames.contains(variableName);
    }

    // @todo 1 tb/tb add tests 2020-10-23
    String getManifestId(String variableName) {
        String manifestId = nadir1kmNames.get(variableName);
        if (manifestId != null) {
            return manifestId;
        }

        manifestId = nadir500mNames.get(variableName);
        if (manifestId != null) {
            return manifestId;
        }

        manifestId = oblique1kmNames.get(variableName);
        if (manifestId != null) {
            return manifestId;
        }

        manifestId = oblique500mNames.get(variableName);
        if (manifestId != null) {
            return manifestId;
        }

        throw new IllegalArgumentException("Unsupported variable: " + variableName);
    }
}
