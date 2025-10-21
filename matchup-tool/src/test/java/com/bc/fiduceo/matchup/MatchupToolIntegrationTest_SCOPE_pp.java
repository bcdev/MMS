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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_pp extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_scope_pp() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-pp")
                .withTimeDeltaSeconds(0, null)  // 0 seconds - match by month only via reader sensing times
                .withMaxPixelDistanceKm(15, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-pp.xml");

        insert_scope_pp_insitu();
        insert_scope_sat_pp_April_2016();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-092", "-end", "2016-121"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-092", "2016-121");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {

            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(21, matchupCount);

            NCTestUtils.assert3DVariable("scope-pp_PP", 0, 0, 0, 377.05f, mmd);
            NCTestUtils.assert3DVariable("scope-pp_latitude", 0, 0, 0, 32.955f, mmd);
            NCTestUtils.assert3DVariable("scope-pp_longitude", 0, 0, 0, -117.305f, mmd);

            NCTestUtils.assert3DVariable("scope-sat-pp_pp", 1, 1, 0, Float.NaN, mmd);
            NCTestUtils.assert3DVariable("scope-sat-pp_lat", 1, 1, 0, 32.9375f, mmd);
            NCTestUtils.assert3DVariable("scope-sat-pp_lon", 1, 1, 0, -117.3125f, mmd);
        }
    }

    private void insert_scope_pp_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-pp";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "scope", "wp26", "SCOPE_WP26_PP_1958_2021.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_scope_sat_pp_April_2016() throws IOException, SQLException {
        final String sensorKey = "scope-sat-pp";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"scope-merge", "wp26", "2016", "04", "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_201604-fv6.0.out.nc"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-pp"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-pp", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-pp")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-scope-pp").getPath())
                .withDimensions(dimensions);
    }
}
