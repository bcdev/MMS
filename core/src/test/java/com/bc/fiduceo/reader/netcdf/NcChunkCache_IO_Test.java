package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class NcChunkCache_IO_Test {

    @Test
    public void testRead_AVHRR_unknownVariable() throws IOException {
        final File file = getAvhrrGACFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcChunkCache ncTileCache = new NcChunkCache(netcdfFile);

            ncTileCache.read(new int[] {56}, new int[] {109},  "heffalump");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testRead_AVHRR_GAC_2d_chunked() throws IOException {
        final File file = getAvhrrGACFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcChunkCache ncTileCache = new NcChunkCache(netcdfFile);

            final Array array = ncTileCache.read(new int[] {0, 0}, new int[] {1, 1},  "lat");
        }
    }

    private File getAvhrrGACFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-ma", "v2.10.2", "2010", "01", "01", "20100101113716-ESACCI-L1C-AVHRRMTA_G-v1.5-fv02.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getWindsatFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"windsat-coriolis", "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
