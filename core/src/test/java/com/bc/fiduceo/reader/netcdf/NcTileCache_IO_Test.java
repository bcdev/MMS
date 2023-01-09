package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class NcTileCache_IO_Test {

    @Test
    public void testRead_MODIS_unknownVariable() throws IOException {
        final File file = getMODISAquaFile();

        try (NetcdfFile netcdfFile = NetcdfFiles.open(file.getPath())) {
            final NcTileCache ncTileCache = new NcTileCache(netcdfFile);

            ncTileCache.read(new int[] {56}, new int[] {109},  "heffalump");
            fail("IOException expected");
        } catch (IOException expected) {

        }
    }

    private File getMODISAquaFile() throws IOException {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"myd021km-aq", "v61", "2011", "06", "17", "MYD021KM.A2011168.2210.061.2018032001033.hdf"}, false);
        return TestUtil.getTestDataFileAsserted(testFilePath);
    }
}
