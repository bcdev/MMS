package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.NetCDFUtils;

class TemplateVariable {

    private static final float FILL_VALUE = NetCDFUtils.getDefaultFillValue(float.class).floatValue();

    private String name;
    private final String units;
    private final String longName;
    private final String standardName;
    private final boolean is3d;

    private float fill_value;

    TemplateVariable(String name, String units, String longName, String standardName, boolean is3d) {
        this.name = name;
        this.units = units;
        this.longName = longName;
        this.standardName = standardName;
        this.is3d = is3d;
        this.fill_value = FILL_VALUE;
    }

    public void setFill_value(float fill_value) {
        this.fill_value = fill_value;
    }

    float getFillValue() {
        return fill_value;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    String getLongName() {
        return longName;
    }

    String getUnits() {
        return units;
    }

    String getStandardName() {
        return standardName;
    }

    boolean is3d() {
        return is3d;
    }
}
