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
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reader for SCOPE monthly gridded satellite data files.
 *
 * Handles monthly Level-3 gridded ocean data organized in year/month folders.
 * Supported products:
 * - wp23 (Coastal Dissolved Organic Carbon)
 * - wp24 (Dissolved Organic Carbon)
 * - wp25 (Phytoplankton Carbon)
 * - wp26 (Primary Production)
 * - wpPIC (Particulate Inorganic Carbon)
 * - wpPOC (Particulate Organic Carbon)
 *
 * File organization: product/YYYY/MM/filename.nc
 * Temporal: Monthly composites (one file per month)
 * Spatial: Global or regional grids at 4km or 9km resolution
 */
class ScopeSatMonthlyReader extends NetCDFReader {

    // Match various SCOPE monthly file patterns
    // Examples:
    // SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_202001-fv6.0.out.nc (wp26)
    // SCOPE_NCEO_PC-MARANON_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-202001-fv6.0.out.nc (wp25)
    // SCOPE_NCEO_POC_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-202001-fv6.0.out.nc (wpPOC)
    // DOC_new_OC_199801.out.nc (wp24)
    // Global_DOC_199801.out.nc (wp23)
    // PIC_Prediction_202001.out.nc (wpPIC)
    private static final String REG_EX = ".*[-_](\\d{6})[-_.].*\\.nc";

    private static final Rectangle2D.Float BOUNDARY = new Rectangle2D.Float(-180.f, -90.f, 360.f, 180.f);

    private final ReaderContext readerContext;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;

    ScopeSatMonthlyReader(ReaderContext readerContext) {
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
        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");

        final double[] geoMinMax = extractMinMax(longitudes, latitudes);

        final GeometryFactory geometryFactory = readerContext.getGeometryFactory();
        final Polygon polygon = createPolygonFromMinMax(geoMinMax, geometryFactory);
        acquisitionInfo.setBoundingGeometry(polygon);

        // Extract sensing times from filename (YYYYMM format)
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
            final Array longitudes = arrayCache.get("lon");
            final Array latitudes = arrayCache.get("lat");

            float[] latArray = (float[]) latitudes.get1DJavaArray(DataType.FLOAT);

            // RasterPixelLocator requires latitudes in ascending order
            // If descending (last < first), reverse the array
            if (latArray.length > 1 && latArray[latArray.length - 1] < latArray[0]) {
                latArray = reverseArray(latArray);
            }

            pixelLocator = new RasterPixelLocator(
                    (float[]) longitudes.get1DJavaArray(DataType.FLOAT),
                    latArray,
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
            // For monthly composites, create a constant time locator
            // that returns the middle of the month for all pixels
            final int[] ymd = extractYearMonthDayFromFilename(netcdfFile.getLocation());
            final Calendar calendar = TimeUtils.getUTCCalendar();
            calendar.set(Calendar.YEAR, ymd[0]);
            calendar.set(Calendar.MONTH, ymd[1] - 1);
            calendar.set(Calendar.DAY_OF_MONTH, 15);  // middle of month
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            final long constantTime = calendar.getTimeInMillis();

            // Create a simple TimeLocator that always returns the same time
            timeLocator = new TimeLocator() {
                @Override
                public long getTimeFor(int x, int y) {
                    return constantTime;
                }
            };
        }
        return timeLocator;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        // Extract YYYYMM from filename
        final Pattern pattern = Pattern.compile(REG_EX);
        final Matcher matcher = pattern.matcher(fileName);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Filename does not match expected pattern: " + fileName);
        }

