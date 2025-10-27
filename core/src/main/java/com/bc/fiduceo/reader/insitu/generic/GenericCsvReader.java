package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.netcdf.StringVariable;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generic CSV/ASCII reader driven by an json config file.
 */
public class GenericCsvReader implements Reader {

    private String resourceKey;

    private CsvFormatConfig config;
    private List<GenericVariable> variables;
    private List<GenericRecord> records;
    private GenericRecord stationDatabaseRecord;

    public GenericCsvReader(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public void open(File file) throws IOException {
        config = CsvFormatConfig.loadConfig(resourceKey);
        records = GenericCsvHelper.parseData(file, config, resourceKey);
        variables = config.getAllVariables();

        StationDatabase stationDatabase = config.getStationDatabase();
        if (stationDatabase != null) {
            String primaryId = GenericCsvHelper.getPrimaryIdFromFilename(file, resourceKey);
            String secondaryId = GenericCsvHelper.getSecondaryIdFromFilename(file, resourceKey);
            stationDatabaseRecord = stationDatabase.extractRecord(primaryId, secondaryId);
        }
    }

    @Override
    public void close() throws IOException {
        if (records != null) {
            for (GenericRecord record : records) {
                record.getValues().clear();
            }
            records.clear();
            records = null;
        }
        if (variables != null) {
            variables.clear();
            variables = null;
        }
        if (stationDatabaseRecord != null) {
            stationDatabaseRecord.getValues().clear();
            stationDatabaseRecord = null;
        }
        if (config != null) {
            config.getVariables().clear();
            final StationDatabase stationDatabase = config.getStationDatabase();
            if (stationDatabase != null) {
                stationDatabase.getStations().clear();
                stationDatabase.getVariables().clear();
            }
            config = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;
        String timeName = config.getTimeName();
        for (final GenericRecord record : records) {
            int time = (int) record.getValues().get(timeName);
            if (time < minTime) {
                minTime = time;
            }
            if (time > maxTime) {
                maxTime = time;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime * 1000L));
        acquisitionInfo.setSensingStop(new Date(maxTime * 1000L));

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        if (config == null) {
            config = CsvFormatConfig.loadConfig(resourceKey);
        }
        return config.getRegex();
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        long[] timeArray = new long[records.size()];

        int ii = 0;
        for (final GenericRecord record : records) {
            String timeName = config.getTimeName();
            int timeInSeconds = (int) record.getValues().get(timeName);
            timeArray[ii] = timeInSeconds * 1000L;
            ii++;
        }

        return new TimeLocator_MillisSince1970(timeArray);
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        return GenericCsvHelper.extractYearMonthDayFromFilename(fileName, resourceKey);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        GenericVariable variable = findVariable(variableName);
        if (variable != null) {
            Object o;
            if (variable.getOrigin() == 's') {
                o = stationDatabaseRecord.get(variableName);
            } else {
                GenericRecord record = records.get(centerY);
                o = record.get(variableName);
            }
            return createResultArray(o, variable.getFillValue(), variable.getType(), interval);
        }

        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        String timeName = config.getTimeName();
        final Array timeArray = readRaw(x, y, interval, timeName);

        return (ArrayInt.D2) timeArray;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();
        List<Attribute> attributes;

        for (GenericVariable var : this.variables) {
            attributes = new ArrayList<>();
            setNcAttributes(attributes, var);

            DataType ncDataType = GenericCsvHelper.getNcDataType(var.getType());
            VariableProxy varProxy = new VariableProxy(var.getName(), ncDataType, attributes);

            if (ncDataType == DataType.STRING) {
                StringVariable stringVar = new StringVariable(varProxy, 50);
                variables.add(stringVar);
            } else {
                variables.add(varProxy);
            }
        }

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    @Override
    public String getLongitudeVariableName() {
        return config.getLongitudeName();
    }

    @Override
    public String getLatitudeVariableName() {
        return config.getLatitudeName();
    }

    private GenericVariable findVariable(String variableName) {
        for (GenericVariable variable : variables) {
            if (variable.getName().equals(variableName)) {
                return variable;
            }
        }
        return null;
    }

    protected Array createResultArray(Object value, Number fillValue, String dataType, Interval interval) {
        if (value instanceof String) {
            final Array resultArray = Array.factory(DataType.STRING, new int[]{1, 1});
            resultArray.setObject(0, value);
            return resultArray;
        }

        if (fillValue == null) {
            fillValue = NetCDFUtils.getDefaultFillValue(GenericCsvHelper.getFillValueClass(dataType));
        }

        DataType ncDataType = GenericCsvHelper.getNcDataType(dataType);

        final int windowHeight = interval.getY();
        final int windowWidth = interval.getX();
        final Array windowArray = NetCDFUtils.create(ncDataType,
                new int[]{windowHeight, windowWidth},
                fillValue);

        final int windowCenterX = windowWidth / 2;
        final int windowCenterY = windowHeight / 2;

        Object safeValue = value != null ? value : fillValue;
        windowArray.setObject(windowWidth * windowCenterY + windowCenterX, safeValue);
        return windowArray;
    }

    private void setNcAttributes(List<Attribute> attributes, GenericVariable var) {
        if (var.getUnits() != null) {
            attributes.add(new Attribute(NetCDFUtils.CF_UNITS_NAME, var.getUnits()));
        }
        if (var.getFillValue() != null) {
            Number typedFillValue = GenericCsvHelper.castFillValue(var.getFillValue(), var.getType());
            attributes.add(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, typedFillValue));
        } else {
            if (!var.getType().equals("string")) {
                Number fillValue = NetCDFUtils.getDefaultFillValue(GenericCsvHelper.getFillValueClass(var.getType()));
                attributes.add(new Attribute(NetCDFUtils.CF_FILL_VALUE_NAME, fillValue));
            }
        }
        if (var.getCfStandard() != null) {
            attributes.add(new Attribute(NetCDFUtils.CF_STANDARD_NAME, var.getCfStandard()));
        }
        if (var.getLongName() != null) {
            attributes.add(new Attribute(NetCDFUtils.CF_LONG_NAME, var.getLongName()));
        }
        if (var.getAncillaryVariables() != null) {
            attributes.add(new Attribute(NetCDFUtils.CF_ANCILLARY_VARIABLES_NAME, var.getAncillaryVariables()));
        }
    }
}
