package com.bc.fiduceo.geometry;

import static org.junit.Assert.assertEquals;

public class GeometryTestUtils {

    public static void assertSameGeometry(Geometry expected, Geometry geoBounds) {
        Point[] expectedCoords = expected.getCoordinates();
        Point[] actualCoords = geoBounds.getCoordinates();
        assertEquals(expectedCoords.length, actualCoords.length);
        for(int i = 0; i < expectedCoords.length; i++) {
            assertEquals(expectedCoords[i].getLon(), actualCoords[i].getLon(), 1e-8);
            assertEquals(expectedCoords[i].getLat(), actualCoords[i].getLat(), 1e-8);
        }
    }
}
