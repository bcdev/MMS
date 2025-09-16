package com.bc.fiduceo.location;

import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.geom.Point2D;

public class PixelGeoCodingPixelLocator implements PixelLocator {

    private final ComponentGeoCoding geoCoding;

    public PixelGeoCodingPixelLocator(Array longitudes, Array latitudes, String lonVariableName, String latVariableName, double groundResolutionInKm, GeoChecks geoChecks) {
        final double[] lonArray = (double[]) longitudes.get1DJavaArray(DataType.DOUBLE);
        final double[] latArray = (double[]) latitudes.get1DJavaArray(DataType.DOUBLE);
        final int[] shape = longitudes.getShape();

        final GeoRaster geoRaster = new GeoRaster(lonArray, latArray, lonVariableName, latVariableName, shape[1], shape[0], groundResolutionInKm);
        final ForwardCoding forwardCoding = ComponentFactory.getForward(PixelForward.KEY);
        final InverseCoding inverseCoding = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);
        geoCoding = new ComponentGeoCoding(geoRaster, forwardCoding, inverseCoding, geoChecks);
        geoCoding.initialize();
    }

    @Override
    public Point2D getGeoLocation(double x, double y, Point2D g) {
        final GeoPos geoPos = geoCoding.getGeoPos(new PixelPos(x + 0.5, y + 0.5), null);
        return new Point2D.Double(geoPos.lon, geoPos.lat);
    }

    @Override
    public Point2D[] getPixelLocation(double lon, double lat) {
        final PixelPos pixelPos = geoCoding.getPixelPos(new GeoPos(lat, lon), null);
        return new Point2D[]{new Point2D.Double(pixelPos.getX() - 0.5, pixelPos.getY() - 0.5)};
    }
}
