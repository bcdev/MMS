package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.core.IntRange;
import com.bc.fiduceo.log.FiduceoLogger;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.util.NetCDFUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.bc.fiduceo.post.plugin.era5.Era5PostProcessing.DATA_ARRAY_WIDTH;
import static com.bc.fiduceo.post.plugin.era5.VariableUtils.*;
import static com.bc.fiduceo.post.util.PPUtils.convertToFitTheRangeMinus180to180;

class SatelliteFields extends FieldsProcessor {

    private List<Dimension> dimension2d;
    private List<Dimension> dimension3d;
    private Map<String, TemplateVariable> variables;
    private Era5Collection collection;

    void prepare(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFile reader, NetcdfFileWriter writer, Era5Collection collection) {
        satFieldsConfig.verify();
        setDimensions(satFieldsConfig, writer, reader);

        this.collection = collection;

        variables = getVariables(satFieldsConfig);
        final Collection<TemplateVariable> values = variables.values();
        for (TemplateVariable template : values) {
            final List<Dimension> dimensions = getDimensions(template);

            final Variable variable = writer.addVariable(template.getName(), DataType.FLOAT, dimensions);
            VariableUtils.addAttributes(template, variable);
        }

        addTimeVariable(satFieldsConfig, writer);
    }

    void compute(Configuration config, NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        final SatelliteFieldsConfiguration satFieldsConfig = config.getSatelliteFields();
        final int numLayers = satFieldsConfig.get_z_dim();
        final Era5Archive era5Archive = new Era5Archive(config, collection);
        final VariableCache variableCache = new VariableCache(era5Archive, 52); // 4 * 13 variables tb 2020-11-25

        try {
            // open input time variable
            // + read completely
            // + convert to ERA-5 time stamps
            // + write to MMD
            final Array timeArray = VariableUtils.readTimeArray(satFieldsConfig.get_time_variable_name(), reader);
            final Array era5TimeArray = convertToEra5TimeStamp(timeArray);
            final Variable targetTimeVariable = NetCDFUtils.getVariable(writer, satFieldsConfig.get_nwp_time_variable_name());
            writer.write(targetTimeVariable, era5TimeArray);

            // open longitude and latitude input variables
            // + read completely or specified x/y subset
            // + scale if necessary
            final com.bc.fiduceo.core.Dimension geoDimension = new com.bc.fiduceo.core.Dimension("geoloc", satFieldsConfig.get_x_dim(), satFieldsConfig.get_y_dim());
            final Array lonArray = readGeolocationVariable(geoDimension, reader, satFieldsConfig.get_longitude_variable_name());
            convertToFitTheRangeMinus180to180(lonArray);
            final Array latArray = readGeolocationVariable(geoDimension, reader, satFieldsConfig.get_latitude_variable_name());

            // prepare data
            // + calculate dimensions
            // + allocate target data arrays
            final int numMatches = NetCDFUtils.getDimensionLength(satFieldsConfig.getMatchupDimensionName(), reader);
            final int[] nwpShape = getNwpShape(geoDimension, lonArray.getShape());
            final int[] nwpOffset = getNwpOffset(lonArray.getShape(), nwpShape);
            final int[] nwpStride = {1, 1, 1};
            final HashMap<String, Array> targetArrays = allocateTargetData(writer, variables);

            // iterate over matchups
            //   + convert geo-region to era-5 extract
            //   + prepare interpolation context
            final Index timeIndex = era5TimeArray.getIndex();
            for (int m = 0; m < numMatches; m++) {
                nwpOffset[0] = m;
                nwpShape[0] = 1;

                // get a subset of one matchup layer and convert to 2D dataset
                final Array lonLayer = lonArray.sectionNoReduce(nwpOffset, nwpShape, nwpStride).reduce(0).copy();
                final Array latLayer = latArray.sectionNoReduce(nwpOffset, nwpShape, nwpStride).reduce(0).copy();

                final int[] shape = lonLayer.getShape();
                final int width = shape[1];
                final int height = shape[0];

                final InterpolationContext interpolationContext = Era5PostProcessing.getInterpolationContext(lonLayer, latLayer);

                timeIndex.set(m);
                final int era5Time = era5TimeArray.getInt(timeIndex);
                final boolean isTimeFill = VariableUtils.isTimeFill(era5Time);

                //   iterate over variables
                //     + assemble variable file name
                //     + read variable data extract
                //     + interpolate (2d, 3d per layer)
                //     - store to target raster
                final Set<String> variableKeys = variables.keySet();
                for (final String variableKey : variableKeys) {
                    final float fillValue = variables.get(variableKey).getFillValue();
                    final Variable variable = variableCache.get(variableKey, era5Time);

                    final Array targetArray = targetArrays.get(variableKey);
                    final Index targetIndex = targetArray.getIndex();

                    final int rank = variable.getRank();
                    final Array era5Data = loadEra5Data(variable, interpolationContext, numLayers);
                    final Index era5Index = era5Data.getIndex();

                    if (rank == 3) {
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                targetIndex.set(m, y, x);
                                if (isTimeFill) {
                                    targetArray.setFloat(targetIndex, fillValue);
                                    continue;
                                }

                                final BilinearInterpolator interpolator = interpolationContext.get(x, y);
                                if (interpolator == null) {
                                    targetArray.setFloat(targetIndex, fillValue);
                                    continue;
                                }

                                // calculate offsets into era5 data
                                final int era5X = interpolator.getRelXMin();
                                final int era5Y = interpolator.getRelYMin();
                                // read c00 -> c11 directly from era5 data array
                                era5Index.set(0, era5Y, era5X);
                                final float c00 = era5Data.getFloat(era5Index);
                                era5Index.set(0, era5Y, era5X + 1);
                                final float c10 = era5Data.getFloat(era5Index);
                                era5Index.set(0, era5Y + 1, era5X);
                                final float c01 = era5Data.getFloat(era5Index);
                                era5Index.set(0, era5Y + 1, era5X + 1);
                                final float c11 = era5Data.getFloat(era5Index);

                                final double interpolate = interpolator.interpolate(c00, c10, c01, c11);

                                targetArray.setFloat(targetIndex, (float) interpolate);
                            }
                        }
                    } else if (rank == 4) {
                        for (int z = 0; z < numLayers; z++) {
                            for (int y = 0; y < height; y++) {
                                for (int x = 0; x < width; x++) {
                                    targetIndex.set(m, z, y, x);

                                    if (isTimeFill) {
                                        targetArray.setFloat(targetIndex, fillValue);
                                        continue;
                                    }

                                    final BilinearInterpolator interpolator = interpolationContext.get(x, y);
                                    if (interpolator == null) {
                                        targetArray.setFloat(targetIndex, fillValue);
                                        continue;
                                    }

                                    // calculate offsets into era5 data
                                    final int era5X = interpolator.getRelXMin();
                                    final int era5Y = interpolator.getRelYMin();
                                    // read c00 -> c11 directly from era5 data array
                                    era5Index.set(0, z, era5Y, era5X);
                                    final float c00 = era5Data.getFloat(era5Index);
                                    era5Index.set(0, z, era5Y, era5X + 1);
                                    final float c10 = era5Data.getFloat(era5Index);
                                    era5Index.set(0, z, era5Y + 1, era5X);
                                    final float c01 = era5Data.getFloat(era5Index);
                                    era5Index.set(0, z, era5Y + 1, era5X + 1);
                                    final float c11 = era5Data.getFloat(era5Index);

                                    final double interpolate = interpolator.interpolate(c00, c10, c01, c11);

                                    targetArray.setFloat(targetIndex, (float) interpolate);
                                }
                            }
                        }
                    } else {
                        throw new IllegalStateException("Unexpected variable rank: " + rank + "  " + variableKey);
                    }
                }
            }

