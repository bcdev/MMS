package com.bc.fiduceo.post.plugin.era5;

import org.esa.snap.core.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class SatelliteFieldsConfiguration extends FieldsConfiguration {

    private static final String SENSOR_REF = "{sensor-ref}";
    private final HashMap<String, String> variableNames;

    private int x_dim;
    private int y_dim;
    private int z_dim;
    private String x_dim_name;
    private String y_dim_name;
    private String z_dim_name;

    private String nwp_time_variable_name;
    private String longitude_variable_name;
    private String latitude_variable_name;
    private String time_variable_name;

    private String sensorRef;
    private Map<String, TemplateVariable> generalizedVariables;

    SatelliteFieldsConfiguration() {
        variableNames = new HashMap<>();
        variableNames.put("an_ml_q", "nwp_q");
        variableNames.put("an_ml_t", "nwp_t");
        variableNames.put("an_ml_o3", "nwp_o3");
        variableNames.put("an_ml_lnsp", "nwp_lnsp");
        variableNames.put("an_sfc_siconc", "nwp_siconc");
        variableNames.put("an_sfc_t2m", "nwp_t2m");
        variableNames.put("an_sfc_u10", "nwp_u10");
        variableNames.put("an_sfc_v10", "nwp_v10");
        variableNames.put("an_sfc_msl", "nwp_msl");
        variableNames.put("an_sfc_skt", "nwp_skt");
        variableNames.put("an_sfc_sst", "nwp_sst");
        variableNames.put("an_sfc_tcc", "nwp_tcc");
        variableNames.put("an_sfc_tcwv", "nwp_tcwv");

        sensorRef = null;

        x_dim = -1;
        y_dim = -1;
        z_dim = -1;
    }

    Set<String> getVarNameKeys() {
        return variableNames.keySet();
    }

    String getVarName(String key) {
        return expand(variableNames.get(key));
    }

    void setVarName(String key, String name) {
        variableNames.put(key, name);
    }

    int get_x_dim() {
        return x_dim;
    }

    void set_x_dim(int x_dim) {
        this.x_dim = x_dim;
    }

    int get_y_dim() {
        return y_dim;
    }

    void set_y_dim(int y_dim) {
        this.y_dim = y_dim;
    }

    int get_z_dim() {
        return z_dim;
    }

    void set_z_dim(int z_dim) {
        this.z_dim = z_dim;
    }

    String get_x_dim_name() {
        return x_dim_name;
    }

    void set_x_dim_name(String x_dim_name) {
        this.x_dim_name = x_dim_name;
    }

    String get_y_dim_name() {
        return y_dim_name;
    }

    void set_y_dim_name(String y_dim_name) {
        this.y_dim_name = y_dim_name;
    }

    String get_z_dim_name() {
        return z_dim_name;
    }

    void set_z_dim_name(String z_dim_name) {
        this.z_dim_name = z_dim_name;
    }

    String get_nwp_time_variable_name() {
        return expand(nwp_time_variable_name);
    }

    void set_nwp_time_variable_name(String nwp_time_variable_name) {
        this.nwp_time_variable_name = nwp_time_variable_name;
    }

    String get_time_variable_name() {
        return expand(time_variable_name);
    }

    void set_time_variable_name(String time_variable_name) {
        this.time_variable_name = time_variable_name;
    }

    String get_longitude_variable_name() {
        return expand(longitude_variable_name);
    }

    void set_longitude_variable_name(String longitude_variable_name) {
        this.longitude_variable_name = longitude_variable_name;
    }

    String get_latitude_variable_name() {
        return expand(latitude_variable_name);
    }

    void set_latitude_variable_name(String latitude_variable_name) {
        this.latitude_variable_name = latitude_variable_name;
    }

    public String getSensorRef() {
        return sensorRef;
    }

    public void setSensorRef(String sensorRef) {
        this.sensorRef = sensorRef;
    }

    void verify() {
        if (x_dim < 1 || y_dim < 1) {
            // do not check z-dimension, this might be not configured tb 2020-11-16
            throw new IllegalArgumentException("dimensions incorrect: x:" + x_dim + " y:" + y_dim);
        }

        if (StringUtils.isNullOrEmpty(x_dim_name)) {
            throw new IllegalArgumentException("x dimension name not configured");
        }

        if (StringUtils.isNullOrEmpty(y_dim_name)) {
            throw new IllegalArgumentException("y dimension name not configured");
        }

        if (z_dim_name != null) {
            if (StringUtils.isNullOrEmpty(z_dim_name)) {
                throw new IllegalArgumentException("z dimension name not configured");
            }
            if (z_dim < 1) {
                throw new IllegalArgumentException("dimension incorrect: z:" + z_dim);
            }
        }

        if (StringUtils.isNullOrEmpty(nwp_time_variable_name)) {
            throw new IllegalArgumentException("era-5 time variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(time_variable_name)) {
            throw new IllegalArgumentException("satellite time variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(longitude_variable_name)) {
            throw new IllegalArgumentException("satellite lon variable name not configured");
        }

        if (StringUtils.isNullOrEmpty(latitude_variable_name)) {
            throw new IllegalArgumentException("satellite lat variable name not configured");
        }
    }

    private String expand(String variableName) {
        return expand(variableName, SENSOR_REF, getSensorRef());
    }

    public void setGeneralizedVariables(Map<String, TemplateVariable> generalizedVariables) {
        this.generalizedVariables = generalizedVariables;
        for (String key : generalizedVariables.keySet()) {
            setVarName(key, "nwp_" + generalizedVariables.get(key).getName());
        }
    }

    public Map<String, TemplateVariable> getGeneralizedVariables() {
        return generalizedVariables;
    }
}
