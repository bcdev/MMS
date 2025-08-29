package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MHS_L1B_Reader implements Reader {

    public static final String RESOURCE_KEY = "MHS_L1B";
    private static final int NUM_SPLITS = 2;

    private final GeometryFactory geometryFactory;
    private VariableRegistry registry;
    private EpsVariableCache cache;

    MHS_L1B_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        final byte[] rawDataBuffer;
        try (FileInputStream fis = new FileInputStream(file)) {
            rawDataBuffer = fis.readAllBytes();
        }
        registry = VariableRegistry.load(RESOURCE_KEY);
        cache = new EpsVariableCache(rawDataBuffer, registry, EPS_Constants.MHS_FOV_COUNT);
    }

    @Override
    public void close() throws IOException {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        MPHR recordMPHR = cache.getMPHR();
        setSensingDates(acquisitionInfo, recordMPHR);

        Array lon = cache.getRaw("longitude");
        Array lat = cache.getRaw("latitude");
        double scaleFactor_lon = registry.getVariableDef("longitude").getScale_factor();
        double scaleFactor_lat = registry.getVariableDef("latitude").getScale_factor();

        final Geometries geometries = extractGeometries(EpsReaderUtils.scale(lon, scaleFactor_lon), EpsReaderUtils.scale(lat, scaleFactor_lat));
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    private static void setSensingDates(AcquisitionInfo acquisitionInfo, MPHR recordMPHR) throws IOException {
        final Date sensingStart = recordMPHR.getDate("SENSING_START");
        acquisitionInfo.setSensingStart(sensingStart);
        final Date sensingEnd = recordMPHR.getDate("SENSING_END");
        acquisitionInfo.setSensingStop(sensingEnd);
    }

    @Override
    public String getRegEx() {
        return "MHSx_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat";
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
        throw new RuntimeException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        // todo 28-08-2025 BL: evaluate functionality and test edge cases
        Array array = cache.getRaw(variableName);
        // todo 28-08-2025 BL: figure out what the fill values are
        Number fillValue = Double.NaN;
        return RawDataReader.read(centerX, centerY, interval, fillValue, array, new Dimension("size", EPS_Constants.MHS_FOV_COUNT, 0));
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        // todo 28-08-2025 BL: evaluate functionality and test edge cases
        Array array = readRaw(centerX, centerY, interval, variableName);
        double scaleFactor = registry.getVariableDef(variableName).getScale_factor();
        return EpsReaderUtils.scale(array, scaleFactor);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getLatitudeVariableName() {
        throw new RuntimeException("not implemented");
    }


    private BoundingPolygonCreator getBoundingPolygonCreator() {
        return new BoundingPolygonCreator(new Interval(10, 20), geometryFactory);
    }

    private Geometries extractGeometries(Array longitudes, Array latitudes) throws IOException {
        final Geometries geometries = new Geometries();
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator();

        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        Geometry timeAxisGeometry;

        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, NUM_SPLITS, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, NUM_SPLITS);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }
}
