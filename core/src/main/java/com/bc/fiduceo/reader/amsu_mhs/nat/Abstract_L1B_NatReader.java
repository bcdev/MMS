package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import ucar.ma2.Array;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

abstract public class Abstract_L1B_NatReader implements Reader {

    protected VariableRegistry registry;
    protected EpsVariableCache cache;
    protected final GeometryFactory geometryFactory;

    public Abstract_L1B_NatReader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
    }

    protected static void setSensingDates(AcquisitionInfo acquisitionInfo, MPHR recordMPHR) throws IOException {
        final Date sensingStart = recordMPHR.getDate("SENSING_START");
        acquisitionInfo.setSensingStart(sensingStart);
        final Date sensingEnd = recordMPHR.getDate("SENSING_END");
        acquisitionInfo.setSensingStop(sensingEnd);
    }

    protected void initializeRegistry(String resourceKey) {
        registry = VariableRegistry.load(resourceKey);
    }

    protected void readDataToCache(File file, int sensorKey) throws IOException {
        final byte[] rawDataBuffer;
        try (FileInputStream fis = new FileInputStream(file)) {
            rawDataBuffer = fis.readAllBytes();
        }
        cache = new EpsVariableCache(rawDataBuffer, registry, sensorKey);
    }

    @Override
    public void close() throws IOException {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
        // @todo 2 tb/tb implement clear() method for registry
        registry = null;
    }

    protected Geometries extractGeometries(Array longitudes, Array latitudes, int numSplits, Interval interval) throws IOException {
        final Geometries geometries = new Geometries();
        final BoundingPolygonCreator boundingPolygonCreator = getBoundingPolygonCreator(interval);

        Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometryClockwise(longitudes, latitudes);
        Geometry timeAxisGeometry;

        if (!boundingGeometry.isValid()) {
            boundingGeometry = boundingPolygonCreator.createBoundingGeometrySplitted(longitudes, latitudes, numSplits, true);
            if (!boundingGeometry.isValid()) {
                throw new RuntimeException("Invalid bounding geometry detected");
            }
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometrySplitted(longitudes, latitudes, numSplits);
        } else {
            timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitudes, latitudes);
        }

        geometries.setBoundingGeometry(boundingGeometry);
        geometries.setTimeAxesGeometry(timeAxisGeometry);

        return geometries;
    }

    private BoundingPolygonCreator getBoundingPolygonCreator(Interval interval) {
        return new BoundingPolygonCreator(interval, geometryFactory);
    }
}
