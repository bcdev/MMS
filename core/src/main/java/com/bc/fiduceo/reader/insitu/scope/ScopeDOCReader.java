package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class ScopeDOCReader extends ScopeReader {

    private static final String REG_EX_DOC = "SCOPE_WP24.*\\.txt";

    private static final String DOC = "DOC";

    private ArrayList<DocRecord> records;
    private TimeLocator timeLocator;

    @Override
    public void open(File file) throws IOException {
        parseFile(file);
    }

    @Override
    public void close() throws IOException {
        if (records != null) {
            records.clear();
            records = null;
        }
        timeLocator = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;
        for (final DocRecord record : records) {
            if (record.utc < minTime) {
                minTime = record.utc;
            }
            if (record.utc > maxTime) {
                maxTime = record.utc;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime * 1000L));
        acquisitionInfo.setSensingStop(new Date(maxTime * 1000L));
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_DOC;
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
        if (timeLocator == null) {
            createTimeLocator();
        }
        return timeLocator;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final DocRecord record = records.get(centerY);

        switch (variableName) {
            case LONGITUDE:
                return createResultArray(record.longitude, Float.NaN, DataType.FLOAT, interval);
            case LATITUDE:
                return createResultArray(record.latitude, Float.NaN, DataType.FLOAT, interval);
            case TIME:
                return createResultArray(record.utc, NetCDFUtils.getDefaultFillValue(int.class), DataType.INT, interval);
            case DOC:
                return createResultArray(record.doc, Float.NaN, DataType.FLOAT, interval);
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
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();

        List<Attribute> attributes = new ArrayList<>();
        createBasicScopeVariables(variables, attributes);

        createMeasurementTimeVariable(variables);

        // DOC variable
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "umol/l"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Dissolved Organic Carbon concentration"));
        variables.add(new VariableProxy(DOC, DataType.FLOAT, attributes));

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return new Dimension("product_size", 1, records.size());
    }

    private void parseFile(File file) throws IOException {
        records = new ArrayList<>();

        try (final FileReader fileReader = new FileReader(file)) {
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    // Skip comment lines
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                final DocRecord docRecord = parseLine(line);
                if (docRecord != null) {
                    records.add(docRecord);
                }
            }
        }
    }

    DocRecord parseLine(String line) {
        try {
            line = line.replaceAll(" +", " ").trim(); // Normalize whitespace
            final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

            if (tokens.length < 4) {
                return null; // Invalid line for DOC format
            }

            final DocRecord record = new DocRecord();
            record.utc = Integer.parseInt(tokens[0]);
            record.longitude = Float.parseFloat(tokens[1]);
            record.latitude = Float.parseFloat(tokens[2]);
            record.doc = Float.parseFloat(tokens[3]);

            return record;
        } catch (Exception e) {
            // Skip invalid lines
            return null;
        }
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final DocRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }
}