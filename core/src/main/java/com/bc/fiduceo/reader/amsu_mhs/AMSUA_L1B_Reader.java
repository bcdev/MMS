package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;

public class AMSUA_L1B_Reader extends Abstract_L1B_NatReader {

    public static final String RESOURCE_KEY = "AMSUA_L1B";

    AMSUA_L1B_Reader(ReaderContext readerContext) {
        super(readerContext);
    }

    @Override
    public void open(File file) throws IOException {
        initializeRegistry(RESOURCE_KEY);
        readDataToCache(file, EPS_Constants.AMSUA_FOV_COUNT);

        final List<MDR> mdrs = cache.getMdrs();
        ensureMdrVersionSupported(mdrs.get(0).getHeader());
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        return super.read(new Interval(6, 20));
    }

    @Override
    public String getRegEx() {
        return "AMSA_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat";
    }

    @Override
    public Dimension getProductSize() throws IOException {
        return super.getProductSize(AMSUA_FOV_COUNT);
    }

    static void ensureMdrVersionSupported(GENERIC_RECORD_HEADER header) {
        final byte recordSubClass = header.getRecordSubClass();
        final byte recordSubClassVersion = header.getRecordSubClassVersion();
        if (recordSubClass != 2 || recordSubClassVersion != 3) {
            throw new IllegalStateException("Unsupported MDR version: " + recordSubClass + " v " + recordSubClassVersion);
        }
    }
}
