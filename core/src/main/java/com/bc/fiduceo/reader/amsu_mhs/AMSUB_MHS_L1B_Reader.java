package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
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
import java.util.stream.Collectors;

public class AMSUB_MHS_L1B_Reader implements Reader {

    private FileInputStream fileInputStream;

    @Override
    public void open(File file) throws IOException {
        fileInputStream = new FileInputStream(file);
    }

    @Override
    public void close() throws IOException {
        if (fileInputStream != null) {
            fileInputStream.close();
            fileInputStream = null;
        }
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        byte[] bytes = fileInputStream.readAllBytes();
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();

        List<Record> records = RecordFactory.parseRecordsForIngestion(bytes);

        MPHR recordMPHR = (MPHR) records.get(0);
        List<MDR> recordsMDR = records.subList(1, records.size())
                .stream()
                .map(r -> (MDR) r)
                .collect(Collectors.toList());

        Date sensingStart = recordMPHR.getDate("SENSING_START");
        Date sensingEnd = recordMPHR.getDate("SENSING_END");

        // todo bl: boundingGeometry
        // todo bl: timeAxes

        acquisitionInfo.setSensingStart(sensingStart);
        acquisitionInfo.setSensingStop(sensingEnd);
        acquisitionInfo.setNodeType(NodeType.UNDEFINED);

        return acquisitionInfo;
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
