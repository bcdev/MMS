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
 * Integration test for running SCOPE-Phytoplankton matchups on the entire SCOPE-Phytoplankton database.
 * This test ingests all available SCOPE-Phytoplankton satellite data (1998-2023) and runs
 * matchup detection against the in-situ SCOPE-Phytoplankton measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_phytoplankton_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-Phytoplankton dataset (1998-2023)
     * This test:
     * 1. Inserts the SCOPE-Phytoplankton in-situ data
     * 2. Inserts ALL SCOPE-Phytoplankton satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_phytoplankton_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-phytoplankton")
                .withMaxPixelDistanceKm(3, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-phytoplankton.xml");

        // Insert in-situ data
        insert_scope_phytoplankton_insitu();

        // Insert ALL satellite data from all years (1998-2023)
        insert_all_scope_sat_phytoplankton_data();

        // Run matchups for the complete time range
        // Date range: 1998-001 (Jan 1, 1998) to 2023-365 (Dec 31, 2023)
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1998-001",
            "-end", "2023-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-Phytoplankton in-situ measurements (1997-2023)
     */
    private void insert_scope_phytoplankton_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-phytoplankton";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp25", "SCOPE_WP25_PHYTO_CARBON_1997_2023.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "wp25");
        storage.insert(satelliteObservation);
    }

    /**
     * Insert ALL SCOPE-Phytoplankton satellite data from 1998-2023
     * This method inserts all monthly files for all available data
     */
    private void insert_all_scope_sat_phytoplankton_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-phytoplankton";

        // Insert data for all years from 1998 to 2023, all months
        for (int year = 1998; year <= 2023; year++) {
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%02d", month);
                String yearMonthStr = String.format("%04d%02d", year, month);

                final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                    new String[]{"satellite", "wp25", String.valueOf(year), monthStr,
                        "SCOPE_NCEO_PC-MARANON_ESA-OC-L3S-MERGED-1M_MONTHLY_4km_mapped-" + yearMonthStr + "-fv6.0.out.nc"},
                    true);

                try {
                    final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v1");
                    storage.insert(satelliteObservation);
                } catch (IOException e) {
                    // Log but continue if a file doesn't exist
                    System.out.println("Warning: Could not insert satellite data for " + year + "-" + monthStr + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create use case configuration for SCOPE-Phytoplankton matchups
     */
    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder(String primarySensor) {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor(primarySensor);
        primary.setPrimary(true);
        sensorList.add(primary);
        sensorList.add(new Sensor("scope-sat-phytoplankton"));

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension(primarySensor, 1, 1));
        dimensions.add(new com.bc.fiduceo.core.Dimension("scope-sat-phytoplankton", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("mmd-scope-phytoplankton")
                .withSensors(sensorList)
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }
}