package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.*;

class VariableUtils {

    static int TIME_FILL = NetCDFUtils.getDefaultFillValue(DataType.INT, false).intValue();

    // package access for testing purpose only tb 2020-12-02
    static void addAttributes(TemplateVariable template, Variable variable) {
        variable.addAttribute(new Attribute("units", template.getUnits()));
        variable.addAttribute(new Attribute("long_name", template.getLongName()));
        final String standardName = template.getStandardName();
        if (StringUtils.isNotNullAndNotEmpty(standardName)) {
            variable.addAttribute(new Attribute("standard_name", standardName));
        }
        variable.addAttribute(new Attribute("_FillValue", template.getFillValue()));
        variable.addAttribute(new Attribute("missing_value", template.getFillValue()));
    }

    static Array readTimeArray(String timeVariableName, NetcdfFile reader) throws IOException, InvalidRangeException {
        final Variable timeVariable = NetCDFUtils.getVariable(reader, timeVariableName, true);
        final Number fillValue = NetCDFUtils.getFillValue(timeVariable);

        Array timeArray;
        final int rank = timeVariable.getRank();

        // @todo 2 tb/tb this block might be of general interest, extract and test 2020-11-17
        if (rank == 1) {
            timeArray = timeVariable.read();
        } else if (rank == 2) {
            final int[] shape = timeVariable.getShape();
            final int shapeOffset = shape[1] / 2;
            final int[] offset = {0, shapeOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1});
            timeArray = timeArray.reduce(1);    // ensure we have a vector
        } else if (rank == 3) {
            final int[] shape = timeVariable.getShape();
            final int yOffset = shape[1] / 2;
            final int xOffset = shape[2] / 2;
            final int[] offset = {0, yOffset, xOffset};
            timeArray = timeVariable.read(offset, new int[]{shape[0], 1, 1});
            timeArray = timeArray.reduce(1).reduce(1); // ensure we have a vector.
            // Note: the second call of reduce operates already on a 2D dataset, hence we must reduce dim(1) again
        } else {
            throw new IllegalArgumentException("Rank of time-variable not supported");
        }

        // ensure that we have the internal time fill value so we do not need to distinguish later
        final IndexIterator indexIterator = timeArray.getIndexIterator();
        while (indexIterator.hasNext()) {
            final double time = indexIterator.getDoubleNext();
            if (Math.abs(time - fillValue.doubleValue()) < 1e-8) {
                indexIterator.setIntCurrent(TIME_FILL);
            }
        }

        return timeArray;
    }

    static Array convertToEra5TimeStamp(Array timeArray) {
        final Array era5TimeArray = Array.factory(timeArray.getDataType(), timeArray.getShape());
        final IndexIterator era5Iterator = era5TimeArray.getIndexIterator();
        final IndexIterator indexIterator = timeArray.getIndexIterator();
        while (indexIterator.hasNext() && era5Iterator.hasNext()) {
            final int satelliteTime = indexIterator.getIntNext();
            if (isTimeFill(satelliteTime)) {
                era5Iterator.setIntNext(TIME_FILL);
            } else {
                final int era5Time = toEra5TimeStamp(satelliteTime);
                era5Iterator.setIntNext(era5Time);
            }
        }
        return era5TimeArray;
    }

    static boolean isTimeFill(int timeValue) {
        return timeValue == TIME_FILL;
    }

    static int toEra5TimeStamp(int utc1970Seconds) {
        final Calendar utcCalendar = TimeUtils.getUTCCalendar();
        utcCalendar.setTime(new Date(utc1970Seconds * 1000L));

        final int minutes = utcCalendar.get(Calendar.MINUTE);
        if (minutes >= 30) {
            utcCalendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        utcCalendar.set(Calendar.MINUTE, 0);
        utcCalendar.set(Calendar.SECOND, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);

        return (int) (utcCalendar.getTimeInMillis() / 1000L);
    }

    static int[] getNwpShape(com.bc.fiduceo.core.Dimension dimension, int[] shape) {
        int xExtract = dimension.getNx();
        int yExtract = dimension.getNy();
        if (yExtract >= shape[1]) {
            yExtract = shape[1];
        }
        if (xExtract >= shape[2]) {
            xExtract = shape[2];
        }
        return new int[]{shape[0], yExtract, xExtract};
    }

    static int[] getNwpOffset(int[] shape, int[] nwpShape) {
        final int yOffset = shape[1] / 2 - nwpShape[1] / 2;
        final int xOffset = shape[2] / 2 - nwpShape[2] / 2;
        return new int[]{0, yOffset, xOffset};
    }

    static Array readGeolocationVariable(com.bc.fiduceo.core.Dimension dimension, NetcdfFile reader, String lonVarName) throws IOException, InvalidRangeException {
        final Variable geoVariable = NetCDFUtils.getVariable(reader, lonVarName, true);

        final int[] shape = geoVariable.getShape();

        final int[] nwpShape = getNwpShape(dimension, shape);
        final int[] offset = getNwpOffset(shape, nwpShape);

        Array rawData = geoVariable.read(offset, nwpShape);

        final double scaleFactor = NetCDFUtils.getScaleFactor(geoVariable);
        final double addOffset = NetCDFUtils.getOffset(geoVariable);
        if (ReaderUtils.mustScale(scaleFactor, addOffset)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, addOffset);
            rawData = MAMath.convert2Unpacked(rawData, scaleOffset);
        }
        return rawData;
    }

    static HashMap<String, Array> allocateTargetData(NetcdfFileWriter writer, Map<String, TemplateVariable> variables) {
        final HashMap<String, Array> targetArrays = new HashMap<>();
        final Set<Map.Entry<String, TemplateVariable>> entries = variables.entrySet();
        for (final Map.Entry<String, TemplateVariable> entry : entries) {
            final TemplateVariable templateVariable = entry.getValue();
            final String name = templateVariable.getName();
            final String escapedName = NetCDFUtils.escapeVariableName(name);
            final Variable variable = writer.findVariable(escapedName);
            final Array targetArray = Array.factory(DataType.FLOAT, variable.getShape());
            targetArrays.put(entry.getKey(), targetArray);
        }

        return targetArrays;
    }
}
