package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class AMSUB_MHS_L1B_ReaderPlugin implements ReaderPlugin {

    private static final String[] SENSOR_KEYS = {"mhs-ma", "mhs-mb", "mhs-mc", "mhs-n18"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new AMSUB_MHS_L1B_Reader();
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
