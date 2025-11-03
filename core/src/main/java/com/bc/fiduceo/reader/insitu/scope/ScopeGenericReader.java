package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

class ScopeGenericReader implements Reader {

    private Reader actualReader;
    protected GeometryFactory geometryFactory;

    public ScopeGenericReader(ReaderContext readerContext) {
        geometryFactory = readerContext.getGeometryFactory();
    }

    @Override
    public void open(File file) throws IOException {
        actualReader = detectAndCreateReaderFromFilename(file.getName(), geometryFactory);
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
        return "SCOPE_WP\\d+.*\\.txt";
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
        int[] ymd = new int[3];
        String[] parts = fileName.split("_");
        String yearPart = parts[parts.length - 2];
        ymd[0] = Integer.parseInt(yearPart);
        ymd[1] = 1;
        ymd[2] = 1;
        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return actualReader.readRaw(centerX, centerY, interval, variableName);
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        return actualReader.readScaled(centerX, centerY, interval, variableName);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
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

    static Reader detectAndCreateReaderFromFilename(String fileName, GeometryFactory geometryFactory) throws IOException {
        // Normalize to uppercase for case-insensitive matching
        String upperFileName = fileName.toUpperCase();

        // Detect format based on filename pattern
        // Check _CDOC_ first (more specific than _DOC_)
        if (upperFileName.contains("_CDOC_")) {
            return new ScopeCDOCReader();
        } else if (upperFileName.contains("_DOC_")) {
            return new ScopeDOCReader(geometryFactory);
        } else if (upperFileName.contains("PHYTO")) {
            return new ScopePhytoReader(geometryFactory);
        } else if (upperFileName.contains("_PIC_")) {
            return new ScopePICReader(geometryFactory);
        } else if (upperFileName.contains("_POC_")) {
            return new ScopePOCReader(geometryFactory);
        } else if (upperFileName.contains("_PP_")) {
            return new ScopePPReader(geometryFactory);
        } else {
            throw new IOException("Unknown SCOPE file format. Cannot detect type from filename: " + fileName);
        }
    }
}