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
import ucar.ma2.*;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * Reader for SCOPE monthly gridded satellite data files.
 * <p>
 * Handles monthly Level-3 gridded ocean data organized in year/month folders.
 * Supported products:
 * - wp21 (Carbonate System)
 * - wp22 (Interior Dissolved Inorganic Carbon)
 * - wp23 (Coastal Dissolved Organic Carbon)
 * - wp24 (Dissolved Organic Carbon)
 * - wp25 (Phytoplankton Carbon)
 * - wp26 (Primary Production)
 * - PIC (Particulate Inorganic Carbon)
 * - POC (Particulate Organic Carbon)
 * <p>
 * File organization: product/YYYY/MM/filename.nc
 * Temporal: Monthly composites (one file per month)
 * Spatial: Global or regional grids at 4km or 9km resolution
 */
class ScopeSatReader extends NetCDFReader {

    // Match various SCOPE monthly file patterns
    // Examples:
    // UExP-FNN-U_physics_carbonatesystem_ESASCOPE_v5_198501.nc (wp21)
    // SCOPE_Interior_DIC_Data_0000_199101.nc (wp22)
    // SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_202001-fv6.0.out.nc (wp26)
    // SCOPE_NCEO_PC-MARANON_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-202001-fv6.0.out.nc (wp25)
    // SCOPE_NCEO_POC_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-202001-fv6.0.out.nc (POC)
    // DOC_new_OC_199801.out.nc (wp24)
    // Global_DOC_199801.out.nc (wp23)
    // PIC_Prediction_202001.out.nc (PIC)
    private static final String REG_EX_SCOPE = ".*SCOPE.*\\.nc|.*PIC.*\\.nc|.*POC.*\\.nc";
    private static final String REG_EX_YYYYMM = ".*(\\d{6}).*\\.nc";

    private static final Rectangle2D.Float BOUNDARY = new Rectangle2D.Float(-180.0f, -90.0f, 360.0f, 180.0f);

    private final ReaderContext readerContext;
    private PixelLocator pixelLocator;
    private TimeLocator timeLocator;
    private Boolean latitudesDescending;  // Cached state: null = not checked, true/false = checked

    ScopeSatReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    // Package-private for testing
    static double[] extractMinMax(Array lonArray, Array latArray) {
        final int finalLon = (int) lonArray.getSize() - 1;
        final int finalLat = (int) latArray.getSize() - 1;

        final double[] minMax = new double[4];
        minMax[0] = Math.min(lonArray.getDouble(0), lonArray.getDouble(finalLon));
        minMax[1] = Math.max(lonArray.getDouble(0), lonArray.getDouble(finalLon));
        minMax[2] = Math.min(latArray.getDouble(0), latArray.getDouble(finalLat));
        minMax[3] = Math.max(latArray.getDouble(0), latArray.getDouble(finalLat));

        return minMax;
    }

    // Package-private for testing
    static Polygon createPolygonFromMinMax(double[] minMax, GeometryFactory geometryFactory) {
        final double lonMin = minMax[0];
        final double lonMax = minMax[1];
        final double latMin = minMax[2];
        final double latMax = minMax[3];

        return geometryFactory.createPolygon(asList(geometryFactory.createPoint(lonMin, latMin),
                geometryFactory.createPoint(lonMax, latMin), geometryFactory.createPoint(lonMax, latMax),
                geometryFactory.createPoint(lonMin, latMax), geometryFactory.createPoint(lonMin, latMin)));
    }

    private static float[] reverseArray(float[] array) {
        final float[] reverseArray = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            reverseArray[i] = array[array.length - 1 - i];
        }
        return reverseArray;
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

        final Array lonArray = arrayCache.get(getLonVarName());
        final Array latArray = arrayCache.get(getLatVarName());
        final double[] minMax = extractMinMax(lonArray, latArray);

        final GeometryFactory geometryFactory = readerContext.getGeometryFactory();
        final Polygon polygon = createPolygonFromMinMax(minMax, geometryFactory);
        acquisitionInfo.setBoundingGeometry(polygon);

        setSensingTimes(acquisitionInfo);

        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX_SCOPE;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array lonArray = arrayCache.get(getLonVarName());
            final Array latArray = arrayCache.get(getLatVarName());
            final float[] lonData = (float[]) lonArray.get1DJavaArray(DataType.FLOAT);
            final float[] latData = (float[]) latArray.get1DJavaArray(DataType.FLOAT);