        final String datePart = matcher.group(1);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));  // year
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));  // month
        ymd[2] = 1;  // day - set to 1st of month for monthly data

        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        final Array array = arrayCache.get(variableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, variableName);

        // Handle scalar variables (rank 0) - e.g., time, depth in monthly files
        if (array.getRank() == 0) {
            return readScalarAsWindow(centerX, centerY, interval, fillValue, array);
        }

        // Special handling for longitude: it's a 1D array indexed by X, not Y
        if ("lon".equals(variableName) && array.getRank() == 1) {
            return readLongitudeWindow(centerX, centerY, interval, fillValue, array);
        }

        return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        // For monthly composites, return a constant time for the whole grid
        // Use middle of month (15th day at noon)
        final int[] ymd = extractYearMonthDayFromFilename(netcdfFile.getLocation());
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(Calendar.YEAR, ymd[0]);
        calendar.set(Calendar.MONTH, ymd[1] - 1);  // month is zero-based
        calendar.set(Calendar.DAY_OF_MONTH, 15);  // middle of month
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final int acquisitionTime = (int) (calendar.getTimeInMillis() / 1000L);

        final ArrayInt.D2 timeArray = new ArrayInt.D2(interval.getY(), interval.getX(), false);
        for (int i = 0; i < interval.getY(); i++) {
            for (int j = 0; j < interval.getX(); j++) {
                timeArray.set(i, j, acquisitionTime);
            }
        }

        return timeArray;
    }

    @Override
    public List<ucar.nc2.Variable> getVariables() throws InvalidRangeException, IOException {
        return netcdfFile.getVariables();
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");

        return new Dimension("size", (int) longitudes.getSize(), (int) latitudes.getSize());
    }

    @Override
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
    }

    // Package-private for testing
    static double[] extractMinMax(Array longitudes, Array latitudes) {
        final double[] minMax = new double[4];

        int size = (int) longitudes.getSize();
        final double lon0 = longitudes.getDouble(0);
        final double lon1 = longitudes.getDouble(size - 1);
        minMax[0] = Math.min(lon0, lon1);  // lonMin
        minMax[1] = Math.max(lon0, lon1);  // lonMax

        size = (int) latitudes.getSize();
        final double lat0 = latitudes.getDouble(0);
        final double lat1 = latitudes.getDouble(size - 1);
        minMax[2] = Math.min(lat0, lat1);  // latMin
        minMax[3] = Math.max(lat0, lat1);  // latMax

        return minMax;
    }

    // Package-private for testing
    static Polygon createPolygonFromMinMax(double[] minMax, GeometryFactory geometryFactory) {
        final double lonMin = minMax[0];
        final double lonMax = minMax[1];
        final double latMin = minMax[2];
        final double latMax = minMax[3];

        return geometryFactory.createPolygon(
                java.util.Arrays.asList(
                        geometryFactory.createPoint(lonMin, latMin),
                        geometryFactory.createPoint(lonMax, latMin),
                        geometryFactory.createPoint(lonMax, latMax),
                        geometryFactory.createPoint(lonMin, latMax),
                        geometryFactory.createPoint(lonMin, latMin)
                )
        );
    }

    private Array readLongitudeWindow(int centerX, int centerY, Interval interval, Number fillValue, Array array)
            throws IOException {
        final int halfWidth = interval.getX() / 2;

        final int offsetX = centerX - halfWidth;
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();

        final Dimension productSize = getProductSize();
        final int rawWidth = productSize.getNx();  // Size of the lon array (8640)

        // Handle different data types
        if (array.getElementType() == float.class) {
            return readLongitudeWindowFloat(offsetX, windowWidth, windowHeight, fillValue, (ArrayFloat.D1) array, rawWidth);
        } else if (array.getElementType() == double.class) {
            return readLongitudeWindowDouble(offsetX, windowWidth, windowHeight, fillValue, (ArrayDouble.D1) array, rawWidth);
        } else {
            // Fallback to default behavior for other types
            return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
        }
    }

    private Array readLongitudeWindowFloat(int offsetX, int windowWidth, int windowHeight, Number fillValue,
                                           ArrayFloat.D1 lonArray, int rawWidth) {
        final ArrayFloat.D2 windowArray = new ArrayFloat.D2(windowHeight, windowWidth);
        final float fillVal = fillValue.floatValue();

        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                final int xRaw = x + offsetX;

                if (xRaw >= 0 && xRaw < rawWidth) {
                    // Use X index for longitude, not Y
                    windowArray.set(y, x, lonArray.get(xRaw));
                } else {
                    windowArray.set(y, x, fillVal);
                }
            }
        }

        return windowArray;
    }

    private Array readLongitudeWindowDouble(int offsetX, int windowWidth, int windowHeight, Number fillValue,
                                            ArrayDouble.D1 lonArray, int rawWidth) {
        final ArrayDouble.D2 windowArray = new ArrayDouble.D2(windowHeight, windowWidth);
        final double fillVal = fillValue.doubleValue();

        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                final int xRaw = x + offsetX;

                if (xRaw >= 0 && xRaw < rawWidth) {
                    // Use X index for longitude, not Y
                    windowArray.set(y, x, lonArray.get(xRaw));
                } else {
                    windowArray.set(y, x, fillVal);
                }
            }
        }

        return windowArray;
    }

    private Array readScalarAsWindow(int centerX, int centerY, Interval interval, Number fillValue, Array array) {
        // Handle scalar variables (rank 0) like time, depth
        // Create a 2D window filled with the scalar value
        final int windowHeight = interval.getY();
        final int windowWidth = interval.getX();

        // Get the scalar value
        final Object scalarValue = array.getObject(0);

        // Create output array with same data type as input
        final Array windowArray = Array.factory(array.getDataType(), new int[]{windowHeight, windowWidth});
        final Index index = windowArray.getIndex();

        // Fill entire window with the scalar value
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                index.set(y, x);
                windowArray.setObject(index, scalarValue);
            }
        }

        return windowArray;
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) {
        final String location = netcdfFile.getLocation();
        final Path path = Paths.get(location);
        final String filename = path.getFileName().toString();

        final int[] ymd = extractYearMonthDayFromFilename(filename);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, ymd[0]);
        utcCalendar.set(Calendar.MONTH, ymd[1] - 1);  // month is zero-based

        // For monthly composites, set sensing start to beginning of month
        utcCalendar.set(Calendar.DAY_OF_MONTH, 1);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 0);
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        acquisitionInfo.setSensingStart(utcCalendar.getTime());

        // Set sensing stop to end of month
        // Get the last day of the month
        final int lastDay = utcCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        utcCalendar.set(Calendar.DAY_OF_MONTH, lastDay);
        utcCalendar.set(Calendar.HOUR_OF_DAY, 23);
        utcCalendar.set(Calendar.MINUTE, 59);
        utcCalendar.set(Calendar.SECOND, 59);
        utcCalendar.set(Calendar.MILLISECOND, 999);
        acquisitionInfo.setSensingStop(utcCalendar.getTime());
    }

    private static float[] reverseArray(float[] array) {
        final float[] reversed = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i];
        }
        return reversed;
    }
}
