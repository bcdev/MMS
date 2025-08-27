package com.bc.fiduceo.reader.amsu_mhs.nat;

public class VariableDefinition {

    private String name;
    private String data_type;
    private int productDataType;
    private int offset;
    private int stride;
    private int scale_factor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getData_type() {
        return productDataType;
    }

    public void setData_type(String data_type) {
        this.productDataType = EpsReaderUtils.mapToProductData(data_type);
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

    public int getScale_factor() {
        return scale_factor;
    }

    public void setScale_factor(int scale_factor) {
        this.scale_factor = scale_factor;
    }
}
