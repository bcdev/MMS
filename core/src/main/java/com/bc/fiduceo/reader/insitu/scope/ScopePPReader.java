package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MillisSince1970;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import com.bc.fiduceo.util.VariableProxy;
import com.bc.fiduceo.reader.netcdf.StringVariable;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.*;

class ScopePPReader extends ScopeReader {

    private static final String REG_EX_PP = "SCOPE_WP26_PP.*\\.txt";

    private static final String PP = "PP";
    private static final String DATABASE = "database";

    private ArrayList<PpRecord> records;
    private TimeLocator timeLocator;
    private GeometryFactory geometryFactory;

    public ScopePPReader(GeometryFactory geometryFactory) {
        super();
        this.geometryFactory = geometryFactory;
    }

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
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (final PpRecord record : records) {
            if (record.utc < minTime) {
                minTime = record.utc;
            }
            if (record.utc > maxTime) {
                maxTime = record.utc;
            }
            if (record.latitude < minLat) {
                minLat = record.latitude;
            }
            if (record.latitude > maxLat) {
                maxLat = record.latitude;
            }
            if (record.longitude < minLon) {
                minLon = record.longitude;
            }
            if (record.longitude > maxLon) {
                maxLon = record.longitude;
            }
        }

        acquisitionInfo.setSensingStart(new Date(minTime * 1000L));
        acquisitionInfo.setSensingStop(new Date(maxTime * 1000L));
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        // Set bounding geometry for the in-situ measurements
        final com.bc.fiduceo.geometry.Polygon polygon = geometryFactory.createPolygon(Arrays.asList(
                geometryFactory.createPoint(minLon, minLat),
                geometryFactory.createPoint(minLon, maxLat),
                geometryFactory.createPoint(maxLon, maxLat),
                geometryFactory.createPoint(maxLon, minLat),
                geometryFactory.createPoint(minLon, minLat)
        ));
        acquisitionInfo.setBoundingGeometry(polygon);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_PP;
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
        final PpRecord record = records.get(centerY);

        switch (variableName) {
            case LONGITUDE:
                return createResultArray(record.longitude, Float.NaN, DataType.FLOAT, interval);
            case LATITUDE:
                return createResultArray(record.latitude, Float.NaN, DataType.FLOAT, interval);
            case TIME:
                return createResultArray(record.utc, NetCDFUtils.getDefaultFillValue(int.class), DataType.INT, interval);
            case PP:
                return createResultArray(record.pp, Float.NaN, DataType.FLOAT, interval);
            case DATABASE:
                final Array resultArray = Array.factory(DataType.STRING, new int[]{interval.getY(), interval.getX()});
                for (int y = 0; y < interval.getY(); y++) {
                    for (int x = 0; x < interval.getX(); x++) {
                        if (y == interval.getY() / 2 && x == interval.getX() / 2) {
                            resultArray.setObject(y * interval.getX() + x, record.database);
                        } else {
                            resultArray.setObject(y * interval.getX() + x, "");
                        }
                    }
                }
                return resultArray;
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

        // PP variable
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "mgC/m^2/day"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Primary Production"));
        variables.add(new VariableProxy(PP, DataType.FLOAT, attributes));

        // Database variable
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_LONG_NAME, "Database source"));
        variables.add(new StringVariable(new VariableProxy(DATABASE, DataType.STRING, attributes), 50));

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

                final PpRecord ppRecord = parseLine(line);
                if (ppRecord != null) {
                    records.add(ppRecord);
                }
            }
        }
    }

    PpRecord parseLine(String line) {
        try {
            line = line.replaceAll(" +", " ").trim(); // Normalize whitespace
            final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

            if (tokens.length < 5) {
                return null; // Invalid line for PP format
            }

            final PpRecord record = new PpRecord();
            record.utc = Integer.parseInt(tokens[0]);

            // Skip data before 1970 (negative timestamps)
            if (record.utc < 0) {
                return null;
            }

            record.longitude = Float.parseFloat(tokens[1]);
            record.latitude = Float.parseFloat(tokens[2]);
            record.pp = Float.parseFloat(tokens[3]);
            record.database = tokens[4];

            return record;
        } catch (Exception e) {
            // Skip invalid lines
            return null;
        }
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final PpRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }
}