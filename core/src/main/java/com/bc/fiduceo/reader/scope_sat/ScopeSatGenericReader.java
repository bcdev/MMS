package com.bc.fiduceo.reader.scope_sat;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic reader for SCOPE satellite data that auto-detects the file type.
 * <p>
 * Handles two types of SCOPE satellite data:
 * 1. Monthly composites (wp23-26, wpPIC, wpPOC): Files organized in year/month folders, no time dimension
 * 2. Time series (wp21-wp22): Single files with time dimension (time = 384-468)
 * <p>
 * The reader opens the NetCDF file, checks for time dimension, and delegates to the appropriate reader.
 */
class ScopeSatGenericReader implements Reader {

    private static final String MONTHLY_REG_EX = ".*_(\\d{6})[-_].*\\.nc";

    private final ReaderContext readerContext;
    private Reader actualReader;

    ScopeSatGenericReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        actualReader = detectAndCreateReader(file);
        actualReader.open(file);
    }

    @Override
    public void close() throws IOException {
        if (actualReader != null) {
            actualReader.close();
            actualReader = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return actualReader.read();
    }

    @Override
    public String getRegEx() {
        // Match any SCOPE satellite NetCDF file
        return ".*SCOPE.*\\.nc|.*PIC.*\\.nc|.*POC.*\\.nc";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        return actualReader.getPixelLocator();
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return actualReader.getSubScenePixelLocator(sceneGeometry);
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        return actualReader.getTimeLocator();
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        // Extract YYYYMM from filename
        final Pattern pattern = Pattern.compile(MONTHLY_REG_EX);
        final Matcher matcher = pattern.matcher(fileName);

        final int[] ymd = new int[3];
        if (!matcher.matches()) {
            return ymd;
        }

        final String datePart = matcher.group(1);

        ymd[0] = Integer.parseInt(datePart.substring(0, 4));  // year
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));  // month
        ymd[2] = 1;  // day - set to 1st of month for monthly data

        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        return actualReader.readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName)
            throws IOException, InvalidRangeException {
        return actualReader.readScaled(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval)
            throws IOException, InvalidRangeException {
        return actualReader.readAcquisitionTime(x, y, interval);
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        return actualReader.getVariables();
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return actualReader.getProductSize();
    }

    @Override
    public String getLongitudeVariableName() {
        return actualReader.getLongitudeVariableName();
    }

    @Override
    public String getLatitudeVariableName() {
        return actualReader.getLatitudeVariableName();
    }

    /**
     * Detects the type of SCOPE satellite file and creates the appropriate reader.
     * <p>
     * Detection logic:
     * 1. Opens NetCDF file temporarily
     * 2. Checks for "time" dimension
     * 3. If time dimension exists and size > 1 → TimeSeriesReader (wp21-wp22)
     * 4. Otherwise → MonthlyReader (wp23-26, wpPIC, wpPOC)
     *
     * @param file The NetCDF file to analyze
     * @return Appropriate reader for the file type
     * @throws IOException If file cannot be opened or read
     */
    Reader detectAndCreateReader(File file) throws IOException {
        NetcdfFile netcdfFile = null;
        try {
            netcdfFile = NetcdfFile.open(file.getAbsolutePath());

            // Check for time dimension
            ucar.nc2.Dimension timeDim = netcdfFile.findDimension("time");

            if (timeDim != null && timeDim.getLength() > 1) {
                // Time series file (wp21-wp22) - has substantial time dimension
                return new ScopeSatTimeSeriesReader(readerContext);
            } else {
                // Monthly composite file (wp23-26, wpPIC, wpPOC) - no time or time=1
                return new ScopeSatMonthlyReader(readerContext);
            }

        } finally {
            if (netcdfFile != null) {
                netcdfFile.close();
            }
        }
    }
}
