package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.post.PostProcessing;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.io.IOException;

class Era5PostProcessing extends PostProcessing {

    private static final double EPS = 0.00000001;

    private final Configuration configuration;

    private SatelliteFields satelliteFields;
    private MatchupFields matchupFields;

    Era5PostProcessing(Configuration configuration) {
        super();

        this.configuration = configuration;
        satelliteFields = null;
        matchupFields = null;
    }

    static int getEra5LatMin(float latMax) {
        final double shiftedLat = latMax + EPS;
        final double scaledLatMax = Math.ceil(shiftedLat * 4) / 4;
        return (int) ((90.0 - scaledLatMax) * 4.0);
    }

    static int getEra5LonMin(float lonMin) {
        final double normLonMin = (lonMin + 360.0) % 360.0;
        final double xIndex = Math.floor(normLonMin * 4);  // lon index
        return (int) xIndex;
    }

    // package access for testing only tb 2020-11-20
    static InterpolationContext getInterpolationContext(Array lonArray, Array latArray) {
        final int[] shape = lonArray.getShape();
        if (shape.length == 2) {
            return createInterpolationContext_2D(lonArray, latArray, shape);
        } else if (shape.length == 0) {
            return createInterpolationContext_0D(lonArray, latArray);
        }

        throw new IllegalStateException("Unsupported dimensionality of geolocation data");
    }

    private static InterpolationContext createInterpolationContext_2D(Array lonArray, Array latArray, int[] shape) {
        final InterpolationContext context = new InterpolationContext(shape[1], shape[0]);

        final Index lonIdx = lonArray.getIndex();
        final Index latIdx = latArray.getIndex();
        for (int y = 0; y < shape[0]; y++) {
            for (int x = 0; x < shape[1]; x++) {
                lonIdx.set(y, x);
                latIdx.set(y, x);

                final float lon = lonArray.getFloat(lonIdx);
                final float lat = latArray.getFloat(latIdx);
                if (!(isValidLon(lon) && isValidLat(lat))) {
                    // we cannot interpolate here tb 2021-05-04
                    continue;
                }

                // + detect four era5 corner-points for interpolation
                // + calculate longitude delta -> a
                // + calculate latitude delta -> b
                // + create BilinearInterpolator(a, b)
                // + store to context at (x, y)
                final int era5_X_min = getEra5LonMin(lon);
                final int era5_Y_min = getEra5LatMin(lat);

                final BilinearInterpolator interpolator = createInterpolator(lon, lat, era5_X_min, era5_Y_min);
                context.set(x, y, interpolator);
            }
        }


        return context;
    }

    static BilinearInterpolator createInterpolator(float lon, float lat, int era5_X_min, int era5_Y_min) {
        double era5LonMin = era5_X_min * 0.25;
        if (era5LonMin >= 180) {
            era5LonMin -= 360.0;
        }
        final double era5LatMin = 90.0 - era5_Y_min * 0.25;

        // we have a quarter degree raster and need to normalize the distance tb 2020-11-20
        final double lonDelta = (lon - era5LonMin) * 4.0;
        final double latDelta = (era5LatMin - lat) * 4.0;

        return new BilinearInterpolator(lonDelta, latDelta, era5_X_min, era5_Y_min);
    }

    private static InterpolationContext createInterpolationContext_0D(Array lonArray, Array latArray) {
        final InterpolationContext context = new InterpolationContext(1, 1);

        final float lon = lonArray.getFloat(0);
        final float lat = latArray.getFloat(0);

        final int era5_X_min = getEra5LonMin(lon);
        final int era5_Y_min = getEra5LatMin(lat);

        final BilinearInterpolator interpolator = createInterpolator(lon, lat, era5_X_min, era5_Y_min);
        context.set(0, 0, interpolator);

        return context;
    }

    static boolean isValidLon(float lon) {
        return lon >= -180.f && lon <= 180.f;
    }

    static boolean isValidLat(float lat) {
        return lat >= -90.f && lat <= 90.f;
    }

    // package access for testing only tb 2021-01-12
    static Era5Collection getEra5Collection(Configuration configuration) {
        String collection = configuration.getEra5Collection();
        if (StringUtils.isNotNullAndNotEmpty(collection)) {
            return Era5Collection.fromString(collection);
        }

        // we need to find the collection in the path-name
        final String nwpAuxDir = configuration.getNWPAuxDir();
        final String upperCaseAuxDir = nwpAuxDir.toUpperCase();
        if (upperCaseAuxDir.contains("ERA5T") || upperCaseAuxDir.contains("ERA-5T")) {
            return Era5Collection.ERA_5T;
        } else if (upperCaseAuxDir.contains("ERA51") || upperCaseAuxDir.contains("ERA-51")) {
            return Era5Collection.ERA_51;
        } else if (upperCaseAuxDir.contains("ERA5") || upperCaseAuxDir.contains("ERA-5")) {
            return Era5Collection.ERA_5;
        }
        throw new IllegalArgumentException("Unable to detect ERA5 collection from datapath. Please configure explicitly.");
    }

    @Override
    protected void prepare(NetcdfFile reader, NetcdfFileWriter writer) {
        final String matchupDimensionName = getMatchupDimensionName();
        final Dimension matchupCountDimension = reader.findDimension(matchupDimensionName);
        if (matchupCountDimension == null) {
            throw new RuntimeException("Expected dimension not present in file: " + matchupDimensionName);
        }

        final Era5Collection collection = getEra5Collection(configuration);
        writer.addGlobalAttribute("era5-collection", collection.toString());

        final SatelliteFieldsConfiguration satFieldsConfig = configuration.getSatelliteFields();
        if (satFieldsConfig != null) {
            satFieldsConfig.setMatchupDimensionName(getMatchupDimensionName());
            satelliteFields = new SatelliteFields();
            satelliteFields.prepare(satFieldsConfig, reader, writer, collection);
        }

        final MatchupFieldsConfiguration matchupFieldsConfig = configuration.getMatchupFields();
        if (matchupFieldsConfig != null) {
            matchupFieldsConfig.setMatchupDimensionName(getMatchupDimensionName());
            matchupFields = new MatchupFields();
            matchupFields.prepare(matchupFieldsConfig, reader, writer, collection);
        }
    }

    @Override
    protected void compute(NetcdfFile reader, NetcdfFileWriter writer) throws IOException, InvalidRangeException {
        if (satelliteFields != null) {
            satelliteFields.compute(configuration, reader, writer);
        }

        if (matchupFields != null) {
            matchupFields.compute(configuration, reader, writer);
        }
    }

    @Override
    protected void dispose() {
        satelliteFields = null;
        matchupFields = null;

        super.dispose();
    }
}
