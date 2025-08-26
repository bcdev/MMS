package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

@RunWith(IOTestRunner.class)
public class AMSUA_L1B_Reader_IO_Test {

    @Test
    public void testReadAcquisitionInfo_MetopA() throws IOException, ParseException {
        final File file = createAmsuaMetopAPath("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");

        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader();
        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date expectedStart = sdf.parse("20160101234924Z");
            Date expectedStop  = sdf.parse("20160102013124Z");

            assertEquals(expectedStart, acquisitionInfo.getSensingStart());
            assertEquals(expectedStop, acquisitionInfo.getSensingStop());
            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());

            // @todo 1 tb/tb
            // acquisitionInfo.getBoundingGeometry();
            // @todo 1 tb/tb
            // acquisitionInfo.getTimeAxes();
        } finally {
            reader.close();
        }
    }

    private File createAmsuaMetopAPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"amsua-ma", "v8A", "2016", "01", "01", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
