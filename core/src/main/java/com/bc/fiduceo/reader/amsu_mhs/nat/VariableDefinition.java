package com.bc.fiduceo.reader.amsu_mhs.nat;

public class VariableDefinition {

    private String data_type;
    private int offset;
    private int stride;
    private double scale_factor;
    private String units;
    private String flag_values;
    private String flag_meanings;

    public VariableDefinition() {
        scale_factor = 1.0;
        units = "";
    }

    public int getProductData_type() {
        return EpsReaderUtils.mapToProductData(data_type);
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public double getScale_factor() {
        return scale_factor;
    }

    public void setScale_factor(double scale_factor) {
        this.scale_factor = scale_factor;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getFlag_values() {
        return flag_values;
    }

    public void setFlag_values(String flag_values) {
        this.flag_values = flag_values;
    }

    public String getFlag_meanings() {
        return flag_meanings;
    }

    public void setFlag_meanings(String flag_meanings) {
        this.flag_meanings = flag_meanings;
    }
}
