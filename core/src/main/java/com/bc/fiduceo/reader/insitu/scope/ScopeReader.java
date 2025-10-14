package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

abstract class ScopeReader implements Reader {

    static final String LONGITUDE = "longitude";
    static final String LATITUDE = "latitude";
    static final String TIME = "time";

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        int[] ymd = new int[3];
        String[] parts = fileName.split("_");
        String yearPart = parts[parts.length - 2];
        ymd[0] = Integer.parseInt(yearPart);
        ymd[1] = 1;
        ymd[2] = 1;
        return ymd;
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE;
    }

    static void createMeasurementTimeVariable(ArrayList<Variable> variables) {
        List<Attribute> attributes;
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "seconds since 1970-01-01"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(int.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "time"));
        variables.add(new VariableProxy(TIME, DataType.INT, attributes));
    }

    static void createBasicScopeVariables(ArrayList<Variable> variables, List<Attribute> attributes) {
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_east"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "longitude"));
        variables.add(new VariableProxy(LONGITUDE, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "degree_north"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, NetCDFUtils.getDefaultFillValue(float.class)));
        attributes.add(new Attribute(CF_STANDARD_NAME, "latitude"));
        variables.add(new VariableProxy(LATITUDE, DataType.FLOAT, attributes));
    }

    protected Array createResultArray(Number value, Number fillValue, DataType dataType, Interval interval) {
        final int windowHeight = interval.getY();
        final int windowWidth = interval.getX();
        final Array windowArray = NetCDFUtils.create(dataType,
                new int[]{windowHeight, windowWidth},
                fillValue);

        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, value);
        return windowArray;
    }
}