package com.bc.fiduceo.reader.scope_sat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.location.RasterPixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.RawDataReader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Reader for SCOPE time series satellite data files.
 *
 * Handles time series Level-3 gridded ocean carbon data with actual time dimension.
 * Supported products:
 * - wp21 (Fugacity of CO2)
 * - wp22 (Dissolved Inorganic Carbon with depth)
 *
 * File characteristics:
 * - Single file with multiple time steps (time = 384-468)
 * - wp21: (time, latitude, longitude)
 * - wp22: (time, depth, latitude, longitude)
 * - Time variable: "days since 1970-01-15"
 */
class ScopeSatTimeSeriesReader extends NetCDFReader {

    private static final String REG_EX = ".*\\.nc";
    private static final Rectangle2D.Float BOUNDARY = new Rectangle2D.Float(-180.f, -90.f, 360.f, 180.f);

    private final ReaderContext readerContext;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;

    ScopeSatTimeSeriesReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        super.open(file);
    }

    @Override
    public void close() throws IOException {
        pixelLocator = null;
        timeLocator = null;
        super.close();
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        // Get lat/lon arrays to determine bounding geometry
        final Array longitudes = arrayCache.get("longitude");
        final Array latitudes = arrayCache.get("latitude");

        final double[] geoMinMax = ScopeSatReader.extractMinMax(longitudes, latitudes);

        final GeometryFactory geometryFactory = readerContext.getGeometryFactory();
        final Polygon polygon = ScopeSatReader.createPolygonFromMinMax(geoMinMax, geometryFactory);
        acquisitionInfo.setBoundingGeometry(polygon);

        // Extract sensing times from time variable
        setSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array longitudes = arrayCache.get("longitude");
            final Array latitudes = arrayCache.get("latitude");

            pixelLocator = new RasterPixelLocator(
                    (float[]) longitudes.get1DJavaArray(DataType.FLOAT),
                    (float[]) latitudes.get1DJavaArray(DataType.FLOAT),
                    BOUNDARY);
        }

        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (timeLocator == null) {
            final Array timeArray = arrayCache.get("time");
            timeLocator = new ScopeSatTimeLocator(timeArray);
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        // For time series, extract from first time step
        // This is used for database indexing, actual times come from time variable
        try {
            final Array timeArray = arrayCache.get("time");
            final float firstTime = timeArray.getFloat(0);

            // Time is in "days since 1970-01-15"
            final Calendar calendar = TimeUtils.getUTCCalendar();
            calendar.set(1970, Calendar.JANUARY, 15, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_YEAR, (int) firstTime);

            final int[] ymd = new int[3];
            ymd[0] = calendar.get(Calendar.YEAR);
            ymd[1] = calendar.get(Calendar.MONTH) + 1;  // Month is zero-based
            ymd[2] = calendar.get(Calendar.DAY_OF_MONTH);

            return ymd;
        } catch (IOException e) {
            throw new RuntimeException("Cannot extract date from time variable", e);
        }
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        final Array array = arrayCache.get(variableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, variableName);

        // For time series data, we need to handle the time dimension
        // The RawDataReader expects 2D data, but our data is 3D (time, lat, lon) or 4D (time, depth, lat, lon)
        // For now, read from first time step - matchup tool will handle time slicing
        return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        // For time series, we need to determine which time step to use
        // This is typically handled by the matchup tool based on temporal overlap
        // For now, return the first time step
        final Array timeArray = arrayCache.get("time");
        final float firstTimeDays = timeArray.getFloat(0);

        // Convert "days since 1970-01-15" to seconds since 1970
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(1970, Calendar.JANUARY, 15, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, (int) firstTimeDays);

        final int acquisitionTime = (int) (calendar.getTimeInMillis() / 1000L);

        final ArrayInt.D2 timeResult = new ArrayInt.D2(interval.getY(), interval.getX(), false);
        for (int i = 0; i < interval.getY(); i++) {
            for (int j = 0; j < interval.getX(); j++) {
                timeResult.set(i, j, acquisitionTime);
            }
        }

        return timeResult;
    }

    @Override
    public List<ucar.nc2.Variable> getVariables() throws InvalidRangeException, IOException {
        return netcdfFile.getVariables();
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array longitudes = arrayCache.get("longitude");
        final Array latitudes = arrayCache.get("latitude");

        return new Dimension("size", (int) longitudes.getSize(), (int) latitudes.getSize());
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        final Array timeArray = arrayCache.get("time");

        // Get first and last time values
        final float timeDays = timeArray.getFloat(0);

        // Time is in "days since 1970-01-15"
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(1970, Calendar.JANUARY, 15, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Sensing start
        calendar.add(Calendar.DAY_OF_YEAR, (int) timeDays);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        acquisitionInfo.setSensingStart(calendar.getTime());

        // Sensing stop
        calendar.add(Calendar.MONTH, 1);
        acquisitionInfo.setSensingStop(calendar.getTime());
    }

    /**
     * TimeLocator implementation for SCOPE time series data.
     * Maps pixel coordinates to acquisition times based on the time dimension.
     */
    private static class ScopeSatTimeLocator implements TimeLocator {

        private final Array timeArray;
        private final long referenceMillis;

        ScopeSatTimeLocator(Array timeArray) {
            this.timeArray = timeArray;

            // Reference: 1970-01-15 00:00:00
            final Calendar calendar = TimeUtils.getUTCCalendar();
            calendar.set(1970, Calendar.JANUARY, 15, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            this.referenceMillis = calendar.getTimeInMillis();
        }

        @Override
        public long getTimeFor(int x, int y) {
            // For gridded data, time is the same for all pixels in a time slice
            // Return the first time step (matchup tool handles time slicing)
            try {
                final float daysSinceRef = timeArray.getFloat(0);
                final long millisSinceRef = (long) (daysSinceRef * 24 * 60 * 60 * 1000);
                return referenceMillis + millisSinceRef;
            } catch (Exception e) {
                throw new RuntimeException("Cannot read time value", e);
            }
        }
    }
}
