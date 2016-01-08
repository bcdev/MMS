/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.ingest;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(IOTestRunner.class)
public class IngestionToolIntegrationTest {

    private File configDir;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testIngest_notInputParameter() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        final String[] args = new String[0];
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_help() throws ParseException, IOException, SQLException {
        // @todo 4 tb/tb find a way to steal system.err to implement assertions 2015-12-09
        String[] args = new String[]{"-h"};
        IngestionToolMain.main(args);

        args = new String[]{"--help"};
        IngestionToolMain.main(args);
    }

    @Test
    public void testIngest_missingSystemProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs-aqua"};

        writeDatabaseProperties();

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_missingDatabaseProperties() throws ParseException, IOException, SQLException {
        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs-aqua"};

        writeSystemProperties();

        try {
            IngestionToolMain.main(args);
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    @Test
    public void testIngest_AIRS() throws ParseException, IOException, SQLException {
        // @todo 1 tb/** this test relies on the results being returned in a specifi order - change this 2015-12-22
        // @todo 2 tb/tb move geometry factory type to some other location, parametrize test 2015-12-16
        final Storage storage = Storage.create(getDatasource(), new GeometryFactory(GeometryFactory.Type.JTS));
        storage.initialize();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-s", "airs-aqua"};

        try {
            writeSystemProperties();
            writeDatabaseProperties();

            IngestionToolMain.main(args);

            final List<SatelliteObservation> satelliteObservations = storage.get();
            assertEquals(3, satelliteObservations.size());

            final SatelliteObservation observation = satelliteObservations.get(1);
            final Sensor sensor = observation.getSensor();
            assertEquals("airs-aqua", sensor.getName());

            // @todo 2 tb/** check why the database strips off the hour/minute/second part of the dates  2015-12-22
            assertEquals("2015-09-02", observation.getStartTime().toString());
            assertEquals("2015-09-02", observation.getStopTime().toString());
            // @todo 1 tb/** something is wrong with the path stored in the DB check and resolve 2015-12-22
            assertTrue(observation.getDataFile().getAbsolutePath().contains("AIRS.2015.09.02.023.L1B.AIRS_Rad.v5.0.23.0.G15246021652.hdf"));

            assertEquals("POLYGON ((129.0250670999712 89.79658975406977, -6.4170300611108315 88.23613967607469, -8.920099154760043 86.32499905024413, -9.893744504161857 84.41191323747495, -10.505485010935299 82.49870429049207, -10.975842076883234 80.58535883463844, -11.377940291650905 78.67196596645067, -11.740975374740096 76.7579444858891, -12.080750819552595 74.84320632611005, -12.405163962874246 72.9280900870151, -12.718673119359968 71.0130353793829, -13.02427652238762 69.09764314472332, -13.074610533265286 68.77849058101255, -4.17068964541869 68.44711693204631, 1.2785654966557936 68.00522531950848, 5.402603891171518 67.53889306433688, 9.013604411603218 67.02699129324324, 12.587203385901809 66.41330926760962, 16.607890159954643 65.57760911279104, 21.850407220205888 64.21820075117444, 24.809181150357116 63.29386187193271, 27.651190877552047 64.80095475900657, 30.823735270859643 66.24658402113647, 34.37287139761094 67.61918933634195, 38.34567255972898 68.90462336877947, 42.79684164519756 70.08122973044242, 47.73275674707719 71.14340662799174, 53.20262229851623 72.05601617696243, 59.18281156604972 72.80034444822064, 65.61766811537095 73.3539676636789, 72.40057120621034 73.69182913234798, 79.3675206918862 73.81209529369254, 80.53453700811556 73.81603312585354, 80.71104039446459 77.08294691030505, 80.84489337073471 79.15438865050432, 80.9699719499203 80.78247578538293, 81.11962740918655 82.26953345570404, 81.3576435143464 83.81490908412849, 81.90290725421067 85.65352859896593, 84.86982219864322 88.25602001883792, 129.0250670999712 89.79658975406977))",
                    observation.getGeoBounds().toString());

            // @todo 1 tb/** this is not correct, check why and correct 2015-12-22
            assertEquals(NodeType.UNDEFINED, observation.getNodeType());
        } finally {
            storage.close();
        }
    }

    private BasicDataSource getDatasource() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
        dataSource.setUsername("");
        dataSource.setPassword("sa");
        return dataSource;
    }

    private void writeDatabaseProperties() throws IOException {
        final Properties properties = new Properties();
        final BasicDataSource datasource = getDatasource();
        properties.setProperty("driverClassName", datasource.getDriverClassName());
        properties.setProperty("url", datasource.getUrl());
        properties.setProperty("username", datasource.getUsername());
        properties.setProperty("password", datasource.getPassword());

        storePropertiesToTemp(properties, "database.properties");
    }

    private void writeSystemProperties() throws IOException {
        final Properties properties = new Properties();
        properties.setProperty("archive-root", TestUtil.getTestDataDirectory().getAbsolutePath());

        storePropertiesToTemp(properties, "system.properties");
    }

    private void storePropertiesToTemp(Properties properties, String child) throws IOException {
        final File dataSourcePropertiesFile = new File(configDir, child);
        if (!dataSourcePropertiesFile.createNewFile()) {
            fail("Unable to create test file: " + dataSourcePropertiesFile.getAbsolutePath());
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(dataSourcePropertiesFile);
            properties.store(outputStream, "");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}