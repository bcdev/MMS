package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

import java.io.IOException;

public class ScopeReaderPlugin implements ReaderPlugin {

    private final String[] SUPPORTED_KEYS = {"scope-coastal-doc", "scope-doc", "scope-phytoplankton", "scope-pic", "scope-poc","scope-pp"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new ScopeGenericReader();
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