            latitudesDescending = ((latData.length > 1) && (latData[latData.length - 1] < latData[0]));
            pixelLocator = new RasterPixelLocator(lonData, latitudesDescending ? reverseArray(latData) : latData,
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
            final long constantTime = getCalendarFromFilename().getTimeInMillis();
            timeLocator = (x, y) -> constantTime;
        }
        return timeLocator;
    }

    private Calendar getCalendarFromFilename() {
        final int[] ymd = extractYearMonthDayFromFilename(netcdfFile.getLocation());
        final Calendar calendar = TimeUtils.getUTCCalendar();
        calendar.set(Calendar.YEAR, ymd[0]);
        calendar.set(Calendar.MONTH, ymd[1] - 1);  // month is zero-based
        calendar.set(Calendar.DAY_OF_MONTH, ymd[2]);  // first day of month
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final Pattern pattern = Pattern.compile(REG_EX_YYYYMM);
        final Matcher matcher = pattern.matcher(fileName);

        if (!matcher.matches()) {
            return new int[]{0, 0, 0};
        }

        final String datePart = matcher.group(1);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));  // year
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));  // month
        ymd[2] = 1;  // first day of month

        return ymd;
    }

    @Override
    public Array readRaw(int centerX,
                         int centerY,
                         Interval interval,
                         String variableName) throws IOException, InvalidRangeException {
        final Array array = arrayCache.get(variableName);
        final Number fillValue = arrayCache.getNumberAttributeValue(NetCDFUtils.CF_FILL_VALUE_NAME, variableName);

        // Handle scalar variables
        if (array.getRank() == 0) {
            return readWindowScalar(interval, array);
        }
        // Handle longitude coordinate
        if (variableName.equals(getLonVarName()) && array.getRank() == 1) {
            return readWindowLon(centerX, centerY, interval, fillValue, array);
        }
        // Adjust Y coordinate for descending latitudes
        final Dimension productSize = getProductSize();
        final int mappedCenterY = mapYCoordinate(centerY, productSize.getNy());

        return RawDataReader.read(centerX, mappedCenterY, interval, fillValue, array, productSize);
    }

    @Override
    public Array readScaled(int centerX,
                            int centerY,
                            Interval interval,
                            String variableName) throws IOException, InvalidRangeException {
        return readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        // Returns the same time for the whole interval
        final Calendar calendar = getCalendarFromFilename();
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
        final Array lonArray = arrayCache.get(getLonVarName());
        final Array latArray = arrayCache.get(getLatVarName());

        return new Dimension("size", (int) lonArray.getSize(), (int) latArray.getSize());
    }

    @Override
    public String getLongitudeVariableName() {
        return getLonVarName();
    }

    private String getLonVarName() {
        return getVariableName("lon", "longitude");
    }

    @Override
    public String getLatitudeVariableName() {
        return getLatVarName();
    }

    private String getLatVarName() {
        return getVariableName("lat", "latitude");
    }

    private String getVariableName(String defaultName, String alternativeName) {
        if (netcdfFile.getRootGroup().findVariable(alternativeName) != null) {
            return alternativeName;
        }
        return defaultName;
    }

    private Array readWindowLon(int centerX,
                                int centerY,
                                Interval interval,
                                Number fillValue,
                                Array array) throws IOException {
        final int windowWidth = interval.getX();
        final int windowHeight = interval.getY();
        final int offsetX = centerX - windowWidth / 2;
        final Dimension productSize = getProductSize();
        final int rawWidth = productSize.getNx();
        // Handle different data types
        if (array.getElementType() == float.class)
            return readWindowLonFloat(offsetX, windowWidth, windowHeight, fillValue, (ArrayFloat.D1) array, rawWidth);
        else if (array.getElementType() == double.class) {
            return readWindowLonDouble(offsetX, windowWidth, windowHeight, fillValue, (ArrayDouble.D1) array, rawWidth);
        } else {
            // Fallback to default behavior for other types
            return RawDataReader.read(centerX, centerY, interval, fillValue, array, getProductSize());
        }
    }

    private Array readWindowLonFloat(int offsetX,
                                     int windowWidth,
                                     int windowHeight,
                                     Number fillValue,
                                     ArrayFloat.D1 lonArray,
                                     int rawWidth) {
        final ArrayFloat.D2 windowArray = new ArrayFloat.D2(windowHeight, windowWidth);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                final int rawX = x + offsetX;
                if (rawX >= 0 && rawX < rawWidth) {
                    windowArray.set(y, x, lonArray.get(rawX));
                } else {
                    windowArray.set(y, x, fillValue.floatValue());
                }
            }
        }
        return windowArray;
    }

    private Array readWindowLonDouble(int offsetX,
                                      int windowWidth,
                                      int windowHeight,
                                      Number fillValue,
                                      ArrayDouble.D1 lonArray,
                                      int rawWidth) {
        final ArrayDouble.D2 windowArray = new ArrayDouble.D2(windowHeight, windowWidth);
        for (int y = 0; y < windowHeight; y++) {
            for (int x = 0; x < windowWidth; x++) {
                final int rawX = x + offsetX;
                if (rawX >= 0 && rawX < rawWidth) {
                    windowArray.set(y, x, lonArray.get(rawX));
                } else {
                    windowArray.set(y, x, fillValue.doubleValue());
                }
            }
        }
        return windowArray;
    }

    private Array readWindowScalar(Interval interval, Array array) {
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

    private boolean areLatitudesDescending() throws IOException {
        if (latitudesDescending == null) {
            final Array latArray = arrayCache.get(getLatVarName());
            final int size = (int) latArray.getSize();
            if (size > 1) {
                final double startLat = latArray.getDouble(0);
                final double finalLat = latArray.getDouble(size - 1);
                latitudesDescending = finalLat < startLat;
            } else {
                latitudesDescending = false;
            }
        }
        return latitudesDescending;
    }

    private int mapYCoordinate(int centerY, int rawHeight) throws IOException {
        if (areLatitudesDescending()) {
            // Flip Y coordinate for descending latitudes
            return rawHeight - 1 - centerY;
        }
        return centerY;
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) {
        final Calendar calendar = getCalendarFromFilename();
        // Set sensing start to beginning of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        acquisitionInfo.setSensingStart(calendar.getTime());
        // Set sensing stop to end of month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        acquisitionInfo.setSensingStop(calendar.getTime());
    }
}
