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

class ScopePhytoReader extends ScopeReader {

    private static final String REG_EX_PHYTO = "SCOPE_WP25.*\\.txt";

    private static final String PHYTOPLANKTON_CARBON = "phytoplankton_carbon";
    private static final String DEPTH_M = "depth_m";

    private ArrayList<PhytoRecord> records;
    private TimeLocator timeLocator;
    private GeometryFactory geometryFactory;

    public ScopePhytoReader(GeometryFactory geometryFactory) {
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

        for (final PhytoRecord record : records) {
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
        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_PHYTO;
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
        final PhytoRecord record = records.get(centerY);

        switch (variableName) {
            case LONGITUDE:
                return createResultArray(record.longitude, Float.NaN, DataType.FLOAT, interval);
            case LATITUDE:
                return createResultArray(record.latitude, Float.NaN, DataType.FLOAT, interval);
            case TIME:
                return createResultArray(record.utc, NetCDFUtils.getDefaultFillValue(int.class), DataType.INT, interval);
            case PHYTOPLANKTON_CARBON:
                return createResultArray(record.phytoplanktonCarbon, Float.NaN, DataType.FLOAT, interval);
            case DEPTH_M:
                return createResultArray(record.depthM, Float.NaN, DataType.FLOAT, interval);
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

        // Phytoplankton carbon variable
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "mg/m^3"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_LONG_NAME, "Phytoplankton carbon concentration"));
        variables.add(new VariableProxy(PHYTOPLANKTON_CARBON, DataType.FLOAT, attributes));

        // Depth variable
        attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_UNITS_NAME, "m"));
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, Float.NaN));
        attributes.add(new Attribute(CF_STANDARD_NAME, "depth"));
        attributes.add(new Attribute(CF_LONG_NAME, "Depth below sea surface"));
        variables.add(new VariableProxy(DEPTH_M, DataType.FLOAT, attributes));

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

                final PhytoRecord phytoRecord = parseLine(line);
                if (phytoRecord != null) {
                    records.add(phytoRecord);
                }
            }
        }
    }

    PhytoRecord parseLine(String line) {
        try {
            line = line.replaceAll(" +", " ").trim(); // Normalize whitespace
            final String[] tokens = StringUtils.split(line, new char[]{' '}, true);

            if (tokens.length < 5) {
                return null; // Invalid line for Phyto format
            }

            final PhytoRecord record = new PhytoRecord();
            record.utc = Integer.parseInt(tokens[0]);
            record.longitude = Float.parseFloat(tokens[1]);
            record.latitude = Float.parseFloat(tokens[2]);
            record.phytoplanktonCarbon = Float.parseFloat(tokens[3]);
            record.depthM = Float.parseFloat(tokens[4]);

            return record;
        } catch (Exception e) {
            // Skip invalid lines
            return null;
        }
    }

    private void createTimeLocator() {
        long[] timeArray = new long[records.size()];

        int i = 0;
        for (final PhytoRecord record : records) {
            timeArray[i] = record.utc * 1000L;
            i++;
        }

        timeLocator = new TimeLocator_MillisSince1970(timeArray);
    }
}