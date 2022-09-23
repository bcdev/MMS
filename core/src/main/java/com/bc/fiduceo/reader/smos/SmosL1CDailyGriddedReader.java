package com.bc.fiduceo.reader.smos;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class SmosL1CDailyGriddedReader extends NetCDFReader {

    private final ReaderContext readerContext;

    private File productDir;

    SmosL1CDailyGriddedReader(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void open(File file) throws IOException {
        if (ReaderUtils.isCompressed(file)) {
            final String fileName = FileUtils.getFilenameWithoutExtension(file);
            final long millis = System.currentTimeMillis();
            productDir = readerContext.createDirInTempDir(fileName + millis);

            try {
                final File inputFile = extractFromTar(file);
                super.open(inputFile);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException("Unsupported format, this reader accepts only compressed input (tgz)");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (productDir != null) {
            readerContext.deleteTempFile(productDir);
            productDir = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final Array longitudes = arrayCache.get("lon");
        final Array latitudes = arrayCache.get("lat");

        final Polygon polygon = extractPolygonFromMinMax(longitudes, latitudes, readerContext.getGeometryFactory());
        acquisitionInfo.setBoundingGeometry(polygon);

        setSensingTimes(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return "SM_RE07_MIR_CDF3T[AD]_(\\d{8}T\\d{6}_){2}\\d{3}_\\d{3}_\\d{1}.tgz";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String datePart = fileName.substring(19, 27);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));
        ymd[2] = Integer.parseInt(datePart.substring(6, 8));
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public List<Variable> getVariables() throws InvalidRangeException, IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Dimension getProductSize() throws IOException {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "lon";
    }

    @Override
    public String getLatitudeVariableName() {
        return "lat";
    }


    private File extractFromTar(File file) throws IOException {
        TarArchiveInputStream tarIn = null;
        final int oneMb = 1024 * 1024;

        try {
            final BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
            final GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream);
            tarIn = new TarArchiveInputStream(gzipIn);

            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                if (entry.isFile()) {
                    if (entry.getName().endsWith(".DBL.nc")) {
                        // uncompress and open
                        int count;
                        byte[] data = new byte[oneMb];
                        final File targetFile = new File(productDir, entry.getName());
                        FileOutputStream fos = new FileOutputStream(targetFile, false);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, oneMb)) {
                            while ((count = tarIn.read(data, 0, oneMb)) != -1) {
                                dest.write(data, 0, count);
                            }
                        }
                        return targetFile;
                    }
                }
            }
            throw new IOException("No suitable netcdf file found in tar");
        } finally {
            if (tarIn != null) {
                tarIn.close();
            }
        }
    }

    private void setSensingTimes(AcquisitionInfo acquisitionInfo) {
        final String location = netcdfFile.getLocation();
        final String filename = FileUtils.getFilenameFromPath(location);
        final int[] ymd = extractYearMonthDayFromFilename(filename);

        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.set(Calendar.YEAR, ymd[0]);
        utcCalendar.set(Calendar.MONTH, ymd[1] - 1);    // month is zero-based tb 2022-09-15
        utcCalendar.set(Calendar.DAY_OF_MONTH, ymd[2]);

        acquisitionInfo.setSensingStart(utcCalendar.getTime());

        utcCalendar.set(Calendar.HOUR, 23);
        utcCalendar.set(Calendar.MINUTE, 59);
        utcCalendar.set(Calendar.SECOND, 59);

        acquisitionInfo.setSensingStop(utcCalendar.getTime());
    }

    // package access for testing only tb 2022-09-15
    static Polygon extractPolygonFromMinMax(Array longitudes, Array latitudes, GeometryFactory geometryFactory) {
        int size = (int) longitudes.getSize();
        final double lonMin = longitudes.getDouble(0);
        final double lonMax = longitudes.getDouble(size - 1);

        size = (int) latitudes.getSize();
        final double latMin = latitudes.getDouble(0);
        final double latMax = latitudes.getDouble(size - 1);

        final Point ll = geometryFactory.createPoint(lonMin, latMin);
        final Point ul = geometryFactory.createPoint(lonMin, latMax);
        final Point ur = geometryFactory.createPoint(lonMax, latMax);
        final Point lr = geometryFactory.createPoint(lonMax, latMin);
        final ArrayList<Point> polygonPoints = new ArrayList<>();
        polygonPoints.add(ll);
        polygonPoints.add(ul);
        polygonPoints.add(ur);
        polygonPoints.add(lr);
        polygonPoints.add(ll);
        return geometryFactory.createPolygon(polygonPoints);
    }
}
