package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.core.SatelliteGeometry;
import com.bc.fiduceo.math.TimeAxis;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeometryUtilsTest {

    private WKTReader wktReader;

    @Before
    public void setUp() {
        wktReader = new WKTReader();
    }

    @Test
    public void testNormalizePolygon_tooSmallArray() {
        Coordinate[] coordinates = new Coordinate[0];
        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(0, coordinates.length);

        coordinates = new Coordinate[1];
        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(1, coordinates.length);
    }

    @Test
    public void testNormalizePolygon_noNormaization() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(10, 10);
        coordinates[1] = new Coordinate(10, 20);
        coordinates[2] = new Coordinate(20, 20);
        coordinates[3] = new Coordinate(20, 10);
        coordinates[4] = new Coordinate(10, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(10, coordinates[0].x, 1e-8);
        assertEquals(10, coordinates[1].x, 1e-8);
        assertEquals(20, coordinates[2].x, 1e-8);
        assertEquals(20, coordinates[3].x, 1e-8);
        assertEquals(10, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeEast() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(170, 10);
        coordinates[1] = new Coordinate(170, 20);
        coordinates[2] = new Coordinate(-175, 20);
        coordinates[3] = new Coordinate(-175, 10);
        coordinates[4] = new Coordinate(170, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(170, coordinates[0].x, 1e-8);
        assertEquals(170, coordinates[1].x, 1e-8);
        assertEquals(185, coordinates[2].x, 1e-8);
        assertEquals(185, coordinates[3].x, 1e-8);
        assertEquals(170, coordinates[4].x, 1e-8);
    }

    @Test
    public void testNormalizePolygon_normalizeWest() {
        final Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(-170, 10);
        coordinates[1] = new Coordinate(-170, 20);
        coordinates[2] = new Coordinate(175, 20);
        coordinates[3] = new Coordinate(175, 10);
        coordinates[4] = new Coordinate(-170, 10);

        GeometryUtils.normalizePolygon(coordinates);
        assertEquals(190, coordinates[0].x, 1e-8);
        assertEquals(190, coordinates[1].x, 1e-8);
        assertEquals(175, coordinates[2].x, 1e-8);
        assertEquals(175, coordinates[3].x, 1e-8);
        assertEquals(190, coordinates[4].x, 1e-8);
    }

    @Test
    public void testMapToGlobe_onlyPointsInGlobe() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((10 10, 20 10, 20 20, 10 20, 10 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((10 10, 10 20, 20 20, 20 10, 10 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedOnlyGlobe() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((-200 10, -190 10, -190 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((160 10, 160 20, 170 20, 170 10, 160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_westShiftedAndCentralGlobe() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((-200 10, -170 10, -170 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -170 20, -170 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedOnlyGlobe() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((200 10, 210 10, 210 20, 200 20, 200 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(1, mappedPolygons.length);
        assertEquals("POLYGON ((-160 10, -160 20, -150 20, -150 10, -160 10))", mappedPolygons[0].toString());
    }

    @Test
    public void testMapToGlobe_eastShiftedAndCentralGlobe() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((170 10, 210 10, 210 20, 170 20, 170 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(2, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 170 10, 170 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[1].toString());
    }

    @Test
    public void testMapToGlobe_allShiftsPresent() throws ParseException {
        final Geometry polygonInGlobe = wktReader.read("POLYGON((-200 10, 210 10, 210 20, -200 20, -200 10))");

        final Polygon[] mappedPolygons = GeometryUtils.mapToGlobe(polygonInGlobe);
        assertEquals(3, mappedPolygons.length);
        assertEquals("POLYGON ((180 20, 180 10, 160 10, 160 20, 180 20))", mappedPolygons[0].toString());
        assertEquals("POLYGON ((-180 10, -180 20, 180 20, 180 10, -180 10))", mappedPolygons[1].toString());
        assertEquals("POLYGON ((-180 10, -180 20, -150 20, -150 10, -180 10))", mappedPolygons[2].toString());
    }

    @Test
    public void testCreateTimeAxis() throws ParseException {
        final Geometry polygon = wktReader.read("POLYGON((10 30, 10 20, 10 10, 20 10, 30 10, 30 20, 30 30, 20 30, 10 30))");

        final TimeAxis timeAxis =  GeometryUtils.createTimeAxis(polygon, 0, 2, new Date(1000), new Date(2000));
        assertNotNull(timeAxis);
        assertEquals(1000, timeAxis.getTime(new Coordinate(10, 30)).getTime());
        assertEquals(1500, timeAxis.getTime(new Coordinate(10, 20)).getTime());
        assertEquals(2000, timeAxis.getTime(new Coordinate(10, 10)).getTime());
    }

    @Test
    public void testPrepareForStorage_onePolygon_oneAxis_descending() {
        final AcquisitionInfo acquisitionInfo = new AcquisitionInfo();
        final List<Coordinate> coordinateList = createCoordinateList(new double[]{10, 10, 10, 30, 30, 30, 10}, new double[]{30, 20, 10, 10, 20, 30, 30});
        acquisitionInfo.setCoordinates(coordinateList);
        acquisitionInfo.setTimeAxisStartIndex(0);
        acquisitionInfo.setTimeAxisEndIndex(2);
        acquisitionInfo.setSensingStart(new Date(100000));
        acquisitionInfo.setSensingStop(new Date(200000));
        acquisitionInfo.setNodeType(NodeType.DESCENDING);

        final SatelliteGeometry satelliteGeometry = GeometryUtils.prepareForStorage(acquisitionInfo);
        assertNotNull(satelliteGeometry);

        final Geometry geometry = satelliteGeometry.getGeometry();
        assertNotNull(geometry);
        assertEquals("POLYGON ((10 30, 10 20, 10 10, 30 10, 30 20, 30 30, 10 30))", geometry.toString());

        final TimeAxis[] timeAxes = satelliteGeometry.getTimeAxes();
        assertNotNull(timeAxes);
        assertEquals(1, timeAxes.length);
        assertEquals(150000, timeAxes[0].getTime(new Coordinate(10, 20)).getTime());
    }

    private List<Coordinate> createCoordinateList(double[] lons, double[] lats) {
        final ArrayList<Coordinate> coordinates = new ArrayList<>(lons.length);

        for (int i = 0; i < lons.length; i++){
            coordinates.add(new Coordinate(lons[i], lats[i]));
        }

        return coordinates;
    }
}