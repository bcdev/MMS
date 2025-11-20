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
 * Integration test for running SCOPE-TA (Total Alkalinity measurements) matchups on the entire database.
 * This test ingests all available SCOPE-TA satellite data and runs
 * matchup detection against the in-situ SCOPE-TA measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_ta_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-TA dataset
     * This test:
     * 1. Inserts the SCOPE-TA in-situ data
     * 2. Inserts ALL SCOPE-TA satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_TA_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-ta")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-ta.xml");

        // Insert in-situ data
        insert_scope_TA_insitu();

        // Insert ALL satellite data from all years
        insert_all_scope_sat_TA_data();

        // Write SCOPE-TA specific mmd-writer-config
        writeScopeMmdWriterConfig();

        // Run matchups for the complete time range
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1972-001",
            "-end", "2023-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-TA in-situ measurements
     */
    private void insert_scope_TA_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-ta";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp21", "SCOPE_WP21_TA_1972_2023.txt"}, true);

        try {
            final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "TA");
            storage.insert(satelliteObservation);
        } catch (IOException e) {
            System.out.println("Warning: Could not insert TA in-situ data: " + e.getMessage());
        }
    }

    /**
     * Insert ALL SCOPE-TA satellite data from available years
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_TA_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-ta";

        // Insert data for all years, all months
        for (int year = 1972; year <= 2023; year++) {
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
                    System.out.println("Warning: Could not insert TA satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-TA matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-ta"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-ta", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-ta")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }

    /**
     * Write SCOPE-TA specific mmd-writer-config with sensor name renaming
     */
    private void writeScopeMmdWriterConfig() throws IOException {
        final String config = "<mmd-writer-config>" +
                "    <overwrite>false</overwrite>" +
                "    <cache-size>512</cache-size>" +
                "    <reader-cache-size>3</reader-cache-size>" +
                "    <netcdf-format>N4</netcdf-format>" +
                "    <variables-configuration>" +
                "        <sensor-rename source-name=\"scope-ta\" target-name=\"scope_ta\"/>" +
                "        <sensor-rename source-name=\"scope-sat-ta\" target-name=\"scope_sat_ta\"/>" +
                "        <separator sensor-names=\"scope_ta, scope_sat_ta\" separator=\"_\"/>" +
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