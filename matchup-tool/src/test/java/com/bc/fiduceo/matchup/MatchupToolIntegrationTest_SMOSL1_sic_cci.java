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
public class MatchupToolIntegrationTest_SMOSL1_sic_cci extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_DTUSIC1() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("DTUSIC1-sic-cci")
                .withTimeDeltaSeconds(28800, null)
                .withMaxPixelDistanceKm(10, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-44.xml");

        insert_DTU_SIC_CCI();
        insert_miras_CDF3TA_June();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-156", "-end", "2016-156"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-156", "2016-156");
        assertTrue(mmdFile.isFile());
        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(2, matchupCount);

            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_AMSR2_10.7GHzV", 0, 0, 0, 267.39f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_AMSR2_Earth-Azimuth", 0, 0, 1, -174.65f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_ASCAT_latitude", 0, 0, 0, 79.45f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_ASCAT_time", 0, 0, 1, 1465041600, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_ERA_istl1", 0, 0, 0, 273.16f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_ERA_tcwv", 0, 0, 1, 8.0443f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_QSCAT_longitude", 0, 0, 0, 174.50399780273438, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_QSCAT_std_inner", 0, 0, 1, 9.969209968386869E36, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_SMAP_RMSE_v", 0, 0, 0, 3.927370071411133, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_SMAP_nmp", 0, 0, 1, 19, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_SMOS_Tbh", 0, 0, 0, 236.36058044433594, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_SMOS_longitude", 0, 0, 1, 147.529f, mmd);
            NCTestUtils.assert3DVariable("DTUSIC1-sic-cci_areachange", 0, 0, 0, 0.998f, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Azimuth_Angle_125", 0, 0, 0, 9549, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_3_075", 1, 0, 1, 1158, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_125", 2, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_H_400", 0, 1, 1, 7770, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Days_125", 1, 1, 0, 5999, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Eta_275", 2, 1, 1, -4723, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis2_075", 0, 2, 0, -22329, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Incidence_Angle_375", 1, 2, 1, -5369, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_RFI_Flags_400", 2, 2, 0, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nviews_525", 0, 0, 1, 16, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_H_025", 1, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_V_475", 2, 0, 1, -30921, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_4_625", 0, 1, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Microseconds_375", 1, 1, 0, 44459, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Xi_375", 2, 1, 1, 1525, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_lon", 0, 2, 0, 174.1642608642578, mmd);
        }
    }

    @Test
    public void testMatchup_ANTXXXI() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("ANTXXXI-sic-cci")
                .withTimeDeltaSeconds(86400, null)
                .withMaxPixelDistanceKm(14, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-44.xml");

        insert_ANT_SIC_CCI();
        insert_miras_CDF3TA_Jan();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-001", "-end", "2016-002"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-001", "2016-002");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(2, matchupCount);

            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_AMSR2_18.7GHzH", 0, 0, 0, 9.969209968386869E36f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_AMSR2_6.9GHzV", 0, 0, 1, 9.969209968386869E36f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_AMSR2_latitude", 0, 0, 0, -75.026f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ASCAT_latitude", 0, 0, 1, -75.03f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ASCAT_time", 0, 0, 0, 1451736000, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ERA_fal", 0, 0, 1, 0.3189f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ERA_msl", 0, 0, 0, 997.4f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ERA_strd", 0, 0, 1, 204.21f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_ERA_u10", 0, 0, 0, 2.42f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_Ridge-height-primary", 0, 0, 1, 1.f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_SIC-secondary", 0, 0, 0, 20.f, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_Snow-cover-type-primary", 0, 0, 1, 4, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_Weather", 0, 0, 0, 1, mmd);
            NCTestUtils.assert3DVariable("ANTXXXI-sic-cci_time", 0, 0, 1, 1451714400, mmd);

            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Azimuth_Angle_125", 0, 0, 0, 8258, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_3_075", 1, 0, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_025", 2, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_4_625", 0, 1, 1, -2410, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_H_575", 1, 1, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_BT_V_525", 2, 1, 1, -586, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Days_475", 0, 2, 0, 5844, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Eta_425", 1, 2, 1, 836, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis1_400", 2, 2, 0, -15255, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Footprint_Axis2_375", 0, 0, 1, -19677, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Incidence_Angle_275", 1, 0, 0, -12760, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_RFI_Flags_225", 2, 0, 1, 0, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nb_SUN_Flags_175", 0, 1, 0, 31, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Nviews_125", 1, 1, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_3_075", 2, 1, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_4_025", 0, 2, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_4_625", 1, 2, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_H_575", 2, 2, 1, -27928, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_BT_Standard_Deviation_V_525", 0, 0, 0, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_3_475", 1, 0, 1, -24717, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_4_425", 2, 0, 0, -24640, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_H_400", 0, 1, 1, -28195, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Pixel_Radiometric_Accuracy_V_375", 1, 1, 0, -28065, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Microseconds_325", 2, 1, 1, 705385, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_UTC_Seconds_275", 0, 2, 0, 38954, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_Xi_175", 1, 2, 1, -32768, mmd);
            NCTestUtils.assert3DVariable("miras-smos-CDF3TA_lat", 2, 2, 0, -74.40499877929688, mmd);
        }
    }

    private void insert_DTU_SIC_CCI() throws IOException, SQLException {
        final String sensorKey = "DTUSIC1-sic-cci";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", sensorKey, "v3", "QSCAT-vs-SMAP-vs-SMOS-vs-ASCAT-vs-AMSR2-vs-ERA-vs-DTUSIC1-2016-N.text"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v3");
        storage.insert(satelliteObservation);
    }

    private void insert_ANT_SIC_CCI() throws IOException, SQLException {
        final String sensorKey = "ANTXXXI-sic-cci";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "sic-cci", sensorKey, "v3", "ASCAT-vs-AMSR2-vs-ERA-vs-ANTXXXI_2_FROSN_SeaIceObservations_reformatted.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v3");
        storage.insert(satelliteObservation);
    }

    private void insert_miras_CDF3TA_Jan() throws IOException, SQLException {
        final String sensorKey = "miras-smos-CDF3TA";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "re07", "2016", "001", "SM_RE07_MIR_CDF3TA_20160101T000000_20160101T235959_330_001_7.tgz"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "re07");
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

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd44")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "usecase-44").getPath())
                .withDimensions(dimensions);
    }
}
