package com.bc.fiduceo.qc;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bc.fiduceo.FiduceoConstants.VERSION_NUMBER;
import static com.bc.fiduceo.util.MMDUtil.getMMDFileNamePattern;

class MmdQCTool {

    private final static Logger logger = FiduceoLogger.getLogger();

    // package access for testing only tb 2023-02-14
    static Options getOptions() {
        final Options options = new Options();

        final Option helpOption = new Option("h", "help", false, "Prints the tool usage.");
        options.addOption(helpOption);

        final Option inputOption = new Option("i", "input", true, "Defines the MMD input directory.");
        inputOption.setRequired(true);
        options.addOption(inputOption);

        final Option outputOption = new Option("o", "outdir", true, "Defines the result output directory.");
        options.addOption(outputOption);

        final Option timeOption = new Option("t", "time", true, "Defines matchup time variable name.");
        timeOption.setRequired(true);
        options.addOption(timeOption);

        final Option plotOption = new Option("p", "plot", false, "Enables plotting the matchup locations onto a global map. Requires 'lon' and 'lat' to be set.");
        options.addOption(plotOption);

        final Option lonOption = new Option("lon", "longitude", true, "Defines the variable name for the longitude.");
        options.addOption(lonOption);

        final Option latOption = new Option("lat", "latitude", true, "Defines the variable name for the latitude.");
        options.addOption(latOption);

        return options;
    }

    // package access for testing only tb 2023-02-14
    static void writeReport(OutputStream outputStream, MatchupAccumulator accumulator, FileMessages fileMessages) {
        final PrintWriter writer = new PrintWriter(outputStream);

        final int fileCount = accumulator.getFileCount();
        writer.println("Analysed " + fileCount + " file(s)");
        if (fileCount == 0) {
            writer.flush();
            return;
        }

        writer.println();
        final HashMap<String, List<String>> messageMap = fileMessages.getMessageMap();
        final int size = messageMap.size();
        writer.println(size + " file(s) with errors");
        if (size > 0) {
            for (Map.Entry<String, List<String>> entry : messageMap.entrySet()) {
                writer.println(entry.getKey());

                final List<String> messages = entry.getValue();
                for (final String message : messages) {
                    writer.println(" - " + message);
                }
            }
        }

        writer.println();
        writer.println("Total number of matchups: " + accumulator.getSummaryCount());
        writer.println("Daily distribution:");

        final TreeMap<String, Integer> treeMap = new TreeMap<>(accumulator.getDaysMap());
        final Set<Map.Entry<String, Integer>> entries = treeMap.entrySet();
        for (final Map.Entry<String, Integer> entry : entries) {
            writer.println(entry.getKey() + ": " + entry.getValue());
        }

        writer.flush();
    }

    void run(CommandLine commandLine) throws IOException {
        final String inputDirOption = commandLine.getOptionValue("i");
        final List<Path> mmdFiles = getInputFiles(inputDirOption);
        logger.info("Found " + mmdFiles.size() + " input file(s) to analyze.");

        final String outDirString = commandLine.getOptionValue("o");
        File outDir;
        if (StringUtils.isNullOrEmpty(outDirString)) {
            outDir = new File(".");
        } else {
            outDir = new File(outDirString);
        }

        final FileMessages fileMessages = new FileMessages();
        final MatchupAccumulator matchupAccumulator = new MatchupAccumulator();

        // loop over files
        for (final Path mmdFile : mmdFiles) {
            try (final NetcdfFile netcdfFile = NetCDFUtils.openReadOnly(mmdFile.toAbsolutePath().toString())) {
                matchupAccumulator.countFile();

                // read time variable center pixel
                final String timeVariableName = commandLine.getOptionValue("t");
                final Array timeArray = NetCDFUtils.getCenterPosArrayFromMMDFile(netcdfFile, timeVariableName, null,
                        null, FiduceoConstants.MATCHUP_COUNT);
                final IndexIterator iterator = timeArray.getIndexIterator();
                while (iterator.hasNext()) {
                    final int time = iterator.getIntNext();
                    matchupAccumulator.add(time);
                }

                if (commandLine.hasOption("p")) {
                    plotMatchups(mmdFile, netcdfFile, commandLine, outDir);
                }

            } catch (IOException | InvalidRangeException ioException) {
                fileMessages.add(mmdFile.getFileName().toString(), ioException.getMessage());
            }
        }

        final Date now = TimeUtils.createNow();
        final String timeString = TimeUtils.format(now, "yyyy-MM-dd");
        final String qcFileName = "mmd_qc_report_" + timeString + ".txt";
        final File reportFile = new File(outDir, qcFileName);
        if (!reportFile.createNewFile()) {
            throw new IOException("unable to create report file: " + reportFile.getAbsolutePath());
        }
        try (FileOutputStream outStream = new FileOutputStream(reportFile)) {
            writeReport(outStream, matchupAccumulator, fileMessages);
        }
    }