            final Set<String> variableKeys = variables.keySet();
            for (final String variableKey : variableKeys) {
                final TemplateVariable templateVariable = variables.get(variableKey);
                final Array targetArray = targetArrays.get(variableKey);
                final Variable targetVariable = writer.findVariable(NetCDFUtils.escapeVariableName(templateVariable.getName()));
                writer.write(targetVariable, targetArray);
            }

        } finally {
            variableCache.close();
        }
    }

    static Array loadEra5Data(Variable variable, InterpolationContext interpolationContext, int numLayers) throws InvalidRangeException, IOException {
        // load the required ERA5 variable data
        final double scaleFactor = NetCDFUtils.getScaleFactor(variable);
        final double offset = NetCDFUtils.getOffset(variable);

        // request xRanges
        final IntRange[] xRanges = interpolationContext.getXRanges();
        final IntRange yRange = interpolationContext.getYRange();
        if (xRanges.length == 1) {
            return readFullEra5Array(variable, numLayers, yRange, xRanges[0], scaleFactor, offset);
        } else {
            final Array left = readFullEra5Array(variable, numLayers, yRange, xRanges[0], scaleFactor, offset);
            final Array right = readFullEra5Array(variable, numLayers, yRange, xRanges[1], scaleFactor, offset);

            return ArrayUtils.mergeAlongX(left, right);
        }
    }

    private static Array readFullEra5Array(Variable variable, int numLayers, IntRange yRange, IntRange xRange, double scaleFactor, double offset) throws IOException, InvalidRangeException {
        final int rank = variable.getRank();
        final int[] offsets;
        final int[] shape;

        final int yMin = yRange.getMin();
        final int xMin = xRange.getMin();
        int yLength = yRange.getLength();
        int xlength = xRange.getLength();

        if (rank == 3) {
            offsets = new int[]{0, yMin, xMin};
            shape = new int[]{1, yLength, xlength};
        } else if (rank == 4) {
            offsets = new int[]{0, 0, yMin, xMin};
            shape = new int[]{1, numLayers, yLength, xlength};
        } else {
            throw new IllegalStateException("unsupport input data rank");
        }

        Array era5Data;
        try {
            era5Data = variable.read(offsets, shape);
            if (ReaderUtils.mustScale(scaleFactor, offset)) {
                era5Data = NetCDFUtils.scale(era5Data, scaleFactor, offset);
            }
        } catch (Exception e) {
            final Logger logger = FiduceoLogger.getLogger();
            logger.severe("Unable to read: " + variable.getFullName());
            logger.severe("offsets: " + yMin + ", " + xMin);
            logger.severe("shape: " + yLength + ", " + xlength);
            throw e;
        }

        return era5Data;
    }

    private void addTimeVariable(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer) {
        final String nwp_time_variable_name = satFieldsConfig.get_nwp_time_variable_name();
        final Variable variable = writer.addVariable(nwp_time_variable_name, DataType.INT, FiduceoConstants.MATCHUP_COUNT);
        variable.addAttribute(new Attribute("description", "Timestamp of ERA-5 data"));
        variable.addAttribute(new Attribute("units", "seconds since 1970-01-01"));
        variable.addAttribute(new Attribute("_FillValue", NetCDFUtils.getDefaultFillValue(DataType.INT, false)));
    }

    // package access for testing purpose only tb 2020-12-02
    List<Dimension> getDimensions(TemplateVariable template) {
        List<Dimension> dimensions;
        if (template.is3d()) {
            dimensions = dimension3d;
        } else {
            dimensions = dimension2d;
        }
        return dimensions;
    }

    // package access for testing purpose only tb 2020-12-02
    void setDimensions(SatelliteFieldsConfiguration satFieldsConfig, NetcdfFileWriter writer, NetcdfFile reader) {
        final Dimension xDim = writer.addDimension(satFieldsConfig.get_x_dim_name(), satFieldsConfig.get_x_dim());
        final Dimension yDim = writer.addDimension(satFieldsConfig.get_y_dim_name(), satFieldsConfig.get_y_dim());
        final Dimension zDim = writer.addDimension(satFieldsConfig.get_z_dim_name(), satFieldsConfig.get_z_dim());

        final Dimension matchupDim = reader.findDimension(satFieldsConfig.getMatchupDimensionName());

        dimension2d = new ArrayList<>();
        dimension2d.add(matchupDim);
        dimension2d.add(yDim);
        dimension2d.add(xDim);

        dimension3d = new ArrayList<>();
        dimension3d.add(matchupDim);
        dimension3d.add(zDim);
        dimension3d.add(yDim);
        dimension3d.add(xDim);
    }

    Map<String, TemplateVariable> getVariables(SatelliteFieldsConfiguration configuration) {
        final Map<String, TemplateVariable> generalizedVariables = configuration.getGeneralizedVariables();
        if (generalizedVariables != null) {
            for (String key : generalizedVariables.keySet()) {
                generalizedVariables.get(key).setName(configuration.getVarName(key));
            }
            return generalizedVariables;
        } else {
            final HashMap<String, TemplateVariable> variablesMap = new HashMap<>();

            variablesMap.put("an_ml_q", createTemplate(configuration.getVarName("an_ml_q"), "kg kg**-1", "Specific humidity", "specific_humidity", true));
            variablesMap.put("an_ml_t", createTemplate(configuration.getVarName("an_ml_t"), "K", "Temperature", "air_temperature", true));
            variablesMap.put("an_ml_o3", createTemplate(configuration.getVarName("an_ml_o3"), "kg kg**-1", "Ozone mass mixing ratio", null, true));
            variablesMap.put("an_ml_lnsp", createTemplate(configuration.getVarName("an_ml_lnsp"), "~", "Logarithm of surface pressure", null, false));
            variablesMap.put("an_sfc_t2m", createTemplate(configuration.getVarName("an_sfc_t2m"), "K", "2 metre temperature", null, false));
            variablesMap.put("an_sfc_u10", createTemplate(configuration.getVarName("an_sfc_u10"), "m s**-1", "10 metre U wind component", null, false));
            variablesMap.put("an_sfc_v10", createTemplate(configuration.getVarName("an_sfc_v10"), "m s**-1", "10 metre V wind component", null, false));
            variablesMap.put("an_sfc_siconc", createTemplate(configuration.getVarName("an_sfc_siconc"), "(0 - 1)", "Sea ice area fraction", "sea_ice_area_fraction", false));
            variablesMap.put("an_sfc_msl", createTemplate(configuration.getVarName("an_sfc_msl"), "Pa", "Mean sea level pressure", "air_pressure_at_mean_sea_level", false));
            variablesMap.put("an_sfc_skt", createTemplate(configuration.getVarName("an_sfc_skt"), "K", "Skin temperature", null, false));
            variablesMap.put("an_sfc_sst", createTemplate(configuration.getVarName("an_sfc_sst"), "K", "Sea surface temperature", null, false));
            variablesMap.put("an_sfc_tcc", createTemplate(configuration.getVarName("an_sfc_tcc"), "(0 - 1)", "Total cloud cover", "cloud_area_fraction", false));
            variablesMap.put("an_sfc_tcwv", createTemplate(configuration.getVarName("an_sfc_tcwv"), "kg m**-2", "Total column water vapour", "lwe_thickness_of_atmosphere_mass_content_of_water_vapor", false));

            return variablesMap;
        }
    }
}
