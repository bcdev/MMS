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
public class MatchupToolIntegrationTest_GBOV extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_gbov() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("gbov")
                .withTimeDeltaSeconds(900, null)
                .withMaxPixelDistanceKm(50, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-123.xml");

        insert_gbov();
        insert_miras_CDF3TA_June();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-156", "-end", "2016-156"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-156", "2016-156");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {

            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(30, matchupCount);

            NCTestUtils.assertStringVariable("gbov_site", null, 50, 0, "Barrow", mmd);
            NCTestUtils.assertStringVariable("gbov_station", null, 50, 0, "Barrow", mmd);
            NCTestUtils.assertStringVariable("gbov_IGBP_class", null, 50, 0, "Snow and Ice", mmd);
            NCTestUtils.assert3DVariable("gbov_elevation", 0, 0, 0, 11.f, mmd);
            NCTestUtils.assert3DVariable("gbov_Lat_IS", 0, 0, 0, 71.3231f, mmd);
            NCTestUtils.assert3DVariable("gbov_Lon_IS", 0, 0, 0, -156.61052f, mmd);

            NCTestUtils.assert3DVariable("gbov_TIME_IS", 0, 0, 0, 1.46504898E9, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_total", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_total_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_FIPAR_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_RM6_down_flag", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_RM6_up_flag", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_Clumping_Miller", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_Clumping_Miller_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_Clumping_Warren", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_Clumping_Warren_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Miller_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Miller_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Miller_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Miller_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Warren_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Warren_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Warren_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_Warren_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_total_Miller", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAI_total_Warren", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Miller_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Miller_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Miller_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Miller_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Warren_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Warren_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Warren_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LAIe_Warren_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAI_Miller", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAI_Miller_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAI_Warren", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAI_Warren_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAIe_Miller", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAIe_Miller_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAIe_Warren", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_PAIe_Warren_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Miller_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Miller_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Miller_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Miller_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Warren_down", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Warren_down_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Warren_up", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_clumping_Warren_up_err", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_RM7_down_flag", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_RM7_up_flag", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LSE", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LSE_STD", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LSR", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LSR_STD", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_QC_LSE", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_QC_LSR", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_LST", 0, 0, 12, 272.80047607421875f, mmd);
            NCTestUtils.assert3DVariable("gbov_LST_STD", 0, 0, 12, 0.038089923560619354f, mmd);
            NCTestUtils.assert3DVariable("gbov_QC_LST", 0, 0, 12, 0, mmd);
            NCTestUtils.assert3DVariable("gbov_QC_SM_5", 0, 0, 12, -999.f, mmd);
            NCTestUtils.assert3DVariable("gbov_SM_5", 0, 0, 12, -999.f, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Azimuth_Angle_175", 0, 0, 12, -22550.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_3_125", 1, 0, 13, -32768.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_175", 2, 0, 14, -996.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_H_425", 0, 1, 15, -6808.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Days_175", 1, 1, 16, 5999.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Eta_325", 2, 1, 17, -3650.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis2_125", 0, 2, 18, -21876.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Incidence_Angle_400", 1, 2, 0, -3615.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_RFI_Flags_425", 2, 2, 1, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nviews_575", 0, 0, 2, 20.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_H_075", 1, 0, 3, -32768.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_V_525", 2, 0, 4, -26642.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_H_025", 0, 1, 5, -32768.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Microseconds_400", 1, 1, 6, 842685.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Xi_400", 2, 1, 7, -6608.0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_lat", 0, 2, 8, 71.7425537109375, mmd);
        }
    }

    private void insert_gbov() throws IOException, SQLException {
        final String sensorKey = "gbov";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", sensorKey, "v1", "2016", "06", "GBOV__Barrow__Barrow__20160601T000000Z__20160630T235900Z.csv"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TA_June() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TA";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2016", "156", "SM_RE07_MIR_CDF3TA_20160604T000000_20160604T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "re07");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String sicSensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(sicSensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("miras-smos-CDF3TA"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(sicSensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("miras-smos-CDF3TA", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd123")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-123").getPath())
                .withDimensions(dimensions);
    }
}
