package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MHS_L1B_Reader implements Reader {

    public static final String RESOURCE_KEY = "MHS_L1B";
    private static final int NUM_SPLITS = 2;

    private FileInputStream fileInputStream;
    private byte[] rawDataBuffer;
    private final VariableRegistry registry;

    private final GeometryFactory geometryFactory;

    MHS_L1B_Reader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
        this.registry = VariableRegistry.load(RESOURCE_KEY);
    }

    @Override
    public void open(File file) throws IOException {
        fileInputStream = new FileInputStream(file);
        rawDataBuffer =  null;
    }

    @Override
    public void close() throws IOException {
        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }
        rawDataBuffer = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        rawDataBuffer = fileInputStream.readAllBytes();
        List<Record> records = RecordFactory.parseRecords(rawDataBuffer);

        MPHR recordMPHR = (MPHR) records.get(0);
        setSensingDates(acquisitionInfo, recordMPHR);

        List<MDR> recordsMDR = MdrUtilities.getMdrList(records);

        List<Array> coordinates = extractCoordinates(recordsMDR);
        final Geometries geometries = extractGeometries(coordinates.get(0), coordinates.get(1));

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
        throw new RuntimeException("not implemented");
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

    private List<Array> extractCoordinates(List<MDR> recordsMDR) {
        List<Array> coordinates = new ArrayList<>();

        int numScanLines = recordsMDR.size();

        Array longitudes = new ArrayDouble.D2(numScanLines, EPS_Constants.MHS_FOV_COUNT);
        Array latitudes = new ArrayDouble.D2(numScanLines, EPS_Constants.MHS_FOV_COUNT);

        for (int ii = 0; ii < numScanLines; ii++) {
            MDR mdr = recordsMDR.get(ii);
            byte[] locationBytes = new byte[EPS_Constants.MHS_EARTH_LOCATIONS_TOTAL_BYTE_SIZE];
            System.arraycopy(mdr.getPayload(), EPS_Constants.MHS_L1B_EARTH_LOCATIONS_OFFSET, locationBytes, 0, EPS_Constants.MHS_EARTH_LOCATIONS_TOTAL_BYTE_SIZE);
            final ByteBuffer byteBuffer = ByteBuffer.wrap(locationBytes);

            for (int jj = 0; jj < EPS_Constants.MHS_FOV_COUNT; jj++) {
                int lat = byteBuffer.getInt();
                int lon = byteBuffer.getInt();
                latitudes.setDouble((ii * EPS_Constants.MHS_FOV_COUNT) + jj, (double) lat / EPS_Constants.MHS_EARTH_LOCATIONS_SCALE_FACTOR);
                longitudes.setDouble((ii * EPS_Constants.MHS_FOV_COUNT) + jj, (double) lon / EPS_Constants.MHS_EARTH_LOCATIONS_SCALE_FACTOR);
            }
        }

        coordinates.add(longitudes);
        coordinates.add(latitudes);
        return coordinates;
    }
}
