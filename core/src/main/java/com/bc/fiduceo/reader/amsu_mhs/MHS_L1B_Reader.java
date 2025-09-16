package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.*;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import com.bc.fiduceo.reader.time.TimeLocator_StartStopDate;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.SENSING_START_KEY;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.SENSING_STOP_KEY;

public class MHS_L1B_Reader extends Abstract_L1B_NatReader {

    public static final String RESOURCE_KEY = "MHS_L1B";
    private static final int NUM_SPLITS = 2;

    MHS_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        initializeRegistry(RESOURCE_KEY);
        readDataToCache(file, EPS_Constants.MHS_FOV_COUNT);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        MPHR recordMPHR = cache.getMPHR();
        setSensingDates(acquisitionInfo, recordMPHR);

        final Array lon = cache.getScaled("longitude");
        final Array lat = cache.getScaled("latitude");

        final Geometries geometries = extractGeometries(lon, lat, NUM_SPLITS, new Interval(10, 20));
        acquisitionInfo.setBoundingGeometry(geometries.getBoundingGeometry());
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "MHSx_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat";
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        // for the test file, the array returned contains only zeros - same as for AMSUA
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
}
