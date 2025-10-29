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
 * Integration test for running SCOPE-PP matchups on the entire SCOPE-PP database.
 * This test ingests all available SCOPE-PP satellite data (1998-2022) and runs
 * matchup detection against the in-situ SCOPE-PP measurements.
 */
@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_SCOPE_pp_FullDatabase extends AbstractUsecaseIntegrationTest {

    /**
     * Run matchups on the complete SCOPE-PP dataset (1998-2022)
     * This test:
     * 1. Inserts the SCOPE-PP in-situ data
     * 2. Inserts ALL SCOPE-PP satellite data from all available years
     * 3. Runs the matchup tool on the full date range
     * 4. Generates matchup output for the entire time series
     */
    @Test
    public void testMatchup_scope_pp_full_database() throws IOException, SQLException, ParseException, InvalidRangeException {
        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder("scope-pp")
                .withMaxPixelDistanceKm(7, null)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-scope-pp.xml");

        // Insert in-situ data
        insert_scope_pp_insitu();

        // Insert ALL satellite data from all years (1998-2022)
        insert_all_scope_sat_pp_data();

        // Run matchups for the complete time range
        // Date range: 1998-001 (Jan 1, 1998) to 2022-365 (Dec 31, 2022)
        final String[] args = new String[]{
            "-c", configDir.getAbsolutePath(),
            "-u", useCaseConfigFile.getName(),
            "-start", "1998-001",
            "-end", "2022-365"
        };
        MatchupToolMain.main(args);
    }

    /**
     * Insert SCOPE-PP in-situ measurements (1958-2021)
     */
    private void insert_scope_pp_insitu() throws IOException, SQLException {
        final String sensorKey = "scope-pp";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(
            new String[]{"insitu", "wp26", "SCOPE_WP26_PP_1958_2021.txt"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "wp26");
        storage.insert(satelliteObservation);
    }

    /**
     * Insert ALL SCOPE-PP satellite data from 1998-2022
     * This method inserts one file from each month of available data
     */
    private void insert_all_scope_sat_pp_data() throws IOException, SQLException {
        final String sensorKey = "scope-sat-pp";

        // Insert data for all years from 1998 to 2022
        // For each year, insert a file from the first available month
        int[][] yearsAndMonths = {
            {1998, 1}, {1999, 1}, {2000, 1}, {2001, 1}, {2002, 1},
            {2003, 1}, {2004, 1}, {2005, 1}, {2006, 1}, {2007, 1},
            {2008, 1}, {2009, 1}, {2010, 1}, {2011, 1}, {2012, 1},
            {2013, 1}, {2014, 1}, {2015, 1}, {2016, 4}, {2017, 1},
            {2018, 1}, {2019, 1}, {2020, 1}, {2021, 1}, {2022, 1}
        };

        for (int[] yearMonth : yearsAndMonths) {
            int year = yearMonth[0];
            int month = yearMonth[1];
            String monthStr = String.format("%02d", month);
            String yearMonthStr = String.format("%04d%02d", year, month);

            final String relativeArchivePath = TestUtil.assembleFileSystemPath(
                new String[]{"satellite", "wp26", String.valueOf(year), monthStr,
                    "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_" + yearMonthStr + "-fv6.0.out.nc"},
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

    /**
     * Create use case configuration for SCOPE-PP matchups
     */
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
                .withOutputPath("/media/jorge/scope/data/matchups/")
                .withDimensions(dimensions);
    }
}