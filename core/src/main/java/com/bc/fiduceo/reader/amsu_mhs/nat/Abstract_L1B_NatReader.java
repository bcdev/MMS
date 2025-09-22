package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelGeoCodingPixelLocator;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_StartStopDate;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.bc.fiduceo.core.NodeType.UNDEFINED;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;
import static ucar.ma2.DataType.INT;

abstract public class Abstract_L1B_NatReader implements Reader {

    protected VariableRegistry registry;
    protected EpsVariableCache cache;
    protected final GeometryFactory geometryFactory;
    private Dimension productSize;
    private static final int NUM_SPLITS = 2;

    private PixelLocator pixelLocator;

    public Abstract_L1B_NatReader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
        pixelLocator = null;
        productSize = null;
    }

    public AcquisitionInfo read(Interval interval) throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(UNDEFINED);

        final MPHR recordMPHR = cache.getMPHR();
        setSensingDates(acquisitionInfo, recordMPHR);

        final Array lon = cache.getScaled(LON_VAR_NAME);
        final Array lat = cache.getScaled(LAT_VAR_NAME);

        final Geometries geometries = extractGeometries(lon, lat, NUM_SPLITS, interval);
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = cache.getRaw(variableName);
        final VariableDefinition variableDef = registry.getVariableDef(variableName);
        final Number fillValue = EpsReaderUtils.getFillValue(variableDef.getData_type());
        final Dimension productSize = getProductSize();
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawData, productSize);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = readRaw(centerX, centerY, interval, variableName);
        final VariableDefinition variableDef = registry.getVariableDef(variableName);
        return EpsReaderUtils.scale(rawData, variableDef.getScale_factor());
    }

    @Override
    public String getLongitudeVariableName() {
        return LON_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LAT_VAR_NAME;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array lon = cache.getScaled(LON_VAR_NAME);
            final Array lat = cache.getScaled(LAT_VAR_NAME);

            pixelLocator = new PixelGeoCodingPixelLocator(lon, lat, LON_VAR_NAME, LAT_VAR_NAME, 48.0, GeoChecks.POLES);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        // for the test file, the array returned contains only zeros - same as for MHS
        // According to the sparse documentation, I would expect this to contain seconds since epoch
        // tb 2025-09-04
        // Array timeAttitude = cache.getRaw("TIME_ATTITUDE");

        // Instead, we interpolate between header start and stop times tb 2025-09-04
        final MPHR mphr = cache.getMPHR();
        final Date sensingStart = mphr.getSensingStart();
        final Date sensingStop = mphr.getSensingStop();

        return new TimeLocator_StartStopDate(sensingStart, sensingStop, getProductSize().getNy());
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String[] strings = fileName.split("_");
        final String dateTimePart = strings[4];

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(dateTimePart.substring(0, 4));
        ymd[1] = Integer.parseInt(dateTimePart.substring(4, 6));
        ymd[2] = Integer.parseInt(dateTimePart.substring(6, 8));
        return ymd;
    }

    protected static void setSensingDates(AcquisitionInfo acquisitionInfo, MPHR recordMPHR) throws IOException {
        final Date sensingStart = recordMPHR.getSensingStart();
        acquisitionInfo.setSensingStart(sensingStart);
        final Date sensingEnd = recordMPHR.getSensingStop();
        acquisitionInfo.setSensingStop(sensingEnd);
    }

    protected void initializeRegistry(String resourceKey) {
        registry = VariableRegistry.load(resourceKey);
    }

    protected void readDataToCache(File file, int sensorKey) throws IOException {
        final byte[] rawDataBuffer;
        try (FileInputStream fis = new FileInputStream(file)) {
            rawDataBuffer = fis.readAllBytes();
        }
        cache = new EpsVariableCache(rawDataBuffer, registry, sensorKey);
    }

    @Override
    public void close() throws IOException {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
        registry.clear();
        registry = null;
        pixelLocator = null;
        productSize = null;
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        final ArrayList<Variable> variables = new ArrayList<>();

        final Map<String, VariableDefinition> regVariables = registry.getVariables();
        final Set<String> keySet = regVariables.keySet();
        for (String variableName : keySet) {
            final VariableDefinition variableDefinition = regVariables.get(variableName);
            final int productDataType = variableDefinition.getProductData_type();
            final DataType netcdfDataType = NetCDFUtils.getNetcdfDataType(productDataType);
            final List<Attribute> attributes = extractCFAttributes(variableDefinition);

            final VariableProxy variable = new VariableProxy(variableName, netcdfDataType, attributes);
            variables.add(variable);
        }

        return variables;
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        final int width = interval.getX();
        final int height = interval.getY();
        final int[] timeArray = new int[width * height];

        final Dimension size = getProductSize();
        final int sceneRasterHeight = size.getNy();
        final int sceneRasterWidth = size.getNx();
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;
        int writeOffset = 0;
        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
        final TimeLocator timeLocator = getTimeLocator();

        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
            int lineTimeSeconds = fillValue;
            if (yRead >= 0 && yRead < sceneRasterHeight) {
                final long lineTimeMillis = timeLocator.getTimeFor(x, yRead);
                lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            }

            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
                if (xRead >= 0 && xRead < sceneRasterWidth) {
                    timeArray[writeOffset] = lineTimeSeconds;
                } else {
                    timeArray[writeOffset] = fillValue;
                }
                ++writeOffset;
            }
        }

        final int[] shape = new int[]{interval.getY(), interval.getX()};
        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
    }

    public Dimension getProductSize(int fov_count) {
        if (productSize == null) {
            final int numScanLines = cache.getMdrs().size();
            productSize = new Dimension("size", fov_count, numScanLines);
        }
        return productSize;
    }

    // public access for testing only tb 2025-09-17
    public static List<Attribute> extractCFAttributes(VariableDefinition variableDefinition) {
        final ArrayList<Attribute> attributes = new ArrayList<>();

        final String units = variableDefinition.getUnits();
        if (StringUtils.isNotNullAndNotEmpty(units)) {
            attributes.add(new Attribute("units", units));
        }

        final double scaleFactor = variableDefinition.getScale_factor();
        if (scaleFactor != 1.0) {
            attributes.add(new Attribute("scale_factor", scaleFactor));
            attributes.add(new Attribute("add_offset", 0.0));
        }

        final String dataType = variableDefinition.getData_type();
        if (StringUtils.isNotNullAndNotEmpty(dataType)) {
            final Number fillValue = EpsReaderUtils.getFillValue(dataType);
            if (fillValue != null) {
                attributes.add(new Attribute("_FillValue", fillValue));
            }
        }

        final String flagMeanings = variableDefinition.getFlag_meanings();
        final String flagValues = variableDefinition.getFlag_values();
        if (StringUtils.isNotNullAndNotEmpty(flagMeanings) && StringUtils.isNotNullAndNotEmpty(flagValues)) {
            attributes.add(new Attribute("flag_meanings", flagMeanings));

            final Array valuesArray = toValuesArray(flagValues, variableDefinition.getData_type());
            attributes.add(new Attribute("flag_values", valuesArray));
        }

        final String standardName = variableDefinition.getStandard_name();
        if (StringUtils.isNotNullAndNotEmpty(standardName)) {
            attributes.add(new Attribute("standard_name", standardName));
        }

        return attributes;
    }

    // public access for testing only tb 2025-09-17
    public static Array toValuesArray(String valuesString, String dataType) {
        final String[] valueStrings = StringUtils.split(valuesString, new char[]{','}, true);
        final int snapDataType = EpsReaderUtils.mapToProductData(dataType);

        Array dataVector = Array.factory(NetCDFUtils.getNetcdfDataType(snapDataType), new int[]{valueStrings.length});

        for (int i = 0; i < valueStrings.length; i++) {
            dataVector.setDouble(i, Double.parseDouble(valueStrings[i]));
        }
        return dataVector;
    }

    protected Geometries extractGeometries(Array longitudes, Array latitudes, int numSplits, Interval interval) throws IOException {
        final Geometries geometries = new Geometries();
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator(interval);

        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        Geometry timeAxisGeometry;

        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, numSplits, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, numSplits);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator(Interval interval) {
        return new BoundingPolygonCreator(interval, geometryFactory);
    }
}
