package com.bc.fiduceo.db;

import com.bc.fiduceo.TestData;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(DbAndIOTestRunner.class)
public class DbMaintenanceToolIntegrationTest {

    private File configDir;
    private GeometryFactory geometryFactory;

    @Before
    public void setUp() {
        final File testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create testGroupInputProduct directory: " + configDir.getAbsolutePath());
        }
        geometryFactory = new GeometryFactory(GeometryFactory.Type.S2);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testInvalidCommandLine() throws ParseException {
        final String ls = System.lineSeparator();
        final PrintStream _err = System.err;
        final PrintStream _out = System.out;

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ByteArrayOutputStream err = new ByteArrayOutputStream();
            final PrintStream psO = new PrintStream(out);
            final PrintStream psE = new PrintStream(err);
            System.setOut(psO);
            System.setErr(psE);

            DbMaintenanceToolMain.main(new String[0]);

            psO.flush();
            psE.flush();

            assertEquals("", out.toString());
            assertEquals("db-maintenance-tool version 1.6.0" + ls +
                    ls +
                    "usage: db_maintenance <options>" + ls +
                    "Valid options are:" + ls +
                    "   -c,--config <arg>     Defines the configuration directory. Defaults to './config'." + ls +
                    "   -d,--dryrun           Defines 'dryrun' status, i.e. just test the replacement and report problems." + ls +
                    "   -h,--help             Prints the tool usage." + ls +
                    "   -p,--path <arg>       Observation path segment to be replaced or truncated." + ls +
                    "   -r,--replace <arg>    Observation path segment replacement." + ls +
                    "   -s,--segments <arg>   Number of segments to consider for paths missing the search expression (default: 4)" + ls +
                    "   -t,--truncate         Command to truncate path segment." + ls, err.toString());
        } finally {
            System.setOut(_out);
            System.setErr(_err);
        }
    }

    @Test
    public void testCorrectPaths_MongoDb_empty_Db() throws IOException, ParseException {
        setUpMongoDb();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                "-p", "/data/archive/wrong", "-r", "/archive/correct"};

        DbMaintenanceToolMain.main(args);
        // a dumb test - just should not throw anything - no testable effects on DB tb 2019-04-03
    }

    @Test
    public void testCorrectPaths_MongoDb_alterNoPath() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_alterNoPath(databaseConfig);
    }

    @Test
    public void testCorrectPaths_Postgres_alterNoPath() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_alterNoPath(databaseConfig);
    }

    @Test
    public void testCorrectPaths_H2_alterNoPath() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpH2Db();

        runTest_alterNoPath(databaseConfig);
    }

    @Test
    public void testCorrectPaths_MongoDb_alterSomePaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_alterSomePaths(databaseConfig);
    }

    @Test
    public void testCorrectPaths_Postgres_alterSomePaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_alterSomePaths(databaseConfig);
    }

    @Test
    public void testTruncatePaths_MongoDb_alterSomePaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_truncatePath(databaseConfig);
    }

    @Test
    public void testTruncatePaths_Postgres_alterSomePaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_truncatePath(databaseConfig);
    }

    @Test
    public void testTruncatePaths_H2_alterSomePaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpH2Db();

        runTest_truncatePath(databaseConfig);
    }

    @Test
    public void testTruncatePaths_MongoDb_innerSegment() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_truncatePath_innerSegment(databaseConfig);
    }

    @Test
    public void testTruncatePaths_Postgres_innerSegment() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_truncatePath_innerSegment(databaseConfig);
    }

    @Test
    public void testTruncatePaths_H2_innerSegment() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpH2Db();

        runTest_truncatePath_innerSegment(databaseConfig);
    }

    @Test
    public void testDryRun_MongoDb_correctPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_dryRun_allOk(databaseConfig);
    }

    @Test
    public void testDryRun_Postgres_correctPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_dryRun_allOk(databaseConfig);
    }

    @Test
    public void testDryRun_H2_correctPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpH2Db();

        runTest_dryRun_allOk(databaseConfig);
    }

    @Test
    public void testDryRun_MongoDb_someIncorrectPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpMongoDb();

        runTest_dryRun_someNotOk(databaseConfig);
    }

    @Test
    public void testDryRun_Postgres_someIncorrectPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpPostgresDb();

        runTest_dryRun_someNotOk(databaseConfig);
    }

    @Test
    public void testDryRun_H2_someIncorrectPaths() throws IOException, ParseException, SQLException {
        final DatabaseConfig databaseConfig = setUpH2Db();

        runTest_dryRun_someNotOk(databaseConfig);
    }

    private void runTest_alterNoPath(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            final String obsPath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct", "the_file_number_" + i}, true);
            observation.setDataFilePath(obsPath);
            storage.insert(observation);
        }

        try {
            final String searchPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "wrong"}, true);
            final String replacePath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct",}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", searchPath,
                    "-r", replacePath};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(12, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertTrue(satelliteObservation.getDataFilePath().toString().contains(replacePath));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void runTest_alterSomePaths(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 16; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            if (i % 2 == 0) {
                observation.setDataFilePath(TestUtil.assembleFileSystemPath(new String[]{"archive", "correct", "the_file_number_" + i}, true));
            } else {
                observation.setDataFilePath(TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "wrong", "the_file_number_" + i}, true));
            }
            storage.insert(observation);
        }

        try {
            final String searchPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "wrong"}, true);
            final String replacePath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct",}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", searchPath,
                    "-r", replacePath};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(16, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertTrue(satelliteObservation.getDataFilePath().toString().contains(replacePath));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void runTest_truncatePath(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            final String obsPath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct", "the_file_number_" + i}, true);
            observation.setDataFilePath(obsPath);
            storage.insert(observation);
        }

        try {
            final String cutPath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct"}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", cutPath,
                    "-t"};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(12, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertFalse(satelliteObservation.getDataFilePath().toString().contains(cutPath));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void runTest_truncatePath_innerSegment(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            final String obsPath = TestUtil.assembleFileSystemPath(new String[]{"archive", "correct", "the_file_number_" + i}, true);
            observation.setDataFilePath(obsPath);
            storage.insert(observation);
        }

        try {
            final String cutPath = TestUtil.assembleFileSystemPath(new String[]{"correct"}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(),
                    "-p", cutPath,
                    "-t"};

            DbMaintenanceToolMain.main(args);

            final List<SatelliteObservation> observations = storage.get();
            assertEquals(12, observations.size());
            for (SatelliteObservation satelliteObservation : observations) {
                assertFalse(satelliteObservation.getDataFilePath().toString().contains(cutPath));
            }
        } finally {
            storage.clear();
            storage.close();
        }
    }

    private void runTest_dryRun_allOk(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final String sep = System.lineSeparator();
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            final String obsPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "correct", "the_file_number_" + i}, true);
            observation.setDataFilePath(obsPath);
            storage.insert(observation);
        }

        final PrintStream _out = System.out;

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PrintStream psO = new PrintStream(out);
            System.setOut(psO);

            final String searchPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "correct"}, true);
            final String replacePath = TestUtil.assembleFileSystemPath(new String[]{"archive", "whatever",}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-d",
                    "-p", searchPath,
                    "-r", replacePath};

            DbMaintenanceToolMain.main(args);

            psO.flush();

            assertEquals("Datasets checked: 12" + sep +
                    "Datasets ok to convert: 12" + sep, out.toString());

        } finally {
            System.setOut(_out);

            storage.clear();
            storage.close();
        }
    }

    private void runTest_dryRun_someNotOk(DatabaseConfig databaseConfig) throws SQLException, ParseException {
        final String sep = System.lineSeparator();
        final Storage storage = initializeStorage(databaseConfig);

        for (int i = 0; i < 12; i++) {
            final SatelliteObservation observation = TestData.createSatelliteObservation(geometryFactory);
            final String obsPath;
            if (i % 3 == 0) {
                obsPath = TestUtil.assembleFileSystemPath(new String[]{"other", "archive", "unexpected", "the_file_number_" + i}, true);
            } else {
                obsPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "correct", "the_file_number_" + i}, true);
            }
            observation.setDataFilePath(obsPath);
            storage.insert(observation);
        }

        final PrintStream _out = System.out;

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PrintStream psO = new PrintStream(out);
            System.setOut(psO);

            final String searchPath = TestUtil.assembleFileSystemPath(new String[]{"data", "archive", "correct"}, true);
            final String replacePath = TestUtil.assembleFileSystemPath(new String[]{"archive", "whatever",}, true);

            final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-d",
                    "-p", searchPath,
                    "-r", replacePath,
                    "-s", "3"}; // for this test we only need 3 path segments tb 2022-07-11

            DbMaintenanceToolMain.main(args);

            psO.flush();

            String expected = TestUtil.assembleFileSystemPath(new String[]{"other", "archive", "unexpected"}, true);
            assertEquals("Datasets checked: 12" + sep +
                    "Datasets ok to convert: 8" + sep +
                    "Datasets with deviating path:" + sep +
                    "- " + expected + ": 4" + sep, out.toString());

        } finally {
            System.setOut(_out);

            storage.clear();
            storage.close();
        }
    }

    private Storage initializeStorage(DatabaseConfig databaseConfig) throws SQLException {
        final Storage storage = Storage.create(databaseConfig, geometryFactory);

        if (!storage.isInitialized()) {
            storage.initialize();
        }

        storage.insert(new Sensor(TestData.SENSOR_NAME));

        return storage;
    }

    private DatabaseConfig setUpMongoDb() throws IOException {
        TestUtil.writeDatabaseProperties_MongoDb(configDir);
        TestUtil.writeSystemConfig(configDir);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDir);
        // final BasicDataSource dataSource_mongoDb = TestUtil.getDataSource_MongoDb();
        //final DatabaseConfig databaseConfig = databaseConfig1;
        //databaseConfig.setDataSource
        return databaseConfig;
    }

    private DatabaseConfig setUpPostgresDb() throws IOException {
        TestUtil.writeDatabaseProperties_Postgres(configDir);
        TestUtil.writeSystemConfig(configDir);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDir);
        return databaseConfig;
        // return TestUtil.getDataSource_Postgres();
    }

    private DatabaseConfig setUpH2Db() throws IOException {
        TestUtil.writeDatabaseProperties_H2(configDir);
        TestUtil.writeSystemConfig(configDir);
        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDir);
        return databaseConfig;
        //return TestUtil.getDatasource_H2();
    }
}
