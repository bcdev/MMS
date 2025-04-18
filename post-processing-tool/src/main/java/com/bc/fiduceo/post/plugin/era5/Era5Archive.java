package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.util.TimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

class Era5Archive {

    private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");

    private final Era5Collection collection;
    private final Configuration config;

    Era5Archive(Configuration config, Era5Collection collection) {
        this.config = config;
        this.collection = collection;
    }

    String getFileName(String collection, String variable, String timeString) {
        return getFileNameBegin() + collection + "_" + timeString + "." + variable + ".nc";
    }

    private String getFileNameBegin() {
        if (collection == Era5Collection.ERA_5) {
            return "ecmwf-era5_oper_";
        } else if(collection == Era5Collection.ERA_5T) {
            return "ecmwf-era5t_oper_";
        } else if (collection == Era5Collection.ERA_51) {
            return "ecmwf-era51_oper_";
        }

        throw new IllegalStateException("Unsupported RA5 collection");
    }

    static String mapVariable(String variable) {
        switch (variable) {
            case "t2m":
                return "2t";
            case "u10":
                return "10u";
            case "v10":
                return "10v";
            case "siconc":
                return "ci";
            default:
                return variable;
        }
    }

    static String getTimeString(String collection, Calendar utcCalendar) {
        int hour = utcCalendar.get(Calendar.HOUR_OF_DAY);
        final int year = utcCalendar.get(Calendar.YEAR);

        final int month = utcCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        if (collection.startsWith("an_")) {
            final String hourString = twoDigitsFormat.format(hour);
            return year + monthString + dayString + hourString + "00";
        } else if (collection.startsWith("fc_")) {
            int forecastTimeStep;
            if (hour <= 6) {
                forecastTimeStep = 6 + hour;
                hour = 18;
            } else if (hour <= 18) {
                forecastTimeStep = hour - 6;
                hour = 6;
            } else {
                forecastTimeStep = hour - 18;
                hour = 18;
            }

            final String hourString = twoDigitsFormat.format(hour);
            return year + monthString + dayString + hourString + "00" + forecastTimeStep;
        } else {
            throw new IllegalArgumentException("Unknown era5 collection: " + collection);
        }
    }

    String get(String variableType, int timeStamp) {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTimeInMillis(timeStamp * 1000L);

        final int cutPoint = StringUtils.ordinalIndexOf(variableType, "_", 2);
        final String collection = variableType.substring(0, cutPoint);

        String variable = variableType.substring(cutPoint + 1);
        if (config.isTranslateVariableNameToFileAccessName()) {
            variable = mapVariable(variable);
        }

        adjustCalendarForForecast(utcCalendar, collection);

        final String timeString = getTimeString(collection, utcCalendar);
        final String fileName = getFileName(collection, variable, timeString);

        final int year = utcCalendar.get(Calendar.YEAR);

        final int month = utcCalendar.get(Calendar.MONTH) + 1;
        final String monthString = twoDigitsFormat.format(month);

        final int day = utcCalendar.get(Calendar.DAY_OF_MONTH);
        final String dayString = twoDigitsFormat.format(day);

        final String rootPath = config.getNWPAuxDir();
        return rootPath + File.separator
               + collection + File.separator
               + year + File.separator
               + monthString + File.separator
               + dayString + File.separator
               + fileName;
    }

    // @todo 1 tb/tb make static and add test 2020-12-11
    private void adjustCalendarForForecast(Calendar utcCalendar, String collection) {
        int hour = utcCalendar.get(Calendar.HOUR_OF_DAY);
        if (hour <= 6 && collection.startsWith("fc_")) {
            utcCalendar.add(Calendar.DATE, -1);
        }
    }
}
