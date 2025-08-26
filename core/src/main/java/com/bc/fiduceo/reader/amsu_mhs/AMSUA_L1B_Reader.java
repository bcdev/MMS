package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
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

import static com.bc.fiduceo.core.NodeType.UNDEFINED;

public class AMSUA_L1B_Reader implements Reader {

    public static final String RESOURCE_KEY = "AMSUA_L1B";

    private FileInputStream fileInputStream;
    private byte[] rawDataBuffer;
    private final VariableRegistry registry;

    public AMSUA_L1B_Reader() {
        this.registry = VariableRegistry.load(RESOURCE_KEY);
    }

    @Override
    public void open(File file) throws IOException {
        fileInputStream = new FileInputStream(file);
        rawDataBuffer = null;
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
        acquisitionInfo.setNodeType(UNDEFINED);

        rawDataBuffer = fileInputStream.readAllBytes();
        final List<Record> records = RecordFactory.parseRecords(rawDataBuffer);

        final MPHR recordMPHR = (MPHR) records.get(0);
        setSensingDates(acquisitionInfo, recordMPHR);

        final List<MDR> recordsMDR = MdrUtilities.getMdrList(records);
        final GENERIC_RECORD_HEADER header = recordsMDR.get(0).getHeader();
        ensureMdrVersionSupported(header);

        int numScanLines = recordsMDR.size();

        // detect data type and allocate array
        for (Record record : recordsMDR) {
            // calculate byte offset and size in payload
            // read subsection from payload
            // interprete and convert data to native type
            // copy to appropriate array location
        }

        return acquisitionInfo;
    }

    // @todo 2 tb/tb add tests 2025-08-26
    static void ensureMdrVersionSupported(GENERIC_RECORD_HEADER header) {
        byte recordSubClass = header.getRecordSubClass();
        byte recordSubClassVersion = header.getRecordSubClassVersion();
        if (recordSubClass != 2 || recordSubClassVersion != 3) {
            throw new IllegalStateException("Unsupported MDR version: " + recordSubClass + " v " + recordSubClassVersion);
        }
    }

    private static void setSensingDates(AcquisitionInfo acquisitionInfo, MPHR recordMPHR) throws IOException {
        final Date sensingStart = recordMPHR.getDate("SENSING_START");
        acquisitionInfo.setSensingStart(sensingStart);
        final Date sensingEnd = recordMPHR.getDate("SENSING_END");
        acquisitionInfo.setSensingStop(sensingEnd);
    }

    @Override
    public String getRegEx() {
        throw new RuntimeException("not implemented");
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
}
