package com.bc.fiduceo.matchup;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Integration test for running SCOPE-DIC (Dissolved Inorganic Carbon measurements) matchups on the entire database.
 * This test inserts all available SCOPE-DIC satellite data and runs
 * matchup detection against the in-situ SCOPE-DIC measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_Dic_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-DIC dataset
     * This test:
     * 1. Inserts the SCOPE-DIC in-situ data
     * 2. Inserts ALL SCOPE-DIC satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_DIC_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-dic")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-dic.xml");

        // Insert in-situ data
        insert_scope_DIC_insitu();

        // Insert ALL satellite data from all years
        insert_all_scope_sat_DIC_data();

        // Write SCOPE-DIC specific mmd-writer-config
        writeScopeMmdWriterConfig();

        // Run matchups for the complete time range
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1985-001",
            "-end", "2022-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-DIC in-situ measurements
     */
    private void insert_scope_DIC_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-dic";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp21", "SCOPE_WP21_DIC_1985_2022.txt"}, true);

        try {
            final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "DIC");
            storage.insert(satelliteObservation);
        } catch (IOException e) {
            System.out.println("Warning: Could not insert DIC in-situ data: " + e.getMessage());
        }
    }

    /**
     * Insert ALL SCOPE-DIC satellite data from available years
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_DIC_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-dic";

        // Insert data for all years, all months
        for (int year = 1985; year <= 2022; year++) {
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);
                String yearMonthStr = String.format("%04d%02d", year, month);

                final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                    new String[]{"satellite", "wp21", String.valueOf(year), monthStr,
                        "UExP-FNN-U_physics_carbonatesystem_ESASCOPE_v5_" + yearMonthStr + ".nc"},
                    true);

                try {
                    final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
                    storage.insert(satelliteObservation);
                } catch (IOException e) {
                    // Log but continue if a file doesn't exist
                    System.out.println("Warning: Could not insert DIC satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-DIC matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-dic"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-dic", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-dic")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }

    /**
     * Write SCOPE-DIC specific mmd-writer-config with sensor name renaming
     */
    private void writeScopeMmdWriterConfig() throws IOException {
        final String config = "<mmd-writer-config>" +
                "    <overwrite>false</overwrite>" +
                "    <cache-size>512</cache-size>" +
                "    <reader-cache-size>3</reader-cache-size>" +
                "    <netcdf-format>N4</netcdf-format>" +
                "    <variables-configuration>" +
                "        <sensor-rename source-name=\"scope-dic\" target-name=\"scope_dic\"/>" +
                "        <sensor-rename source-name=\"scope-sat-dic\" target-name=\"scope_sat_dic\"/>" +
                "        <separator sensor-names=\"scope_dic, scope_sat_dic\" separator=\"_\"/>" +
                "    </variables-configuration>" +
                "</mmd-writer-config>";

        // Delete existing config file if it exists
        final File configFile = new File(configDir, "mmd-writer-config.xml");
        if (configFile.exists()) {
            configFile.delete();
        }

        TestUtil.writeMmdWriterConfigFile(configDir, config);
    }
}