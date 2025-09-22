package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;

import java.io.File;
import java.io.IOException;

import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;

public class MHS_L1B_Reader extends Abstract_L1B_NatReader {

    public static final String RESOURCE_KEY = "MHS_L1B";

    MHS_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        initializeRegistry(RESOURCE_KEY);
        readDataToCache(file, EPS_Constants.MHS_FOV_COUNT);
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return super.read(new Interval(10, 20));
    }

    @Override
    public String getRegEx() {
        return "MHSx_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat";
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return super.getProductSize(MHS_FOV_COUNT);
    }
}
