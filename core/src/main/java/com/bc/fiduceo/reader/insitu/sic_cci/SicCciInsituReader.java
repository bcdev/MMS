package com.bc.fiduceo.reader.insitu.sic_cci;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.time.TimeLocator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SicCciInsituReader implements Reader {

    private static final String REG_EX = "ASCAT-vs-AMSR2-vs-ERA5-vs-DMISIC0-\\d{4}-[N|S].text";

    private FileReader fileReader;
    private ArrayList<String> linelist;

    @Override
    public void open(File file) throws IOException {
        fileReader = new FileReader(file);

        readLines();
    }

    private void readLines() throws IOException {
        linelist = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#")) {
                // skip comment lines tb 2022-11-03
                continue;
            }
            linelist.add(line);
        }
    }

    @Override
    public void close() throws IOException {
        if (linelist != null) {
            linelist.clear();
            linelist = null;
        }
        if (fileReader != null) {
            fileReader.close();
            fileReader = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        parseSensingTimes(acquisitionInfo);

        return acquisitionInfo;
    }

    @Override
    public String getRegEx() {
        return REG_EX;
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
        return "longitude";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude";
    }

    private void parseSensingTimes(AcquisitionInfo acquisitionInfo) throws IOException {
        Date minDate = new Date(Long.MAX_VALUE);
        Date maxDate = new Date(0);
        final ReferenceDataSection referenceDataSection = new ReferenceDataSection();
        try {
            for (String line :linelist) {
                referenceDataSection.parseTime(line);
                final Date refTime = referenceDataSection.getTime();
                if (minDate.after(refTime)) {
                    minDate = refTime;
                }
                if (maxDate.before(refTime)){
                    maxDate = refTime;
                }

                acquisitionInfo.setSensingStart(minDate);
                acquisitionInfo.setSensingStop(maxDate);
            }
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
    }
}
