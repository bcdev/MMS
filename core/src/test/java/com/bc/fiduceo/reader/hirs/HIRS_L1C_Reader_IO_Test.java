/*
 * Copyright (C) 2016 Brockmann Consult GmbH
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

package com.bc.fiduceo.reader.hirs;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.reader.AcquisitionInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(IOTestRunner.class)
public class HIRS_L1C_Reader_IO_Test {

    private File dataDirectory;
    private HIRS_L1C_Reader reader;

    @Before
    public void setUp() throws IOException {
        dataDirectory = TestUtil.getTestDataDirectory();
        reader = new HIRS_L1C_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_TIROSN() throws IOException {
        final File file = getTirosNFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 16, 23, 59, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1979, 10, 14, 18, 7, 33, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(127, coordinates.length);
            assertEquals(-47.484375, coordinates[1].getLon(), 1e-8);
            assertEquals(56.96875, coordinates[1].getLat(), 1e-8);

            assertEquals(111.625, coordinates[63].getLon(), 1e-8);
            assertEquals(-59.0703125, coordinates[63].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(127, coordinates.length);
            assertEquals(144.9609375, coordinates[2].getLon(), 1e-8);
            assertEquals(-65.484375, coordinates[2].getLat(), 1e-8);

            assertEquals(-43.484375, coordinates[64].getLon(), 1e-8);
            assertEquals(67.3671875, coordinates[64].getLat(), 1e-8);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA10() throws IOException {
        final File file = getNOAA10File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 6, 8, 45, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1989, 3, 17, 8, 2, 2, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(-170.703125, coordinates[1].getLon(), 1e-8);
            assertEquals(22.859375, coordinates[1].getLat(), 1e-8);

            assertEquals(-6.3515625, coordinates[63].getLon(), 1e-8);
            assertEquals(-25.4765625, coordinates[63].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(137, coordinates.length);
            assertEquals(6.6640625, coordinates[2].getLon(), 1e-8);
            assertEquals(-46.84375, coordinates[2].getLat(), 1e-8);

            assertEquals(178.5625, coordinates[65].getLon(), 1e-8);
            assertEquals(56.78125, coordinates[65].getLat(), 1e-8);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_METOPA() throws IOException {
        final File file = getMetopAFile();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 16, 41, 20, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(2011, 8, 23, 18, 22, 40, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(125, coordinates.length);
            assertEquals(10.2899, coordinates[4].getLon(), 1e-8);
            assertEquals(76.641, coordinates[4].getLat(), 1e-8);

            assertEquals(177.6307, coordinates[61].getLon(), 1e-8);
            assertEquals(-68.9219, coordinates[61].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(125, coordinates.length);
            assertEquals(-166.8919, coordinates[5].getLon(), 1e-8);
            assertEquals(-80.9488, coordinates[5].getLat(), 1e-8);

            assertEquals(52.7125, coordinates[60].getLon(), 1e-8);
            assertEquals(76.5637, coordinates[60].getLat(), 1e-8);
        } finally {
            reader.close();
        }
    }

    private File getMetopAFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-ma", "1.0", "2011", "08", "23", "190583863.NSS.HIRX.M2.D11235.S1641.E1823.B2513233.SV.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getTirosNFile() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-tn", "1.0", "1979", "10", "14", "NSS.HIRX.TN.D79287.S1623.E1807.B0516566.GC.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getNOAA10File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"hirs-n10", "1.0", "1989", "03", "17", "NSS.HIRX.NG.D89076.S0608.E0802.B1296162.WI.nc"}, false);
        return getFileAsserted(testFilePath);
    }

    private File getFileAsserted(String testFilePath) {
        final File file = new File(dataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
