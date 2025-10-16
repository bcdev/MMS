package com.bc.fiduceo.matchup;

import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.db.DbAndIOTestRunner;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Minimal debug test to trace why SCOPE matchups fail.
 *
 * Uses specific known data:
 * - In-situ: April 1, 2016 at lon=-117.305, lat=32.955
 * - Satellite: April 2016 monthly composite
 */
@RunWith(DbAndIOTestRunner.class)
public class DebugScopeMatchup {

    @Test
    public void testSingleInsituPointMatchup() throws Exception {
        System.out.println("=== DEBUG SCOPE MATCHUP ===");

        // Known in-situ point from April 1, 2016
        final double insituLon = -117.305;
        final double insituLat = 32.955;
        final long insituTime = 1459468800000L; // April 1, 2016 00:00:00 UTC

        System.out.println("\n1. IN-SITU POINT:");
        System.out.println("   Longitude: " + insituLon);
        System.out.println("   Latitude: " + insituLat);
        System.out.println("   Time: " + new Date(insituTime) + " (" + insituTime + ")");

        // Open satellite file for April 2016
        final File satelliteFile = getSatelliteFile();
        System.out.println("\n2. SATELLITE FILE:");
        System.out.println("   Path: " + satelliteFile.getAbsolutePath());
        System.out.println("   Exists: " + satelliteFile.exists());

        // Create reader
        final ReaderContext readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));

        // Initialize ReaderFactory
        final ReaderFactory readerFactory = ReaderFactory.create(
            readerContext.getGeometryFactory(),
            null,  // tempFileUtils
            null,  // archive
            null   // readerConfigDirectory
        );
        final Reader reader = readerFactory.getReader("scope-sat-pp");
        System.out.println("   Reader class: " + reader.getClass().getName());

        reader.open(satelliteFile);
        System.out.println("   After open, reader class: " + reader.getClass().getName());

        // Get PixelLocator
        final PixelLocator pixelLocator = reader.getPixelLocator();
        assertNotNull("PixelLocator should not be null", pixelLocator);

        System.out.println("\n3. PIXEL LOCATOR TEST:");
        System.out.println("   Searching for pixel at (" + insituLon + ", " + insituLat + ")");

        // Try to find pixel for in-situ coordinates
        final Point2D[] pixels = pixelLocator.getPixelLocation(insituLon, insituLat);

        if (pixels == null) {
            System.out.println("   ❌ FAILURE: PixelLocator.getPixelLocation() returned NULL");
            fail("PixelLocator returned null for valid coordinates");
        } else if (pixels.length == 0) {
            System.out.println("   ❌ FAILURE: PixelLocator.getPixelLocation() returned EMPTY array");
            fail("PixelLocator returned empty array for valid coordinates");
        } else {
            System.out.println("   ✓ SUCCESS: Found " + pixels.length + " pixel(s)");
            final Point2D pixel = pixels[0];
            System.out.println("   Pixel coordinates: (" + pixel.getX() + ", " + pixel.getY() + ")");

            // Verify pixel is in bounds
            final int x = (int) pixel.getX();
            final int y = (int) pixel.getY();

            System.out.println("\n4. REVERSE GEO LOOKUP:");
            System.out.println("   Getting geo location for pixel (" + x + ".5, " + y + ".5)");

            // Do reverse lookup (what SampleCollector does)
            final Point2D geoPos = pixelLocator.getGeoLocation(x + 0.5, y + 0.5, null);

            if (geoPos == null) {
                System.out.println("   ❌ FAILURE: Reverse lookup returned NULL");
                fail("Reverse geo location returned null");
            } else if (Double.isNaN(geoPos.getX()) || Double.isNaN(geoPos.getY())) {
                System.out.println("   ❌ FAILURE: Reverse lookup returned NaN");
                System.out.println("   Lon: " + geoPos.getX() + ", Lat: " + geoPos.getY());
                fail("Reverse geo location returned NaN");
            } else {
                System.out.println("   ✓ SUCCESS: Reverse lookup returned valid coordinates");
                System.out.println("   Lon: " + geoPos.getX() + ", Lat: " + geoPos.getY());

                // Check if close to original
                final double lonDiff = Math.abs(geoPos.getX() - insituLon);
                final double latDiff = Math.abs(geoPos.getY() - insituLat);
                System.out.println("   Difference: lon=" + lonDiff + "°, lat=" + latDiff + "°");
            }

            // Test time locator
            System.out.println("\n5. TIME LOCATOR TEST:");
            final com.bc.fiduceo.reader.time.TimeLocator timeLocator = reader.getTimeLocator();
            final long pixelTime = timeLocator.getTimeFor(x, y);
            System.out.println("   Pixel time: " + new Date(pixelTime) + " (" + pixelTime + ")");

            if (pixelTime < 0) {
                System.out.println("   ❌ FAILURE: Time is negative");
                fail("Time locator returned negative time");
            } else {
                System.out.println("   ✓ SUCCESS: Time is positive");

                // Check if within ±15 days of in-situ time
                final long timeDiff = Math.abs(pixelTime - insituTime);
                final long fifteenDaysMillis = 15L * 24 * 60 * 60 * 1000;
                System.out.println("   Time difference: " + (timeDiff / (24 * 60 * 60 * 1000)) + " days");
                System.out.println("   Within ±15 days? " + (timeDiff <= fifteenDaysMillis));
            }
        }

        reader.close();
        System.out.println("\n=== TEST COMPLETE ===");
    }

    private File getSatelliteFile() throws Exception {
        final String relativePath = TestUtil.assembleFileSystemPath(
            new String[]{"satellite", "scope-merge", "wp26", "2016", "04",
                        "SCOPE_NCEO_PP_ESA-OC-L3S-MERGED-1M_MONTHLY_9km_mapped_201604-fv6.0.out.nc"},
            false
        );
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
