package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_StartStopDate;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.VariableProxy;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.bc.fiduceo.core.NodeType.UNDEFINED;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;
import static ucar.ma2.DataType.INT;

public class AMSUA_L1B_Reader extends Abstract_L1B_NatReader {

    public static final String RESOURCE_KEY = "AMSUA_L1B";
    private static final int NUM_SPLITS = 2;
    private Dimension productSize;

    AMSUA_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
        productSize = null;
    }

    // package access for testing only tb 2025-09-17
    static List<Attribute> extractCFAttributes(VariableDefinition variableDefinition) {
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

    // package access for testing only tb 2025-09-17
    public static Array toValuesArray(String valuesString, String dataType) {
        final String[] valueStrings = StringUtils.split(valuesString, new char[]{','}, true);
        final int snapDataType = EpsReaderUtils.mapToProductData(dataType);

        Array dataVector = Array.factory(NetCDFUtils.getNetcdfDataType(snapDataType), new int[]{valueStrings.length});

        for (int i = 0; i < valueStrings.length; i++) {
            dataVector.setDouble(i, Double.parseDouble(valueStrings[i]));
        }
        return dataVector;
    }

    @Override
    public void open(File file) throws IOException {
        initializeRegistry(RESOURCE_KEY);
        readDataToCache(file, EPS_Constants.AMSUA_FOV_COUNT);

        final List<MDR> mdrs = cache.getMdrs();
        ensureMdrVersionSupported(mdrs.get(0).getHeader());
    }

    @Override
    public void close() throws IOException {
        productSize = null;
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(UNDEFINED);

        final MPHR recordMPHR = cache.getMPHR();
        setSensingDates(acquisitionInfo, recordMPHR);

        final Array lon = cache.getScaled(LON_VAR_NAME);
        final Array lat = cache.getScaled(LAT_VAR_NAME);

        final Geometries geometries = extractGeometries(lon, lat, NUM_SPLITS, new Interval(6, 20));
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    static void ensureMdrVersionSupported(GENERIC_RECORD_HEADER header) {
        final byte recordSubClass = header.getRecordSubClass();
        final byte recordSubClassVersion = header.getRecordSubClassVersion();
        if (recordSubClass != 2 || recordSubClassVersion != 3) {
            throw new IllegalStateException("Unsupported MDR version: " + recordSubClass + " v " + recordSubClassVersion);
        }
    }

    @Override
    public String getRegEx() {
        return "AMSA_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat";
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        // for the test file, the array returned contains only zeros - same as for MHS
        // According to the sparse documentation, I would expect this to contain seconds since epoch
        // tb 2025-09-04
        // Array timeAttitude = cache.getRaw("TIME_ATTITUDE");

        // Instead, we interpolate between header start and stop times tb 2025-09-04
        final MPHR mphr = cache.getMPHR();
        // @todo 2 tb this is not good, because I need to know the name, better offer explicit getters for sensing start and stop
        final Date sensingStart = mphr.getDate(SENSING_START_KEY);
        final Date sensingStop = mphr.getDate(SENSING_STOP_KEY);

        return new TimeLocator_StartStopDate(sensingStart, sensingStop, getProductSize().getNy());
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
        // + SCENE_RADIANCE_01 -> SCENE_RADIANCE_15 , scale_factor 10^7, integer4
        // ANGULAR_RELATION, solar_zenith_angle, satellite_zenith_angle, solar_azimuth_angle, satellite_azimuth_angle
        //    scale_factor 10^2, units degree, integer2
        // + EARTH_LOCATION, latitude, longitude, scale_factor 10^4, units degree, integer4
        // SURFACE_PROPERTIES surface_property, property(0 = water, 1 = mixed/coast, 2 = land), integer2
        // TERRAIN_ELEVATION terrain_elevation, units m, integer2
        //
        // to be discussed:
        // REFLECTOR_A11_POSITION, REFLECTOR_A12_POSITION, REFLECTOR_A2_POSITION

        return variables;
    }

    @Override
    public Dimension getProductSize() throws IOException {
        if (productSize == null) {
            final int numScanLines = cache.getMdrs().size();

            productSize = new Dimension("size", AMSUA_FOV_COUNT, numScanLines);
        }
        return productSize;
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }
}
