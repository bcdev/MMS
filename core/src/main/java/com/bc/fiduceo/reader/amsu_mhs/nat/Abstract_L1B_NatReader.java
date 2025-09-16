package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelGeoCodingPixelLocator;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import ucar.ma2.Array;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.*;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.LAT_VAR_NAME;
import static com.bc.fiduceo.reader.amsu_mhs.nat.EPS_Constants.LON_VAR_NAME;

abstract public class Abstract_L1B_NatReader implements Reader {

    protected VariableRegistry registry;
    protected EpsVariableCache cache;
    protected final GeometryFactory geometryFactory;

    private PixelLocator pixelLocator;

    public Abstract_L1B_NatReader(ReaderContext readerContext) {
        this.geometryFactory = readerContext.getGeometryFactory();
        pixelLocator = null;
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Array lon = cache.getScaled(LON_VAR_NAME);
            final Array lat = cache.getScaled(LAT_VAR_NAME);

            pixelLocator = new PixelGeoCodingPixelLocator(lon, lat, LON_VAR_NAME, LAT_VAR_NAME, 48.0, GeoChecks.POLES);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String[] strings = fileName.split("_");
        final String dateTimePart = strings[4];

        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(dateTimePart.substring(0, 4));
        ymd[1] = Integer.parseInt(dateTimePart.substring(4, 6));
        ymd[2] = Integer.parseInt(dateTimePart.substring(6, 8));
        return ymd;
    }

    protected static void setSensingDates(AcquisitionInfo acquisitionInfo, MPHR recordMPHR) throws IOException {
        final Date sensingStart = recordMPHR.getDate(SENSING_START_KEY);
        acquisitionInfo.setSensingStart(sensingStart);
        final Date sensingEnd = recordMPHR.getDate(SENSING_STOP_KEY);
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
        pixelLocator = null;
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
