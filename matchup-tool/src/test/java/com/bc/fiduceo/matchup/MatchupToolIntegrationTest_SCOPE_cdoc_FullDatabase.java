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
 * Integration test for running SCOPE-CDOC (Coastal Dissolved Organic Carbon) matchups on the entire database.
 * This test ingests all available SCOPE-CDOC satellite data and runs
 * matchup detection against the in-situ SCOPE-CDOC measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_cdoc_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-CDOC dataset
     * This test:
     * 1. Inserts the SCOPE-CDOC in-situ data
     * 2. Inserts ALL SCOPE-CDOC satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_coastal_doc_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-coastal-doc")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-coastal-doc.xml");

        // Insert in-situ data
        insert_scope_coastal_doc_insitu();

        // Insert ALL satellite data from all years
        insert_all_scope_sat_coastal_doc_data();

        // Run matchups for the complete time range
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1997-001",
            "-end", "2022-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-CDOC in-situ measurements
     */
    private void insert_scope_coastal_doc_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-coastal-doc";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp23", "SCOPE_WP23_CDOC_1997_2022.txt"}, true);

        try {
            final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "wp23");
            storage.insert(satelliteObservation);
        } catch (IOException e) {
            System.out.println("Warning: Could not insert CDOC in-situ data: " + e.getMessage());
        }
    }

    /**
     * Insert ALL SCOPE-CDOC satellite data from available years
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_coastal_doc_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-coastal-doc";

        // Insert data for all years, all months
        for (int year = 1998; year <= 2022; year++) {
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);
                String yearMonthStr = String.format("%04d%02d", year, month);

                final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                    new String[]{"satellite", "wp23", String.valueOf(year), monthStr,
                        "Global_DOC_" + yearMonthStr + ".out.nc"},
                    true);

                try {
                    final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
                    storage.insert(satelliteObservation);
                } catch (IOException e) {
                    // Log but continue if a file doesn't exist
                    System.out.println("Warning: Could not insert CDOC satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-CDOC matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-coastal-doc"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-coastal-doc", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-coastal-doc")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }
}