    private static void plotMatchups(Path mmdFile, NetcdfFile netcdfFile, CommandLine commandLine, File outDir) throws IOException, InvalidRangeException {
        final String lonVariable = commandLine.getOptionValue("lon");
        final String latVariable = commandLine.getOptionValue("lat");

        if (StringUtils.isNullOrEmpty(lonVariable) || StringUtils.isNullOrEmpty(latVariable)) {
            throw new IllegalArgumentException("must provide lon and lat variable names for plotting");
        }

        final String filenameWithoutExtension = FileUtils.getFilenameWithoutExtension(mmdFile.toFile());
        final String pngFile = filenameWithoutExtension.concat(".png");
        final File pngFilePath = new File(outDir, pngFile);

        final GlobalPlot filePlot = GlobalPlot.create();
        final Array latitudes = NetCDFUtils.getCenterPosArrayFromMMDFile(netcdfFile, latVariable, null,
                null, FiduceoConstants.MATCHUP_COUNT);

        final Array longitudes = NetCDFUtils.getCenterPosArrayFromMMDFile(netcdfFile, lonVariable, null,
                null, FiduceoConstants.MATCHUP_COUNT);

        int numMatches = 1;
        int[] shape = latitudes.getShape();
        if (shape.length > 0) {
           numMatches = shape[0];
        }

        final ArrayList<GeoPos> pointList = new ArrayList<>();
        for (int i = 0; i < numMatches; i++) {
            final GeoPos geoPos = new GeoPos(latitudes.getFloat(i), longitudes.getFloat(i));
            pointList.add(geoPos);
        }

        filePlot.plot(pointList);
        filePlot.writeTo(pngFilePath.getAbsolutePath());
        filePlot.dispose();
    }

    private List<Path> getInputFiles(String inputDirOption) throws IOException {
        final File inputDir = new File(inputDirOption);
        if (!inputDir.isDirectory()) {
            throw new IOException("Not a valid directory: " + inputDir.getAbsolutePath());
        }

        final Pattern pattern = getMMDFileNamePattern();
        try (Stream<Path> pathStream = Files.walk(inputDir.toPath())) {
            final Stream<Path> regularFiles = pathStream.filter(Files::isRegularFile);
            final Stream<Path> mmdFileStream = regularFiles.filter(path -> pattern.matcher(path.getFileName().toString()).matches());
            return mmdFileStream.collect(Collectors.toList());
        }
    }

    static void printUsageTo(OutputStream outputStream) {
        final String ls = System.lineSeparator();
        final PrintWriter writer = new PrintWriter(outputStream);
        writer.write("mmd-qc-tool version " + VERSION_NUMBER);
        writer.write(ls + ls);

        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 120, "mmd-qc-tool <options>", "Valid options are:",
                getOptions(), 3, 3, "");

        writer.flush();
    }
}
