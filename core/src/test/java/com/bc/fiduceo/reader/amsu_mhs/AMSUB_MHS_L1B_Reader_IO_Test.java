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

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class AMSUB_MHS_L1B_Reader_IO_Test {

    @Test
    public void testReadAcquisitionInfo_MetopC() throws IOException, ParseException {
        final File file = createMhsMetopCPath("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");

        final AMSUB_MHS_L1B_Reader reader = new AMSUB_MHS_L1B_Reader();
        try {
            reader.open(file);
            AcquisitionInfo acquisitionInfo = reader.read();

            // todo bl: assert bounding geometry and time axes

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date expectedStart = sdf.parse("20250820060350Z");
            Date expectedStop = sdf.parse("20250820074550Z");

            assertEquals(expectedStart, acquisitionInfo.getSensingStart());
            assertEquals(expectedStop, acquisitionInfo.getSensingStop());
            assertEquals(NodeType.UNDEFINED, acquisitionInfo.getNodeType());
        } finally {
            reader.close();
        }
    }

    private File createMhsMetopCPath(String fileName) throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"mhs-mc", "v10", "2025", "08", "20", fileName}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}