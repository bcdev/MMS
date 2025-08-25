package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.RECORD_CLASS;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;
import com.bc.fiduceo.reader.amsu_mhs.nat.RecordFactory;
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

    private FileInputStream fileInputStream;
    private byte[] rawDataBuffer;

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
        final List<Record> records = RecordFactory.parseRecordsForIngestion(rawDataBuffer);

        final MPHR recordMPHR = (MPHR) records.get(0);
        setSensingDates(acquisitionInfo, recordMPHR);

        for (Record record : records) {
            GENERIC_RECORD_HEADER header = record.getHeader();
            RECORD_CLASS recordClass = header.getRecordClass();

            if (recordClass == RECORD_CLASS.MDR) {
                byte recordSubClass = header.getRecordSubClass();
                byte recordSubClassVersion = header.getRecordSubClassVersion();
                if (recordSubClass != 2 || recordSubClassVersion != 3) {
                    throw new IllegalStateException("Unsupported MDR version: " + recordSubClass + " v " + recordSubClassVersion);
                }

                MDR recordMDR = (MDR) record;
                int[] lons = recordMDR.parseVariable("longitude");
                int[] lats = recordMDR.parseVariable("latitude");
            }
        }

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
