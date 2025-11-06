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
 * Integration test for running SCOPE-PIC (Particulate Inorganic Carbon) matchups on the entire database.
 * This test ingests all available SCOPE-PIC satellite data and runs
 * matchup detection against the in-situ SCOPE-PIC measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_pic_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-PIC dataset
     * This test:
     * 1. Inserts the SCOPE-PIC in-situ data
     * 2. Inserts ALL SCOPE-PIC satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_pic_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-pic")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-pic.xml");

        // Insert in-situ data
        insert_scope_pic_insitu();

        // Insert ALL satellite data from all years
        insert_all_scope_sat_pic_data();

        // Write SCOPE-PIC specific mmd-writer-config
        writeScopeMmdWriterConfig();

        // Run matchups for the complete time range
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1998-001",
            "-end", "2019-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-PIC in-situ measurements
     */
    private void insert_scope_pic_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-pic";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "PIC", "SCOPE_PIC_PIC_1998_2019.txt"}, true);

        try {
            final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "PIC");
            storage.insert(satelliteObservation);
        } catch (IOException e) {
            System.out.println("Warning: Could not insert PIC in-situ data: " + e.getMessage());
        }
    }

    /**
     * Insert ALL SCOPE-PIC satellite data from available years
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_pic_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-pic";

        // Insert data for all years, all months
        for (int year = 1998; year <= 2019; year++) {
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);
                String yearMonthStr = String.format("%04d%02d", year, month);

                final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                    new String[]{"satellite", "PIC", String.valueOf(year), monthStr,
                        "PIC_Prediction_" + yearMonthStr + ".out.nc"},
                    true);

                try {
                    final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
                    storage.insert(satelliteObservation);
                } catch (IOException e) {
                    // Log but continue if a file doesn't exist
                    System.out.println("Warning: Could not insert PIC satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-PIC matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-pic"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-pic", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-pic")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }

    /**
     * Write SCOPE-PIC specific mmd-writer-config with sensor name renaming
     */
    private void writeScopeMmdWriterConfig() throws IOException {
        final String config = "<mmd-writer-config>" +
                "    <overwrite>false</overwrite>" +

                "    <netcdf-format>N4</netcdf-format>" +
                "    <variables-configuration>" +
                "        <sensor-rename source-name=\"scope-pic\" target-name=\"scope_pic\"/>" +
                "        <sensor-rename source-name=\"scope-sat-pic\" target-name=\"scope_sat_pic\"/>" +
                "        <separator sensor-names=\"scope_pic, scope_sat_pic\" separator=\"_\"/>" +
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