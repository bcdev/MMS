package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;


public class GbovReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"gbov"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new GenericCsvReader(GenericCsvHelper.RESOURCE_KEY_GBOV);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.INSITU;
    }
}
