/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.geometry.jts;

import com.bc.fiduceo.geometry.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JtsGeometryFactory extends AbstractGeometryFactory {

    private final org.locationtech.jts.geom.Polygon westShiftedGlobe;
    private final org.locationtech.jts.geom.Polygon eastShiftedGlobe;
    private final org.locationtech.jts.geom.Polygon centralGlobe;

    private final WKTReader wktReader;
    private final GeometryFactory geometryFactory;
    private final WKBWriter wkbWriter;
    private final WKBReader wkbReader;

    public JtsGeometryFactory() {
        wktReader = new WKTReader();
        wkbWriter = new WKBWriter();
        wkbReader = new WKBReader();
        geometryFactory = new GeometryFactory();

        centralGlobe = createCentralGlobe();
        eastShiftedGlobe = createEastShiftedGlobe();
        westShiftedGlobe = createWestShiftedGlobe();
    }

    // @todo 3 tb/** write tests 2016-09-23
    static void ensureClosedPolygon(List<Point> points) {
        final Point first = points.get(0);
        final int lastIndex = points.size() - 1;
        final Point last = points.get(lastIndex);
        if (!first.equals(last)) {
            points.add(points.get(0));
        }
    }

    static Coordinate[] extractCoordinates(List<Point> points) {
        final Coordinate[] coordinates = new Coordinate[points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            coordinates[i] = (Coordinate) point.getInner();
        }
        return coordinates;
    }

    private static Geometry convertGeometry(org.locationtech.jts.geom.Geometry geometry) {
        if (geometry instanceof org.locationtech.jts.geom.Polygon) {
            return new JTSPolygon((org.locationtech.jts.geom.Polygon) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.MultiPolygon) {
            return new JTSMultiPolygon((org.locationtech.jts.geom.MultiPolygon) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.LineString) {
            return new JTSLineString((org.locationtech.jts.geom.LineString) geometry);
        } else if (geometry instanceof org.locationtech.jts.geom.Point) {
            return new JTSPoint(geometry.getCoordinate());
        } else if (geometry instanceof org.locationtech.jts.geom.MultiLineString) {
            return new JTSMultiLineString((org.locationtech.jts.geom.MultiLineString) geometry);
        }
        throw new RuntimeException("Unsupported geometry type");
    }

    @Override
    public Geometry parse(String wkt) {
        final org.locationtech.jts.geom.Geometry geometry;
        try {
            geometry = wktReader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        return convertGeometry(geometry);
    }

    @Override
    public String format(Geometry geometry) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public byte[] toStorageFormat(Geometry geometry) {
        org.locationtech.jts.geom.Geometry jtsGeometry;
        final Object inner = geometry.getInner();
        if (inner instanceof Coordinate) {
            final Coordinate jtsCoordinate = (Coordinate) inner;
            jtsGeometry = geometryFactory.createPoint(jtsCoordinate);
        } else {
            jtsGeometry = (org.locationtech.jts.geom.Geometry) inner;
        }

        return wkbWriter.write(jtsGeometry);
    }

    @Override
    public Geometry fromStorageFormat(byte[] rawData) {
        final org.locationtech.jts.geom.Geometry geometry;
        try {
            geometry = wkbReader.read(rawData);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }

        return convertGeometry(geometry);
    }

    @Override
    public Point createPoint(double lon, double lat) {
        final Coordinate coordinate = new Coordinate(lon, lat);
        return new JTSPoint(coordinate);
    }

    @Override
    public Polygon createPolygon(List<Point> points) {
        ensureClosedPolygon(points);
        final Coordinate[] coordinates = extractCoordinates(points);

        JtsUtils.normalizePolygon(coordinates);
        final org.locationtech.jts.geom.Polygon polygon = geometryFactory.createPolygon(coordinates);
        final org.locationtech.jts.geom.Polygon[] polygons = mapToGlobe(polygon);
        if (polygons.length == 1) {
            return new JTSPolygon(polygons[0]);
        } else {
            final MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);
            return new JTSMultiPolygon(multiPolygon);
        }
    }

    @Override
    public com.bc.fiduceo.geometry.MultiPolygon createMultiPolygon(List<Polygon> polygonList) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public LineString createLineString(List<Point> points) {
        final Coordinate[] coordinates = extractCoordinates(points);

        org.locationtech.jts.geom.LineString lineString = geometryFactory.createLineString(coordinates);
        return new JTSLineString(lineString);
    }

    @Override
    public MultiLineString createMultiLineString(List<LineString> points) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TimeAxis createTimeAxis(LineString lineString, Date startTime, Date endTime) {
        final org.locationtech.jts.geom.LineString jtsLineString = (org.locationtech.jts.geom.LineString) lineString.getInner();
        return new JTSTimeAxis(jtsLineString, startTime, endTime);
    }

    org.locationtech.jts.geom.Polygon[] mapToGlobe(org.locationtech.jts.geom.Polygon polygon) {
        final ArrayList<org.locationtech.jts.geom.Polygon> geometries = new ArrayList<>();
        final org.locationtech.jts.geom.Polygon westShifted = (org.locationtech.jts.geom.Polygon) westShiftedGlobe.intersection((org.locationtech.jts.geom.Polygon) polygon.clone());
        if (!westShifted.isEmpty()) {
            westShifted.apply(new LonShifter(360.0));
            geometries.add(westShifted);
        }

        final org.locationtech.jts.geom.Polygon central = (org.locationtech.jts.geom.Polygon) centralGlobe.intersection((org.locationtech.jts.geom.Polygon) polygon.clone());
        if (!central.isEmpty()) {
            geometries.add(central);
        }

        final org.locationtech.jts.geom.Polygon eastShifted = (org.locationtech.jts.geom.Polygon) eastShiftedGlobe.intersection((org.locationtech.jts.geom.Polygon) polygon.clone());
        if (!eastShifted.isEmpty()) {
            eastShifted.apply(new LonShifter(-360.0));
            geometries.add(eastShifted);
        }

        return geometries.toArray(new org.locationtech.jts.geom.Polygon[geometries.size()]);
    }

    private org.locationtech.jts.geom.Polygon createCentralGlobe() {
        final Coordinate[] pointList = new Coordinate[5];
        pointList[0] = new Coordinate(-180, 90);
        pointList[1] = new Coordinate(-180, -90);
        pointList[2] = new Coordinate(180, -90);
        pointList[3] = new Coordinate(180, 90);
        pointList[4] = new Coordinate(-180, 90);
        return geometryFactory.createPolygon(pointList);
    }

    private org.locationtech.jts.geom.Polygon createEastShiftedGlobe() {
        final Coordinate[] pointList = new Coordinate[5];
        pointList[0] = new Coordinate(180, 90);
        pointList[1] = new Coordinate(180, -90);
        pointList[2] = new Coordinate(540, -90);
        pointList[3] = new Coordinate(540, 90);
        pointList[4] = new Coordinate(180, 90);
        return geometryFactory.createPolygon(pointList);
    }

    private org.locationtech.jts.geom.Polygon createWestShiftedGlobe() {
        final Coordinate[] pointList = new Coordinate[5];
        pointList[0] = new Coordinate(-540, 90);
        pointList[1] = new Coordinate(-540, -90);
        pointList[2] = new Coordinate(-180, -90);
        pointList[3] = new Coordinate(-180, 90);
        pointList[4] = new Coordinate(-540, 90);
        return geometryFactory.createPolygon(pointList);
    }
}
