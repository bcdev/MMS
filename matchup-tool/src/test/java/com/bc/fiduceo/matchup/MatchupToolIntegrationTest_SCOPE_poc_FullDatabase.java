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
 * Integration test for running SCOPE-POC matchups on the entire SCOPE-POC database.
 * This test ingests all available SCOPE-POC satellite data (1998-2020) and runs
 * matchup detection against the in-situ SCOPE-POC measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_poc_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-POC dataset (1998-2020)
     * This test:
     * 1. Inserts the SCOPE-POC in-situ data
     * 2. Inserts ALL SCOPE-POC satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_poc_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-poc")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-poc.xml");

        // Insert in-situ data
        insert_scope_poc_insitu();

        // Insert ALL satellite data from all years (1998-2020)
        insert_all_scope_sat_poc_data();

        // Write SCOPE-POC specific mmd-writer-config
        writeScopeMmdWriterConfig();

        // Run matchups for the complete time range
        // Date range: 1998-001 (Jan 1, 1998) to 2020-365 (Dec 31, 2020)
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1998-001",
            "-end", "2020-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-POC in-situ measurements
     */
    private void insert_scope_poc_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-poc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "POC", "SCOPE_POC_POC_1997_2020.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "POC");
        storage.insert(satelliteObservation);
    }

    /**
     * Insert ALL SCOPE-POC satellite data from 1998-2020
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_poc_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-poc";

        // Insert data for all years from 1998 to 2020, all months
        for (int year = 1998; year <= 2020; year++) {
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);
                String yearMonthStr = String.format("%04d%02d", year, month);

                final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                    new String[]{"satellite", "POC", String.valueOf(year), monthStr,
                        "SCOPE_NCEO_POC_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-" + yearMonthStr + "-fv6.0.out.nc"},
                    true);

                try {
                    final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "POC");
                    storage.insert(satelliteObservation);
                } catch (IOException e) {
                    // Log but continue if a file doesn't exist
                    System.out.println("Warning: Could not insert satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-POC matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-poc"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-poc", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-poc")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }

    /**
     * Write SCOPE-POC specific mmd-writer-config with sensor name renaming
     */
    private void writeScopeMmdWriterConfig() throws IOException {
        final String config = "<mmd-writer-config>" +
                "    <overwrite>false</overwrite>" +
                "    <cache-size>512</cache-size>" +
                "    <reader-cache-size>3</reader-cache-size>" +
                "    <netcdf-format>N4</netcdf-format>" +
                "    <variables-configuration>" +
                "        <sensor-rename source-name=\"scope-poc\" target-name=\"scope_poc\"/>" +
                "        <sensor-rename source-name=\"scope-sat-poc\" target-name=\"scope_sat_poc\"/>" +
                "        <separator sensor-names=\"scope_poc, scope_sat_poc\" separator=\"_\"/>" +
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