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
    public void testRead_MODIS_unknownVariable() throws IOException {
        final File file = getMODISAquaFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcChunkCache ncTileCache = new NcChunkCache(netcdfFile);

            ncTileCache.read(new int[] {56}, new int[] {109},  "heffalump");
            fail("IOException expected");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testRead_AVHRR_GAC_2d_chunked() throws IOException {
        final File file = getWindsatFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcChunkCache ncTileCache = new NcChunkCache(netcdfFile);

            final Array array = ncTileCache.read(new int[] {0, 0}, new int[] {1, 1},  "latitude");
        }
    }

    @Test
    public void testRead_MODIS_AQUA_2d_chunked_with_group() throws IOException {
        final File file = getMODISAquaFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcChunkCache ncTileCache = new NcChunkCache(netcdfFile);

            final Array array = ncTileCache.read(new int[] {1, 1}, new int[] {1, 1},  "MODIS_SWATH_Type_L1B/Geolocation_Fields",
                    "Longitude");
        }
    }

    private File getMODISAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd021km-aq", "v61", "2011", "06", "17", "MYD021KM.A2011168.2210.061.2018032001033.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }

    private File getWindsatFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"windsat-coriolis", "v1.0", "2018", "04", "29", "RSS_WindSat_TB_L1C_r79285_20180429T174238_2018119_V08.0.nc"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
