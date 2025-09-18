/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

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

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class MatchupToolIntegrationTest_AMSUA_point extends AbstractUsecaseIntegrationTest {

    @Test
    public void testMatchup_AMSUA_location_extracts() throws IOException, ParseException, SQLException, InvalidRangeException {
        final File mmdWriterConfig = new File(configDir, "mmd-writer-config.xml");
        if (!mmdWriterConfig.delete()) {
            fail("unable to delete test file");
        }
        TestUtil.writeMmdWriterConfig(configDir);

        final UseCaseConfig useCaseConfig = createUseCaseConfigBuilder()
                .withLocationElement(150.1052, 20.8303)
                .createConfig();
        final File useCaseConfigFile = storeUseCaseConfig(useCaseConfig, "usecase-amsua.xml");

        insert_AMSUA();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-u", useCaseConfigFile.getName(), "-start", "2016-001", "-end", "2016-002"};
        MatchupToolMain.main(args);

        final File mmdFile = getMmdFilePath(useCaseConfig, "2016-001", "2016-002");
        assertTrue(mmdFile.isFile());

        try (NetcdfFile mmd = NetcdfFile.open(mmdFile.getAbsolutePath())) {
            final int matchupCount = NetCDFUtils.getDimensionLength(FiduceoConstants.MATCHUP_COUNT, mmd);
            assertEquals(1, matchupCount);

            NCTestUtils.assert3DVariable("amsua-ma_SCENE_RADIANCE_01", 0, 0, 0, 10912, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_SCENE_RADIANCE_07", 1, 0, 0, 61606, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_SCENE_RADIANCE_14", 2, 0, 0, 74835, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_SURFACE_PROPERTIES", 0, 1, 0, 0, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_acquisition_time", 1, 1, 0, 1451693029, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_longitude", 2, 1, 0, 1494646, mmd);
            NCTestUtils.assert3DVariable("amsua-ma_satellite_zenith_angle", 0, 2, 0, 4035, mmd);
        }
    }

    private void insert_AMSUA() throws IOException, SQLException {
        final String sensorKey = "amsua-ma";
        final String relativeArchivePath = TestUtil.assembleFileSystemPath(new String[]{sensorKey, "v8A", "2016", "01", "01", "AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat"}, true);

        final SatelliteObservation satelliteObservation = readSatelliteObservation(sensorKey, relativeArchivePath, "v8A");
        storage.insert(satelliteObservation);
    }

    private MatchupToolTestUseCaseConfigBuilder createUseCaseConfigBuilder() {
        final List<Sensor> sensorList = new ArrayList<>();
        final Sensor primary = new Sensor("amsua-ma");
        primary.setPrimary(true);
        sensorList.add(primary);

        final List<com.bc.fiduceo.core.Dimension> dimensions = new ArrayList<>();
        dimensions.add(new com.bc.fiduceo.core.Dimension("amsua-ma", 3, 3));

        return (MatchupToolTestUseCaseConfigBuilder) new MatchupToolTestUseCaseConfigBuilder("amsua-ma")
                .withSensors(sensorList)
                .withOutputPath(new File(TestUtil.getTestDir().getPath(), "amsua-ma").getPath())
                .withDimensions(dimensions);
    }
}
