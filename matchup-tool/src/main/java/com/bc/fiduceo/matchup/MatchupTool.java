/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.matchup;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.SystemConfig;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.ValidationResult;
import com.bc.fiduceo.db.DatabaseConfig;
import com.bc.fiduceo.db.QueryParameter;
import com.bc.fiduceo.db.Storage;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.matchup.screening.DistanceScreening;
import com.bc.fiduceo.matchup.screening.Screening;
import com.bc.fiduceo.matchup.screening.TimeScreening;
import com.bc.fiduceo.matchup.writer.MmdWriter;
import com.bc.fiduceo.matchup.writer.VariablePrototype;
import com.bc.fiduceo.matchup.writer.VariablesConfiguration;
import com.bc.fiduceo.math.Intersection;
import com.bc.fiduceo.math.IntersectionEngine;
import com.bc.fiduceo.math.TimeInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.util.StopWatch;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class MatchupTool {

    private static String VERSION = "1.0.0";
    private final Logger logger;
    private final ReaderFactory readerFactory;

    MatchupTool() {
        logger = FiduceoLogger.getLogger();
        readerFactory = ReaderFactory.get();
    }

    public void run(CommandLine commandLine) throws IOException, SQLException, InvalidRangeException {
        final ToolContext context = initialize(commandLine);

        runMatchupGeneration(context);
    }

    static PixelLocator getPixelLocator(Reader reader, boolean isSegmented, Polygon polygon) throws IOException {
        final PixelLocator pixelLocator;
        if (isSegmented) {
            pixelLocator = reader.getSubScenePixelLocator(polygon);
        } else {
            pixelLocator = reader.getPixelLocator();
        }
        return pixelLocator;
    }

    static boolean isSegmented(Geometry primaryGeoBounds) {
        return primaryGeoBounds instanceof GeometryCollection && ((GeometryCollection) primaryGeoBounds).getGeometries().length > 1;
    }

    // package access for testing only tb 2016-03-14
    static QueryParameter getSecondarySensorParameter(UseCaseConfig useCaseConfig, Geometry geoBounds, Date searchTimeStart, Date searchTimeEnd) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor secondarySensor = getSecondarySensor(useCaseConfig);
        parameter.setSensorName(secondarySensor.getName());
        parameter.setStartTime(searchTimeStart);
        parameter.setStopTime(searchTimeEnd);
        parameter.setGeometry(geoBounds);
        return parameter;
    }

    static MatchupSet getFirstMatchupSet(MatchupCollection matchupCollection) {
        final List<MatchupSet> sets = matchupCollection.getSets();
        if (sets.size() > 0) {
            return sets.get(0);
        }
        throw new IllegalStateException("Called getFirst() on empty matchupCollection.");
    }

    // package access for testing only tb 2016-03-14
    static Sensor getSecondarySensor(UseCaseConfig useCaseConfig) {
        final List<Sensor> additionalSensors = useCaseConfig.getAdditionalSensors();
        if (additionalSensors.size() != 1) {
            throw new RuntimeException("Unable to run matchup with given sensor number");
        }

        return additionalSensors.get(0);
    }

    // package access for testing only tb 2016-02-23
    static QueryParameter getPrimarySensorParameter(ToolContext context) {
        final QueryParameter parameter = new QueryParameter();
        final Sensor primarySensor = context.getUseCaseConfig().getPrimarySensor();
        if (primarySensor == null) {
            throw new RuntimeException("primary sensor not present in configuration file");
        }
        parameter.setSensorName(primarySensor.getName());
        parameter.setStartTime(context.getStartDate());
        parameter.setStopTime(context.getEndDate());
        return parameter;
    }

    // package access for testing only tb 2016-02-18
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option configOption = new Option("c", "config", true, "Defines the configuration directory. Defaults to './config'.");
        options.addOption(configOption);

        final Option startOption = new Option("s", "start", true, "Defines the processing start-date, format 'yyyy-DDD'");
        options.addOption(startOption);

        final Option endOption = new Option("e", "end", true, "Defines the processing end-date, format 'yyyy-DDD'");
        options.addOption(endOption);

        final Option useCaseOption = new Option("u", "usecase", true, "Defines the path to the use-case configuration file. Path is relative to the configuration directory.");
        options.addOption(useCaseOption);

        return options;
    }

    // package access for testing only tb 2016-02-23
    static Date getEndDate(CommandLine commandLine) {
        final String endDateString = commandLine.getOptionValue("end");
        if (StringUtils.isNullOrEmpty(endDateString)) {
            throw new RuntimeException("cmd-line parameter `end` missing");
        }
        return TimeUtils.parseDOYEndOfDay(endDateString);
    }

    // package access for testing only tb 2016-02-23
    static Date getStartDate(CommandLine commandLine) {
        final String startDateString = commandLine.getOptionValue("start");
        if (StringUtils.isNullOrEmpty(startDateString)) {
            throw new RuntimeException("cmd-line parameter `start` missing");
        }
        return TimeUtils.parseDOYBeginOfDay(startDateString);
    }

    static VariablesConfiguration createVariablesConfiguration(MatchupCollection matchupCollection, ToolContext context) throws IOException {
        final VariablesConfiguration variablesConfiguration = new VariablesConfiguration();
        extractPrototypes(variablesConfiguration, matchupCollection, context);
        return variablesConfiguration;
    }

    static void extractPrototypes(VariablesConfiguration variablesConfiguration, MatchupCollection matchupCollection, ToolContext context) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final List<Dimension> dimensions = useCaseConfig.getDimensions();
        final Sensor secondarySensor = getSecondarySensor(useCaseConfig);

        final MatchupSet matchupSet = getFirstMatchupSet(matchupCollection);

        variablesConfiguration.extractPrototypes(primarySensor, matchupSet.getPrimaryObservationPath(), dimensions.get(0));
        variablesConfiguration.extractPrototypes(secondarySensor, matchupSet.getSecondaryObservationPath(), dimensions.get(1));
    }

    // package access for testing only tb 2016-02-18
    void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("matchup-tool version " + VERSION);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "matchup-tool <options>", "Valid options are:", getOptions(), 3, 3, "");

        writer.flush();
    }

    private ToolContext initialize(CommandLine commandLine) throws IOException, SQLException {
        logger.info("Loading configuration ...");
        final ToolContext context = new ToolContext();

        final String configValue = commandLine.getOptionValue("config");
        final File configDirectory = new File(configValue);

        final DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.loadFrom(configDirectory);

        final SystemConfig systemConfig = new SystemConfig();
        systemConfig.loadFrom(configDirectory);
        context.setSystemConfig(systemConfig);

        context.setStartDate(getStartDate(commandLine));
        context.setEndDate(getEndDate(commandLine));

        final UseCaseConfig useCaseConfig = loadUseCaseConfig(commandLine, configDirectory);
        final ValidationResult validationResult = useCaseConfig.checkValid();
        if (!validationResult.isValid()) {
            // @todo 3 tb/tb clean up this mess and write test 2016-03-17
            final StringBuilder builder = new StringBuilder();
            final List<String> messages = validationResult.getMessages();
            for (final String message : messages) {
                builder.append(message);
                builder.append("\n");
            }
            throw new IllegalArgumentException("Use case configuration errors: " + builder.toString());
        }
        context.setUseCaseConfig(useCaseConfig);

        final GeometryFactory geometryFactory = new GeometryFactory(systemConfig.getGeometryLibraryType());
        context.setGeometryFactory(geometryFactory);
        final Storage storage = Storage.create(databaseConfig.getDataSource(), geometryFactory);
        context.setStorage(storage);

        logger.info("Sucess loading configuration.");
        return context;
    }

    private void runMatchupGeneration(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        MatchupCollection matchupCollection = createMatchupCollection(context);

        //
        // - detect all pixels (x/y) in primary observation that are contained in intersecting area
        // -- for each pixel:
        // --- perform check on pixel spatial delta -> remove pixels that are further away
        // --- perform check for observation angles (optional) -> remove pixels where constraint is not fulfilled
        // --- perform cloud processing (optional) -> remove pixels or add flags
        //

        System.out.println("rawCount = " + matchupCollection.getNumMatchups());

        Screening screening = createTimeScreening(context);
        matchupCollection = screening.screen(matchupCollection);

        System.out.println("after TimeScreening = " + matchupCollection.getNumMatchups());

        screening = createDistanceScreening(context);
        matchupCollection = screening.screen(matchupCollection);

        System.out.println("after DistanceScreening = " + matchupCollection.getNumMatchups());

        writeMMD(matchupCollection, context);
    }

    private DistanceScreening createDistanceScreening(ToolContext context) {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final float maxPixelDistance = useCaseConfig.getMaxPixelDistanceKm();
        return new DistanceScreening(maxPixelDistance);
    }

    private TimeScreening createTimeScreening(ToolContext context) {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final int timeDelta = useCaseConfig.getTimeDeltaSeconds();
        final int timeDeltaInMillis = timeDelta * 1000;
        return new TimeScreening(timeDeltaInMillis);
    }

    private void writeMMD(MatchupCollection matchupCollection, ToolContext context) throws IOException, InvalidRangeException {
        if (matchupCollection.getNumMatchups() == 0) {
            logger.warning("No matchups in time interval, creation of MMD file skipped.");
            return;
        }

        final VariablesConfiguration variablesConfiguration = createVariablesConfiguration(matchupCollection, context);
        final int cacheSize = 2048;
        final MmdWriter mmdWriter = createMmdWriter(matchupCollection, context, variablesConfiguration, cacheSize);

        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final Sensor primarySensor = useCaseConfig.getPrimarySensor();
        final Sensor secondarySensor = getSecondarySensor(useCaseConfig);
        final String primarySensorName = primarySensor.getName();
        final String secondarySensorName = secondarySensor.getName();
        final List<VariablePrototype> primaryVariables = variablesConfiguration.getPrototypesFor(primarySensorName);
        final List<VariablePrototype> secondaryVariables = variablesConfiguration.getPrototypesFor(secondarySensorName);
        final Dimension primaryDimension = useCaseConfig.getDimensionFor(primarySensorName);
        final Dimension secondaryDimension = useCaseConfig.getDimensionFor(secondarySensorName);
        final Interval primaryInterval = new Interval(primaryDimension.getNx(), primaryDimension.getNy());
        final Interval secondaryInterval = new Interval(secondaryDimension.getNx(), secondaryDimension.getNy());

        final ReaderFactory readerFactory = ReaderFactory.get();

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<MatchupSet> sets = matchupCollection.getSets();
        int zIndex = 0;
        for (MatchupSet set : sets) {
            final Path primaryObservationPath = set.getPrimaryObservationPath();
            final Path secondaryObservationPath = set.getSecondaryObservationPath();
            try (final Reader primaryReader = readerFactory.getReader(primarySensorName);
                 final Reader secondaryReader = readerFactory.getReader(secondarySensorName)) {
                primaryReader.open(primaryObservationPath.toFile());
                secondaryReader.open(secondaryObservationPath.toFile());
                final List<SampleSet> sampleSets = set.getSampleSets();
                for (SampleSet sampleSet : sampleSets) {
                    writeMmdValues(primarySensorName, primaryObservationPath, sampleSet.getPrimary(), zIndex, primaryVariables, primaryInterval, mmdWriter, primaryReader);
                    writeMmdValues(secondarySensorName, secondaryObservationPath, sampleSet.getSecondary(), zIndex, secondaryVariables, secondaryInterval, mmdWriter, secondaryReader);
                    zIndex++;
                    if (zIndex > 0 && zIndex % cacheSize == 0) {
                        mmdWriter.flush();
                    }
                }
            }
        }

        stopWatch.stop();
        System.out.println("stopWatch.getTimeDiffString() = " + stopWatch.getTimeDiffString());

        mmdWriter.close();
    }

    private void writeMmdValues(String sensorName, Path observationPath, Sample sample, int zIndex, List<VariablePrototype> variables, Interval interval, MmdWriter mmdWriter, Reader reader) throws IOException, InvalidRangeException {
        final int x = sample.x;
        final int y = sample.y;
        writeMmdValues(x, y, zIndex, variables, interval, mmdWriter, reader);
        mmdWriter.write(x, sensorName + "_x", zIndex);
        mmdWriter.write(y, sensorName + "_y", zIndex);
        mmdWriter.write(observationPath.getFileName().toString(), sensorName + "_file_name", zIndex);
        mmdWriter.write(reader.readAcquisitionTime(x, y, interval), sensorName + "_acquisition_time", zIndex);
    }

    private void writeMmdValues(int x, int y, int zIndex, List<VariablePrototype> variables, Interval interval, MmdWriter mmdWriter, Reader reader) throws IOException, InvalidRangeException {
        for (VariablePrototype variable : variables) {
            final String sourceVariableName = variable.getSourceVariableName();
            final String targetVariableName = variable.getTargetVariableName();
            final Array window = reader.readRaw(x, y, interval, sourceVariableName);
            mmdWriter.write(window, targetVariableName, zIndex);
        }
    }

    private MmdWriter createMmdWriter(MatchupCollection matchupCollection, ToolContext context, VariablesConfiguration variablesConfiguration, final int cacheSize) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final File file = createMmdFile(context);
        final MmdWriter mmdWriter = new MmdWriter(cacheSize);
        mmdWriter.create(file,
                         useCaseConfig,
                         variablesConfiguration.get(),
                         matchupCollection.getNumMatchups());
        return mmdWriter;
    }

    private File createMmdFile(ToolContext context) throws IOException {
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final String mmdFileName = MmdWriter.createMMDFileName(useCaseConfig, context.getStartDate(), context.getEndDate());
        final Path mmdFile = Paths.get(useCaseConfig.getOutputPath(), mmdFileName);
        final File file = mmdFile.toFile();
        final File targetDir = file.getParentFile();
        if (!targetDir.isDirectory()) {
            if (!targetDir.mkdirs()) {
                throw new IOException("unable to create mmd output directory '" + targetDir.getAbsolutePath() + "'");
            }
        }

        // @todo 3 tb/tb we might set an overwrite property to the system config later, if requested 2016-03-16
        if (!file.createNewFile()) {
            throw new IOException("unable to create mmd output file '" + file.getAbsolutePath() + "'");
        }
        return file;
    }

    private MatchupCollection createMatchupCollection(ToolContext context) throws IOException, SQLException {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();
        final int timeDelta = useCaseConfig.getTimeDeltaSeconds();
        final int timeDeltaInMillis = timeDelta * 1000;

        QueryParameter parameter = getPrimarySensorParameter(context);
        final Storage storage = context.getStorage();
        final List<SatelliteObservation> primaryObservations = storage.get(parameter);

        for (final SatelliteObservation primaryObservation : primaryObservations) {
            final Reader primaryReader = readerFactory.getReader(primaryObservation.getSensor().getName());
            primaryReader.open(primaryObservation.getDataFilePath().toFile());

            final Date searchTimeStart = TimeUtils.addSeconds(-timeDelta, primaryObservation.getStartTime());
            final Date searchTimeEnd = TimeUtils.addSeconds(timeDelta, primaryObservation.getStopTime());

            final Geometry primaryGeoBounds = primaryObservation.getGeoBounds();
            final boolean isPrimarySegmented = isSegmented(primaryGeoBounds);

            parameter = getSecondarySensorParameter(useCaseConfig, primaryGeoBounds, searchTimeStart, searchTimeEnd);
            final List<SatelliteObservation> secondaryObservations = storage.get(parameter);

            for (final SatelliteObservation secondaryObservation : secondaryObservations) {
                final Reader secondaryReader = readerFactory.getReader(secondaryObservation.getSensor().getName());
                secondaryReader.open(secondaryObservation.getDataFilePath().toFile());

                final Geometry secondaryGeoBounds = secondaryObservation.getGeoBounds();
                final boolean isSecondarySegmented = isSegmented(secondaryGeoBounds);

                final Intersection[] intersectingIntervals = IntersectionEngine.getIntersectingIntervals(primaryObservation, secondaryObservation);
                if (intersectingIntervals.length == 0) {
                    continue;
                }

                final MatchupSet matchupSet = new MatchupSet();
                matchupSet.setPrimaryObservationPath(primaryObservation.getDataFilePath());
                matchupSet.setSecondaryObservationPath(secondaryObservation.getDataFilePath());

                for (final Intersection intersection : intersectingIntervals) {
                    final TimeInfo timeInfo = intersection.getTimeInfo();
                    if (timeInfo.getMinimalTimeDelta() < timeDeltaInMillis) {
                        final PixelLocator primaryPixelLocator = getPixelLocator(primaryReader, isPrimarySegmented, (Polygon) intersection.getPrimaryGeometry());

                        SampleCollector sampleCollector = new SampleCollector(context, primaryPixelLocator);
                        sampleCollector.addPrimarySamples((Polygon) intersection.getGeometry(), matchupSet, primaryReader.getTimeLocator());

                        final PixelLocator secondaryPixelLocator = getPixelLocator(secondaryReader, isSecondarySegmented, (Polygon) intersection.getSecondaryGeometry());

                        sampleCollector = new SampleCollector(context, secondaryPixelLocator);
                        sampleCollector.addSecondarySamples(matchupSet.getSampleSets(), secondaryReader.getTimeLocator());
                    }
                }

                if (matchupSet.getNumObservations() > 0) {
                    matchupCollection.add(matchupSet);
                }
            }
        }

        return matchupCollection;
    }

    private UseCaseConfig loadUseCaseConfig(CommandLine commandLine, File configDirectory) throws IOException {
        final String usecaseConfigFileName = commandLine.getOptionValue("usecase");
        if (StringUtils.isNullOrEmpty(usecaseConfigFileName)) {
            throw new RuntimeException("Use case configuration file not supplied");
        }

        final File useCaseConfigFile = new File(configDirectory, usecaseConfigFileName);
        if (!useCaseConfigFile.isFile()) {
            throw new RuntimeException("Use case config file does not exist: '" + usecaseConfigFileName + "'");
        }

        final UseCaseConfig useCaseConfig;
        try (FileInputStream inputStream = new FileInputStream(useCaseConfigFile)) {
            useCaseConfig = UseCaseConfig.load(inputStream);
        }

        return useCaseConfig;
    }
}
