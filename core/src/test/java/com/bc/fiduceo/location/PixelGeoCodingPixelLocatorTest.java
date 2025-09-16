package com.bc.fiduceo.location;

import org.esa.snap.core.dataio.geocoding.GeoChecks;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;

public class PixelGeoCodingPixelLocatorTest {

    private PixelGeoCodingPixelLocator pixelGeoCodingPixelLocator;

    @Before
    public void setUp() {
        final float[] longitudes = {176.4837f, 176.38458f, 176.28566f, 176.23f, 176.13022f, 176.03065f};
        final float[] latitudes = {-72.1376f, -72.16673f, -72.19598f, -72.17846f, -72.207436f, -72.236534f};

        final Array lonArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, longitudes);
        final Array latArray = Array.factory(DataType.FLOAT, new int[]{2, 3}, latitudes);

        pixelGeoCodingPixelLocator = new PixelGeoCodingPixelLocator(lonArray, latArray, "longitude", "latituide", 35, GeoChecks.NONE);
    }

    @Test
    public void testGetGeoLocation() {
        Point2D geoLocation = pixelGeoCodingPixelLocator.getGeoLocation(0, 0, null);
        assertEquals(176.48370361328125, geoLocation.getX(), 1e-8);
        assertEquals(-72.13760375976562, geoLocation.getY(), 1e-8);

        geoLocation = pixelGeoCodingPixelLocator.getGeoLocation(1, 1, null);
        assertEquals(176.13021850585938, geoLocation.getX(), 1e-8);
        assertEquals(-72.20743560791016, geoLocation.getY(), 1e-8);

        geoLocation = pixelGeoCodingPixelLocator.getGeoLocation(-1, 0, null);
        assertEquals(Double.NaN, geoLocation.getX(), 1e-8);
        assertEquals(Double.NaN, geoLocation.getY(), 1e-8);

        geoLocation = pixelGeoCodingPixelLocator.getGeoLocation(1, 17, null);
        assertEquals(Double.NaN, geoLocation.getX(), 1e-8);
        assertEquals(Double.NaN, geoLocation.getY(), 1e-8);
    }

    @Test
    public void testGetPixelLocation() {
        Point2D[] pixelLocation = pixelGeoCodingPixelLocator.getPixelLocation(176.48370361328125, -72.13760375976562);
        assertEquals(1, pixelLocation.length);
        assertEquals(0, pixelLocation[0].getX(), 1e-8);
        assertEquals(0, pixelLocation[0].getY(), 1e-8);

        pixelLocation = pixelGeoCodingPixelLocator.getPixelLocation(176.13021850585938, -72.20743560791016);
        assertEquals(1, pixelLocation.length);
        assertEquals(1, pixelLocation[0].getX(), 1e-8);
        assertEquals(1, pixelLocation[0].getY(), 1e-8);

        pixelLocation = pixelGeoCodingPixelLocator.getPixelLocation(22.9, 0.8);
        assertEquals(1, pixelLocation.length);
        assertEquals(Double.NaN, pixelLocation[0].getX(), 1e-8);
        assertEquals(Double.NaN, pixelLocation[0].getY(), 1e-8);
    }
}
