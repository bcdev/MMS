package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.Attribute;
import org.esa.snap.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class ScopeTaReader extends ScopeReader {

    private static final String REG_EX_TA = "SCOPE_WP21.*TA.*\\.txt";

    private static final String IN_SITU_TA = "in_situ_TA";
    private static final String IN_SITU_TA_STD = "in_situ_TA_std";
    private static final String U_EXP_FNN_U_TA = "UExP_FNN_U_TA";
    private static final String U_EXP_FNN_U_UNCERTAINTY = "UExP_FNN_U_uncertainty";
    private static final String REGION = "region";

    private ArrayList<TaRecord> records;
    private TimeLocator timeLocator;

    ScopeTaReader(GeometryFactory geometryFactory) {
    }

    @Override
    public void open(File file) throws IOException {
        parseFile(file);
        createTimeLocator();
    }

    @Override
    public void close() {
        records.clear();
        records = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        for (final TaRecord record : records) {
            final long time = record.utc * 1000L;
            if (time < minTime) {
                minTime = time;
            }
            if (time > maxTime) {
                maxTime = time;
            }

            if (record.longitude < minLon) {
                minLon = record.longitude;
            }
            if (record.longitude > maxLon) {
                maxLon = record.longitude;
            }

            if (record.latitude < minLat) {
                minLat = record.latitude;
            }
            if (record.latitude > maxLat) {
                maxLat = record.latitude;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime));
        acquisitionInfo.setSensingStop(new Date(maxTime));
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_TA;
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
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    @Override
    public List<Variable> getVariables() throws IOException {
        final ArrayList<Variable> variables = new ArrayList<>();
        List<Attribute> attributes = new ArrayList<>();

        createBasicScopeVariables(variables, attributes);
        createMeasurementTimeVariable(variables);

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "µmol/kg"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "In-situ total alkalinity measurement"));
        variables.add(new VariableProxy(IN_SITU_TA, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "µmol/kg"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Standard deviation of in-situ total alkalinity"));
        variables.add(new VariableProxy(IN_SITU_TA_STD, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "µmol/kg"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "UExP FNN uncertainty for total alkalinity"));
        variables.add(new VariableProxy(U_EXP_FNN_U_TA, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "µmol/kg"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "UExP FNN uncertainty value"));
        variables.add(new VariableProxy(U_EXP_FNN_U_UNCERTAINTY, DataType.FLOAT, attributes));

        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Region code"));
        variables.add(new VariableProxy(REGION, DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final TaRecord record = records.get(centerY);

        switch (variableName) {
            case LONGITUDE:
                return createResultArray(record.longitude, Float.NaN, DataType.FLOAT, interval);
            case LATITUDE:
                return createResultArray(record.latitude, Float.NaN, DataType.FLOAT, interval);
            case TIME:
                return createResultArray(record.utc, NetCDFUtils.getDefaultFillValue(int.class), DataType.INT, interval);
            case IN_SITU_TA:
                return createResultArray(record.inSituTa, Float.NaN, DataType.FLOAT, interval);
            case IN_SITU_TA_STD:
                return createResultArray(record.inSituTaStd, Float.NaN, DataType.FLOAT, interval);
            case U_EXP_FNN_U_TA:
                return createResultArray(record.uExpFnnUTa, Float.NaN, DataType.FLOAT, interval);
            case U_EXP_FNN_U_UNCERTAINTY:
                return createResultArray(record.uExpFnnUUncertainty, Float.NaN, DataType.FLOAT, interval);
            case REGION:
                return createResultArray(record.region, Float.NaN, DataType.FLOAT, interval);
        }

        return null;
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final Array timeArray = readRaw(x, y, interval, TIME);
        return (ArrayInt.D2) timeArray;
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            createTimeLocator();
        }
        return timeLocator;
    }

    private void parseFile(File file) throws IOException {
        records = new ArrayList<>();
        try (final FileReader fileReader = new FileReader(file)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                final TaRecord record = parseLine(line);
                if (record != null) {
                    records.add(record);
                }
            }
        }
    }

    TaRecord parseLine(String line) {
        try {
            line = line.replaceAll(" +", " ").trim();
            final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

            if (tokens.length < 8) {
                return null;
            }

            final TaRecord record = new TaRecord();
            record.utc = Integer.parseInt(tokens[0]);
            record.longitude = Float.parseFloat(tokens[1]);
            record.latitude = Float.parseFloat(tokens[2]);
            record.inSituTa = Float.parseFloat(tokens[3]);
            record.inSituTaStd = Float.parseFloat(tokens[4]);
            record.uExpFnnUTa = Float.parseFloat(tokens[5]);
            record.uExpFnnUUncertainty = Float.parseFloat(tokens[6]);
            record.region = Float.parseFloat(tokens[7]);

            return record;
        } catch (Exception e) {
            return null;
        }
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];
        int i = 0;
        for (final TaRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }
        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }
}