package com.bc.fiduceo.reader.insitu.generic;

import org.esa.snap.core.datamodel.ProductData;

public class GenericVariable {

    private String name;
    private char origin = 'v';   // 'v' = variable (default), 's' = station
    private String type;
    private int productData;
    private Double fillValue;
    private String units;
    private String longName;
    private String cfStandard;
    private String ancillaryVariables;

    public GenericVariable() {
    }

    public GenericVariable(String name, char origin, String type, int productData, Double fillValue, String units, String longName, String cfStandard, String ancillaryVariables) {
        this.name = name;
        this.origin = origin;
        this.type = type;
        this.productData = productData;
        this.fillValue = fillValue;
        this.units = units;
        this.longName = longName;
        this.cfStandard = cfStandard;
        this.ancillaryVariables = ancillaryVariables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getOrigin() {
        return origin;
    }

    public void setOrigin(char origin) {
        this.origin = origin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        setProductData(parseProductData(type));
        this.type = type;
    }

    public int getProductData() {
        return productData;
    }

    public void setProductData(int productData) {
        this.productData = productData;
    }

    public Double getFillValue() {
        return fillValue;
    }

    public void setFillValue(Double fillValue) {
        this.fillValue = fillValue;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getCfStandard() {
        return cfStandard;
    }

    public void setCfStandard(String cfStandard) {
        this.cfStandard = cfStandard;
    }

    public String getAncillaryVariables() {
        return ancillaryVariables;
    }

    public void setAncillaryVariables(String ancillaryVariables) {
        this.ancillaryVariables = ancillaryVariables;
    }

    private static int parseProductData(String type) {
        switch (type.toLowerCase()) {
            case "byte":
                return ProductData.TYPE_INT8;
            case "short":
                return ProductData.TYPE_INT16;
            case "int":
                return ProductData.TYPE_INT32;
            case "long":
                return ProductData.TYPE_INT64;
            case "float":
                return ProductData.TYPE_FLOAT32;
            case "double":
                return ProductData.TYPE_FLOAT64;
            case "string":
                return ProductData.TYPE_ASCII;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
