package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelGeoCodingPixelLocator;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsr.AmsrUtils;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_StartStopDate;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.core.NodeType.UNDEFINED;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;

public class AMSUA_L1B_Reader extends Abstract_L1B_NatReader {

    public static final String RESOURCE_KEY = "AMSUA_L1B";
    private static final int NUM_SPLITS = 2;

    AMSUA_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
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
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(UNDEFINED);

        final MPHR recordMPHR = cache.getMPHR();
        setSensingDates(acquisitionInfo, recordMPHR);

        final Array lon = cache.getScaled("longitude");
        final Array lat = cache.getScaled("latitude");

        final Geometries geometries = extractGeometries(lon, lat, NUM_SPLITS, new Interval(6, 20));
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    static void ensureMdrVersionSupported(GENERIC_RECORD_HEADER header) {
        byte recordSubClass = header.getRecordSubClass();
        byte recordSubClassVersion = header.getRecordSubClassVersion();
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

        final int numScanLines = cache.getMdrs().size();
        return new TimeLocator_StartStopDate(sensingStart, sensingStop, numScanLines);
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = cache.getRaw(variableName);
        final VariableDefinition variableDef = registry.getVariableDef(variableName);
        final Number fillValue = EpsReaderUtils.getFillValue(variableDef.getData_type());
        final int numScanLines = cache.getMdrs().size();
        return RawDataReader.read(centerX, centerY, interval, fillValue, rawData, new Dimension("size", AMSUA_FOV_COUNT, numScanLines));
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implemented");
        // + SCENE_RADIANCE_01 -> SCENE_RADIANCE_15 , scale_factor 10^7, integer4
        // ANGULAR_RELATION, solar_zenith_angle, satellite_zenith_angle, solar_azimuth_angle, satellite_azimuth_angle
        //    scale_factor 10^2, units degree, integer2
        // + EARTH_LOCATION, latitude, longitude, scale_factor 10^4, units degree, integer4
        // SURFACE_PROPERTIES surface_property, property(0 = water, 1 = mixed/coast, 2 = land), integer2
        // TERRAIN_ELEVATION terrain_elevation, units m, integer2
        //
        // to be discussed:
        // REFLECTOR_A11_POSITION, REFLECTOR_A12_POSITION, REFLECTOR_A2_POSITION
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemented");
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
