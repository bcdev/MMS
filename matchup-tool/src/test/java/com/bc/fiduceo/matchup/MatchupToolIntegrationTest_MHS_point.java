package com.bc.fiduceo.matchup;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.util.NetCDFUtils;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_MHS_point extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_MHS_location_extracts() throws IOException, SQLException, ParseException, InvalidRangeException {
        final File mmdWriterConfig = new File(configDir, "mmd-writer-config.xml");
        if (!mmdWriterConfig.delete()) {
            fail("unable to delete test file");
        }
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withLocationElement(50, 60)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-mhs.xml");

        insert_MHS();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2025-232", "-end", "2025-233"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2025-232", "2025-233");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("mhs-mc_FOV_DATA_QUALITY", 0, 0, 0, 0, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_SCENE_RADIANCES_03", 1, 0, 0, 732588, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_SCENE_RADIANCES_05", 2, 0, 0, 868447, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_SURFACE_PROPERTIES", 0, 1, 0, 2, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_acquisition_time", 1, 1, 0, 1755669867, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_longitude", 2, 1, 0, 491101, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_satellite_zenith_angle", 0, 2, 0, 5302, mmd);
            NCTestUtils.assert3DVariable("mhs-mc_TERRAIN_ELEVATION", 0, 2, 0, 0, mmd);
        }
    }

    private void insert_MHS() throws IOException, SQLException {
        final String sensorKey = "mhs-mc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v10", "2025", "08", "20", "MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v10");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("mhs-mc");
        primary.setPrimary(true);
        sensorList.add(primary);

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("mhs-mc", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mhs-mc")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "mhs-mc").getPath())
                .withDimensions(dimensions);
    }
}
