package com.bc.fiduceo.reader.amsr2;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderPlugin;

public class AMSR2_ReaderPlugin implements ReaderPlugin {

    @Override
    public Reader createReader(GeometryFactory geometryFactory) {
        return new AMSR2_Reader(geometryFactory);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return new String[]{"amsr2-gcw1"};
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
