package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.DataType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class GenericCsvHelper {

    public static final String[] SUPPORTED_KEYS_NDBC_SM = {"ndbc-sm-ob", "ndbc-sm-cb", "ndbc-sm-lb", "ndbc-sm-os", "ndbc-sm-cs", "ndbc-sm-ls"};
    public static final String[] SUPPORTED_KEYS_NDBC_CW = {"ndbc-cw-ob", "ndbc-cw-cb", "ndbc-cw-lb", "ndbc-cw-os", "ndbc-cw-cs", "ndbc-cw-ls"};
    public static final String[] SUPPORTED_KEYS_GBOV = {"gbov"};

    public static final String RESOURCE_KEY_NDBC_SM = "NDBC_SM";
    public static final String RESOURCE_KEY_NDBC_CW = "NDBC_CW";
    public static final String RESOURCE_KEY_GBOV = "GBOV";


    public static String getResourceKeyFromPath(String filePath) {
        String lowerPath = filePath.toLowerCase();

        if (Arrays.stream(SUPPORTED_KEYS_NDBC_SM).anyMatch(lowerPath::contains)) {
            return RESOURCE_KEY_NDBC_SM;
        }
        if (Arrays.stream(SUPPORTED_KEYS_NDBC_CW).anyMatch(lowerPath::contains)) {
            return RESOURCE_KEY_NDBC_CW;
        }
        if (Arrays.stream(SUPPORTED_KEYS_GBOV).anyMatch(lowerPath::contains)) {
            return RESOURCE_KEY_GBOV;
        }

        throw new IllegalArgumentException("Unsupported format for file: " + filePath);
    }

    public static int[] extractYearMonthDayFromFilename(String filename, String resourceKey) {
        int[] ymd = new int[3];
        if (resourceKey.equals(GenericCsvHelper.RESOURCE_KEY_NDBC_SM) || resourceKey.equals(GenericCsvHelper.RESOURCE_KEY_NDBC_CW)) {
            final int dotIndex = filename.indexOf('.');
            final String yearString = filename.substring(dotIndex - 4, dotIndex);
            ymd[0] = Integer.parseInt(yearString);
            ymd[1] = 1;
            ymd[2] = 1;

            return ymd;
        } else if (resourceKey.equals(GenericCsvHelper.RESOURCE_KEY_GBOV)) {
            final String[] fileSplit = filename.split("__");
            final String startDate = fileSplit[3];
            final String yearString = startDate.substring(0, 4);
            final String monthString = startDate.substring(4, 6);
            final String dayString = startDate.substring(6, 8);
            ymd[0] = Integer.parseInt(yearString);
            ymd[1] = Integer.parseInt(monthString);
            ymd[2] = Integer.parseInt(dayString);

            return ymd;
        }
        throw new IllegalStateException("Unsupported format for file: '" + filename + "' and resourceKey '" + resourceKey + "'.");
    }

    public static List<GenericRecord> parseData(File file, CsvFormatConfig config, String resourceKey) throws IOException {
        List<GenericRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.charAt(0) == config.getCommentChar()) {
                    continue;
                }

                String[] tokens = tokenize(line, config.getDelimiter());
                List<GenericVariable> vars = config.getVariables();

                if (tokens.length < vars.size()) continue;

                GenericRecord record = parseLine(tokens, vars, resourceKey, config);
                records.add(record);
            }
        }

        return records;
    }

    private static String[] tokenize(String line, String delimiter) {
        if ("space".equalsIgnoreCase(delimiter)) {
            return line.trim().split("\\s+");
        }
        return line.split(delimiter);
    }

    private static GenericRecord parseLine(String[] tokens, List<GenericVariable> vars, String resourceKey, CsvFormatConfig config) {
        GenericRecord record = new GenericRecord();

        if (resourceKey.equals(GenericCsvHelper.RESOURCE_KEY_NDBC_CW) || resourceKey.equals(GenericCsvHelper.RESOURCE_KEY_NDBC_SM)) {
            GenericVariable yyVar = null, mmVar = null, ddVar = null, hhVar = null, minVar = null;
            int yyIndex = -1, mmIndex = -1, ddIndex = -1, hhIndex = -1, minIndex = -1;

            for (int ii = 0; ii < vars.size(); ii++) {
                String name = vars.get(ii).getName();
                switch (name) {
                    case "YY": yyVar = vars.get(ii); yyIndex = ii; break;
                    case "MM": mmVar = vars.get(ii); mmIndex = ii; break;
                    case "DD": ddVar = vars.get(ii); ddIndex = ii; break;
                    case "hh": hhVar = vars.get(ii); hhIndex = ii; break;
                    case "mm": minVar = vars.get(ii); minIndex = ii; break;
                }
            }

            if (yyIndex >= 0 && mmIndex >= 0 && ddIndex >= 0 && hhIndex >= 0 && minIndex >= 0) {
                int year = ((Number) parseValue(tokens[yyIndex], yyVar.getType())).intValue();
                int month = ((Number) parseValue(tokens[mmIndex], mmVar.getType())).intValue();
                int day = ((Number) parseValue(tokens[ddIndex], ddVar.getType())).intValue();
                int hour = ((Number) parseValue(tokens[hhIndex], hhVar.getType())).intValue();
                int minute = ((Number) parseValue(tokens[minIndex], minVar.getType())).intValue();

                final Calendar calendar = TimeUtils.getUTCCalendar();
                calendar.setTimeInMillis(0);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);  // calendar wants month zero-based
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                int seconds = (int) (calendar.getTimeInMillis() * 0.001);

                record.put(config.getTimeName(), seconds);
            }

            for (int ii = 0; ii < vars.size(); ii++) {
                String name = vars.get(ii).getName();
                if (Arrays.asList("YY", "MM", "DD", "hh", "mm").contains(name)) continue;
                record.put(name, parseValue(tokens[ii], vars.get(ii).getType()));
            }
        } else {
            for (int ii = 0; ii < vars.size(); ii++) {
                GenericVariable var = vars.get(ii);

                if (var.getName().equals(config.getTimeName())) {
                    String value = (String) parseValue(tokens[ii], "string");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(config.getTimeFormat()).withZone(ZoneOffset.UTC);
                    Instant instant = Instant.from(formatter.parse(value));
                    record.put(config.getTimeName(), (int) instant.getEpochSecond());
                    continue;
                }

                record.put(var.getName(), parseValue(tokens[ii], var.getType()));
            }
        }

        return record;
    }

    public static Object parseValue(String token, String type) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        token = token.trim();

        switch (type) {
            case "byte":
                return Byte.parseByte(token);
            case "short":
                return Short.parseShort(token);
            case "int":
                return Integer.parseInt(token);
            case "long":
                return Long.parseLong(token);
            case "float":
                return Float.parseFloat(token);
            case "double":
                return Double.parseDouble(token);
            default:
                return token;
        }
    }

    public static String getPrimaryIdFromFilename(File file, String resourceKey) {
        String filename = file.getName();
        if (resourceKey.equals(RESOURCE_KEY_NDBC_SM) || resourceKey.equals(RESOURCE_KEY_NDBC_CW)) {
            if (filename.length() >= 5) {
                return file.getName().substring(0,5);
            }
        }
        if (resourceKey.equals(RESOURCE_KEY_GBOV)) {
            String[] split = filename.split("__");
            return split[1].replace("--", " ");
        }
        throw new IllegalArgumentException("Unsupported format for file: " + file.getAbsolutePath());
    }

    public static String getSecondaryIdFromFilename(File file, String resourceKey) {
        String filename = file.getName();
        if (resourceKey != null && resourceKey.equals(RESOURCE_KEY_GBOV)) {
            String[] split = filename.split("__");
            return split[2].replace("--", " ");
        }
        return null;
    }

    public static DataType getNcDataType(String type) {
        switch (type) {
            case "byte":
                return DataType.BYTE;
            case "short":
                return DataType.SHORT;
            case "int":
                return DataType.INT;
            case "long":
                return DataType.LONG;
            case "float":
                return DataType.FLOAT;
            case "double":
                return DataType.DOUBLE;
            default:
                return DataType.STRING;
        }
    }

    public static Class getFillValueClass(String type) {
        switch (type) {
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                throw new RuntimeException("not implemented for type " + type);
        }
    }
}
