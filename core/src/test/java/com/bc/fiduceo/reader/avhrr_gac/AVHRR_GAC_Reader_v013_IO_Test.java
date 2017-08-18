/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.avhrr_gac;


import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryCollection;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.geometry.TimeAxis;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.TimeLocator;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.iosp.netcdf3.N3iosp;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ThrowFromFinallyBlock")
@RunWith(IOTestRunner.class)
public class AVHRR_GAC_Reader_v013_IO_Test {

    private File testDataDirectory;
    private AVHRR_GAC_Reader reader;

    @Before
    public void setUp() throws IOException {
        testDataDirectory = TestUtil.getTestDataDirectory();
        reader = new AVHRR_GAC_Reader(new GeometryFactory(GeometryFactory.Type.S2));
    }

    @Test
    public void testReadAcquisitionInfo_NOAA10() throws IOException {
        final File file = createAvhrrNOAA10File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 2, 3, 15, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(-53.618011474609375, coordinates[0].getLon(), 1e-8);
            assertEquals(-6.572000026702881, coordinates[0].getLat(), 1e-8);

            assertEquals(-61.7869873046875, coordinates[23].getLon(), 1e-8);
            assertEquals(59.71799850463867, coordinates[23].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(82.66300201416016, coordinates[0].getLon(), 1e-8);
            assertEquals(-11.767999649047852, coordinates[0].getLat(), 1e-8);

            assertEquals(22.195999145507816, coordinates[23].getLon(), 1e-8);
            assertEquals(-66.61399841308594, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 0, 9, 17, 0, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1988, 3, 18, 1, 6, 29, 426, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionInfo_NOAA11() throws IOException {
        final File file = createAvhrrNOAA11File();

        try {
            reader.open(file);

            final AcquisitionInfo acquisitionInfo = reader.read();
            assertNotNull(acquisitionInfo);

            final Date sensingStart = acquisitionInfo.getSensingStart();
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 46, 0, sensingStart);

            final Date sensingStop = acquisitionInfo.getSensingStop();
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 9, 45, 41, 0, sensingStop);

            final NodeType nodeType = acquisitionInfo.getNodeType();
            assertEquals(NodeType.UNDEFINED, nodeType);

            final Geometry boundingGeometry = acquisitionInfo.getBoundingGeometry();
            assertNotNull(boundingGeometry);
            assertTrue(boundingGeometry instanceof GeometryCollection);
            final GeometryCollection geometryCollection = (GeometryCollection) boundingGeometry;
            final Geometry[] geometries = geometryCollection.getGeometries();
            assertEquals(2, geometries.length);

            Point[] coordinates = geometries[0].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(-94.49798583984375, coordinates[17].getLon(), 1e-8);
            assertEquals(36.73500061035156, coordinates[17].getLat(), 1e-8);

            assertEquals(-170.2830047607422, coordinates[58].getLon(), 1e-8);
            assertEquals(-66.19999694824219, coordinates[58].getLat(), 1e-8);

            coordinates = geometries[1].getCoordinates();
            assertEquals(161, coordinates.length);
            assertEquals(127.80999755859375, coordinates[0].getLon(), 1e-8);
            assertEquals(-56.1150016784668, coordinates[0].getLat(), 1e-8);

            assertEquals(93.28900146484375, coordinates[23].getLon(), 1e-8);
            assertEquals(4.603000164031982, coordinates[23].getLat(), 1e-8);

            final TimeAxis[] timeAxes = acquisitionInfo.getTimeAxes();
            assertEquals(2, timeAxes.length);
            coordinates = geometries[0].getCoordinates();
            Date time = timeAxes[0].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 7, 51, 48, 409, time);

            coordinates = geometries[1].getCoordinates();
            time = timeAxes[1].getTime(coordinates[0]);
            TestUtil.assertCorrectUTCDate(1991, 5, 9, 8, 48, 43, 500, time);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1098704671000L;
            assertEquals(referenceTime + 501, timeLocator.getTimeFor(168, 1));
            assertEquals(referenceTime + 7000, timeLocator.getTimeFor(169, 14));
            assertEquals(referenceTime + 507500, timeLocator.getTimeFor(170, 1015));
            assertEquals(referenceTime + 1008002, timeLocator.getTimeFor(171, 2016));
            assertEquals(referenceTime + 6835000, timeLocator.getTimeFor(172, 13670));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetTimeLocator_NOAA18() throws IOException {
        final File file = createAvhrrNOAA18File();

        try {
            reader.open(file);
            final TimeLocator timeLocator = reader.getTimeLocator();
            assertNotNull(timeLocator);

            final long referenceTime = 1359916015000L;
            assertEquals(referenceTime, timeLocator.getTimeFor(301, 0));
            assertEquals(referenceTime + 7512, timeLocator.getTimeFor(304, 15));
            assertEquals(referenceTime + 1008002, timeLocator.getTimeFor(304, 2016));
            assertEquals(referenceTime + 2008507, timeLocator.getTimeFor(305, 4017));
            assertEquals(referenceTime + 5119512, timeLocator.getTimeFor(172, 10239));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA19() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA19File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(35, 210, new Interval(3, 3));
            NCTestUtils.assertValueAt(1446850819, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1446850819, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(1446850819, 1, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA10_singlePixel() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA10File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(39, 214, new Interval(1, 1));
            NCTestUtils.assertValueAt(574646957, 0, 0, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadAcquisitionTime_NOAA11_borderPixel() throws IOException, InvalidRangeException {
        final File file = createAvhrrNOAA11File();

        try {
            reader.open(file);

            final ArrayInt.D2 acquisitionTime = reader.readAcquisitionTime(408, 210, new Interval(3, 3));
            NCTestUtils.assertValueAt(673775611, 0, 0, acquisitionTime);
            NCTestUtils.assertValueAt(673775611, 1, 0, acquisitionTime);
            NCTestUtils.assertValueAt(NetCDFUtils.getDefaultFillValue(int.class).intValue(), 2, 2, acquisitionTime);

        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetVariables_NOAA17() throws IOException {
        final File file = createAvhrrNOAA17File();

        try {
            reader.open(file);

            final List<Variable> variables = reader.getVariables();
            assertEquals(17, variables.size());
            Variable variable = variables.get(1);
            assertEquals("lon", variable.getFullName());

            variable = variables.get(9);
            assertEquals("satellite_zenith_angle", variable.getFullName());

            variable = variables.get(16);
            assertEquals("l1b_line_number", variable.getFullName());
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_windowCenter_NOAA18() throws Exception {
        final File file = createAvhrrNOAA18File();
        reader.open(file);
        try {
            final Array array = reader.readRaw(4, 14, new Interval(3, 3), "qual_flags");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(18, 0, 0, array);
            NCTestUtils.assertValueAt(18, 1, 0, array);
            NCTestUtils.assertValueAt(18, 2, 0, array);
            NCTestUtils.assertValueAt(18, 0, 1, array);
            NCTestUtils.assertValueAt(18, 1, 1, array);
            NCTestUtils.assertValueAt(18, 2, 1, array);
            NCTestUtils.assertValueAt(0, 0, 2, array);
            NCTestUtils.assertValueAt(0, 1, 2, array);
            NCTestUtils.assertValueAt(0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomWindowOut_NOAA19() throws Exception {
        final File avhrrNOAA18Path = createAvhrrNOAA19File();
        reader.open(avhrrNOAA18Path);

        try {
            final Array array = reader.readRaw(5, 12932, new Interval(3, 3), "relative_azimuth_angle");
            assertNotNull(array);
            assertEquals(9, array.getSize());

            NCTestUtils.assertValueAt(3907, 0, 0, array);
            NCTestUtils.assertValueAt(3911, 1, 0, array);
            NCTestUtils.assertValueAt(3914, 2, 0, array);
            NCTestUtils.assertValueAt(3908, 0, 1, array);
            NCTestUtils.assertValueAt(3912, 1, 1, array);
            NCTestUtils.assertValueAt(3916, 2, 1, array);
            NCTestUtils.assertValueAt(-32768.0, 0, 2, array);
            NCTestUtils.assertValueAt(-32768.0, 1, 2, array);
            NCTestUtils.assertValueAt(-32768.0, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topWindowOut_NOAA10() throws Exception {
        final File avhrrNOAA10File = createAvhrrNOAA10File();
        reader.open(avhrrNOAA10File);

        try {
            final Array array = reader.readRaw(3, 1, new Interval(3, 4), "satellite_zenith_angle");
            assertNotNull(array);
            assertEquals(12, array.getSize());

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(-32768, 2, 0, array);

            NCTestUtils.assertValueAt(6709, 0, 1, array);
            NCTestUtils.assertValueAt(6663, 1, 1, array);
            NCTestUtils.assertValueAt(6617, 2, 1, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_leftWindowOut_NOAA11() throws Exception {
        final File avhrrNOAA11File = createAvhrrNOAA11File();
        reader.open(avhrrNOAA11File);

        try {
            final Array array = reader.readRaw(2, 119, new Interval(8, 8), "solar_zenith_angle");
            assertNotNull(array);
            assertEquals(64, array.getSize());

            NCTestUtils.assertValueAt(-32768, 0, 0, array);
            NCTestUtils.assertValueAt(-32768, 1, 0, array);
            NCTestUtils.assertValueAt(8151, 2, 0, array);
            NCTestUtils.assertValueAt(8122, 3, 0, array);
            NCTestUtils.assertValueAt(8096, 4, 0, array);

            NCTestUtils.assertValueAt(-32768, 0, 7, array);
            NCTestUtils.assertValueAt(-32768, 1, 7, array);
            NCTestUtils.assertValueAt(8135, 2, 7, array);
            NCTestUtils.assertValueAt(8107, 3, 7, array);
            NCTestUtils.assertValueAt(8085, 4, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_rightWindowOut_NOAA17() throws Exception {
        final File avhrrNOAA17File = createAvhrrNOAA17File();
        reader.open(avhrrNOAA17File);

        try {
            final Array array = reader.readRaw(407, 241, new Interval(9, 9), "ch1");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            NCTestUtils.assertValueAt(5, 4, 0, array);
            NCTestUtils.assertValueAt(5, 5, 0, array);
            NCTestUtils.assertValueAt(-32768, 6, 0, array);
            NCTestUtils.assertValueAt(-32768, 7, 0, array);
            NCTestUtils.assertValueAt(-32768, 8, 0, array);

            NCTestUtils.assertValueAt(5, 4, 7, array);
            NCTestUtils.assertValueAt(5, 5, 7, array);
            NCTestUtils.assertValueAt(-32768, 6, 7, array);
            NCTestUtils.assertValueAt(-32768, 7, 7, array);
            NCTestUtils.assertValueAt(-32768, 8, 7, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_topLeftWindowOut_NOAA18() throws Exception {
        final File avhrrNOAA18Path = createAvhrrNOAA18File();
        reader.open(avhrrNOAA18Path);

        try {
            final Array array = reader.readRaw(2, 3, new Interval(9, 9), "lat");
            assertNotNull(array);
            assertEquals(81, array.getSize());

            NCTestUtils.assertValueAt(-32768.0, 0, 0, array);
            NCTestUtils.assertValueAt(-32768.0, 4, 0, array);
            NCTestUtils.assertValueAt(-32768.0, 8, 0, array);

            NCTestUtils.assertValueAt(-32768.0, 0, 1, array);
            NCTestUtils.assertValueAt(-32768.0, 1, 1, array);
            NCTestUtils.assertValueAt(62.51599884033203, 2, 1, array);

            NCTestUtils.assertValueAt(-32768.0, 0, 2, array);
            NCTestUtils.assertValueAt(-32768.0, 1, 2, array);
            NCTestUtils.assertValueAt(62.54399871826172, 2, 2, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadRaw_bottomRightWindowOut_NOAA19() throws Exception {
        final File avhrrNOAA19File = createAvhrrNOAA19File();
        reader.open(avhrrNOAA19File);

        try {
            Array array = reader.readRaw(405, 12928, new Interval(9, 9), "cloud_mask");
            assertNotNull(array);

            NCTestUtils.assertValueAt(1, 5, 7, array);
            NCTestUtils.assertValueAt(1, 6, 7, array);
            NCTestUtils.assertValueAt(0, 7, 7, array);
            NCTestUtils.assertValueAt(-128, 8, 7, array);

            NCTestUtils.assertValueAt(1, 5, 8, array);
            NCTestUtils.assertValueAt(1, 6, 8, array);
            NCTestUtils.assertValueAt(1, 7, 8, array);
            NCTestUtils.assertValueAt(-128, 8, 8, array);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReadScaled_scalingAndOffset() throws IOException, InvalidRangeException {
        final File avhrrNOAA10File = createAvhrrNOAA10File();
        reader.open(avhrrNOAA10File);

        try {
            final Array array = reader.readScaled(92, 7327, new Interval(3, 3), "cloud_probability");
            assertNotNull(array);

            NCTestUtils.assertValueAt(0.9566919934004545, 0, 0, array);
            NCTestUtils.assertValueAt(0.9488179935142398, 1, 0, array);
            NCTestUtils.assertValueAt(0.9763769931159914, 2, 0, array);

            NCTestUtils.assertValueAt(0.9842509930022061, 0, 1, array);
            NCTestUtils.assertValueAt(0.8858259944245219, 1, 1, array);
            NCTestUtils.assertValueAt(0.9763769931159914, 2, 1, array);

            NCTestUtils.assertValueAt(0.9960619928315282, 0, 2, array);
            NCTestUtils.assertValueAt(0.9881879929453135, 1, 2, array);
            NCTestUtils.assertValueAt(0.972439993172884, 2, 2, array);
        } finally {
            reader.close();
        }
    }
//
//    @Test
//    public void testReadScaled_onlyScaling_onePixel() throws IOException, InvalidRangeException {
//        final File avhrrNOAA18Path = createAvhrrNOAA18File();
//        reader.open(avhrrNOAA18Path);
//
//        try {
//            final Array array = reader.readScaled(80, 4601, new Interval(1, 1), "satellite_zenith_angle");
//            assertNotNull(array);
//            assertEquals(1, array.getSize());
//
//            assertEquals(39.19999912381172, array.getDouble(0), 1e-8);
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testReadScaled_onlyScaling() throws IOException, InvalidRangeException {
//        final File avhrrNOAA18Path = createAvhrrNOAA18File();
//        reader.open(avhrrNOAA18Path);
//
//        try {
//            final Array array = reader.readScaled(115, 8081, new Interval(3, 3), "ch2");
//            assertNotNull(array);
//
//            NCTestUtils.assertValueAt(0.014999999621068127, 0, 0, array);
//            NCTestUtils.assertValueAt(0.010699999729695264, 1, 0, array);
//            NCTestUtils.assertValueAt(0.008899999775167089, 2, 0, array);
//
//            NCTestUtils.assertValueAt(0.01129999971453799, 0, 1, array);
//            NCTestUtils.assertValueAt(0.008899999775167089, 1, 1, array);
//            NCTestUtils.assertValueAt(0.008899999775167089, 2, 1, array);
//
//            NCTestUtils.assertValueAt(0.017399999560439028, 0, 2, array);
//            NCTestUtils.assertValueAt(0.008899999775167089, 1, 2, array);
//            NCTestUtils.assertValueAt(0.010699999729695264, 2, 2, array);
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testReadScaled_noScale_noOffset() throws IOException, InvalidRangeException {
//        final File avhrrNOAA18Path = createAvhrrNOAA18File();
//        reader.open(avhrrNOAA18Path);
//
//        try {
//            final Array array = reader.readScaled(356, 12234, new Interval(3, 3), "qual_flags");
//            assertNotNull(array);
//
//            NCTestUtils.assertValueAt(17.0, 0, 0, array);
//            NCTestUtils.assertValueAt(17.0, 1, 0, array);
//            NCTestUtils.assertValueAt(17.0, 2, 0, array);
//
//            NCTestUtils.assertValueAt(17.0, 0, 1, array);
//            NCTestUtils.assertValueAt(17.0, 1, 1, array);
//            NCTestUtils.assertValueAt(17.0, 2, 1, array);
//
//            NCTestUtils.assertValueAt(18.0, 0, 2, array);
//            NCTestUtils.assertValueAt(18.0, 1, 2, array);
//            NCTestUtils.assertValueAt(18.0, 2, 2, array);
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testReadRaw_topRightWindowOut() throws Exception {
//        final File avhrrNOAA18Path = createAvhrrNOAA18File();
//
//        try {
//            reader.open(avhrrNOAA18Path);
//            Array array = reader.readRaw(407, 3, new Interval(9, 9), "relative_azimuth_angle");
//            assertNotNull(array);
//
//            NCTestUtils.assertValueAt(-32768.0, 4, 0, array);
//            NCTestUtils.assertValueAt(-32768.0, 5, 0, array);
//            NCTestUtils.assertValueAt(-32768.0, 6, 0, array);
//            NCTestUtils.assertValueAt(-32768.0, 7, 0, array);
//            NCTestUtils.assertValueAt(-32768.0, 8, 0, array);
//
//            NCTestUtils.assertValueAt(-11094.0, 4, 1, array);
//            NCTestUtils.assertValueAt(-11089.0, 5, 1, array);
//            NCTestUtils.assertValueAt(-32768.0, 6, 1, array);
//            NCTestUtils.assertValueAt(-32768.0, 7, 1, array);
//            NCTestUtils.assertValueAt(-32768.0, 8, 1, array);
//
//            NCTestUtils.assertValueAt(-11101.0, 4, 8, array);
//            NCTestUtils.assertValueAt(-11098.0, 5, 8, array);
//            NCTestUtils.assertValueAt(-32768.0, 6, 8, array);
//            NCTestUtils.assertValueAt(-32768.0, 7, 8, array);
//            NCTestUtils.assertValueAt(-32768.0, 8, 8, array);
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testGetProductSize_NOAA18() throws Exception {
//        final File avhrrNOAA18Path = createAvhrrNOAA18File();
//
//        try {
//            reader.open(avhrrNOAA18Path);
//
//            final Dimension productSize = reader.getProductSize();
//            assertNotNull(productSize);
//            assertEquals(409, productSize.getNx());
//            assertEquals(12239, productSize.getNy());
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testGetProductSize_NOAA17() throws Exception {
//        final File avhrrNOAA17Path = createAvhrrNOAA10File();
//
//        try {
//            reader.open(avhrrNOAA17Path);
//
//            final Dimension productSize = reader.getProductSize();
//            assertNotNull(productSize);
//            assertEquals(409, productSize.getNx());
//            assertEquals(13670, productSize.getNy());
//        } finally {
//            reader.close();
//        }
//    }
//
//    @Test
//    public void testGetPixelLocator_NOAA17() throws IOException {
//        final File avhrrNOAA17Path = createAvhrrNOAA10File();
//
//        try {
//            reader.open(avhrrNOAA17Path);
//            final PixelLocator pixelLocator = reader.getPixelLocator();
//            assertNotNull(pixelLocator);
//
//            final Point2D[] pixelLocation = pixelLocator.getPixelLocation(-67.608, -4.931);
//            assertNotNull(pixelLocation);
//            assertEquals(1, pixelLocation.length);
//
//            assertEquals(3.4619535609602408, pixelLocation[0].getX(), 1e-8);
//            assertEquals(14.308903527451893, pixelLocation[0].getY(), 1e-8);
//
//            final Point2D geoLocation = pixelLocator.getGeoLocation(3.5, 14.5, null);
//            assertNotNull(geoLocation);
//            assertEquals(-67.60800170898438, geoLocation.getX(), 1e-8);
//            assertEquals(-4.931000232696533, geoLocation.getY(), 1e-8);
//        } finally {
//            reader.close();
//        }
//    }

    private File createAvhrrNOAA10File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n10", "v01.3", "1988", "03", "18", "19880318000900-ESACCI-L1C-AVHRR10_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA11File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n11", "v01.3", "1991", "05", "09", "19910509075100-ESACCI-L1C-AVHRR11_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA17File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n17", "v01.3", "2004", "10", "25", "20041025114400-ESACCI-L1C-AVHRR17_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA18File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n18", "v01.3", "2013", "02", "03", "20130203182600-ESACCI-L1C-AVHRR18_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }

    private File createAvhrrNOAA19File() {
        final String testFilePath = TestUtil.assembleFileSystemPath(new String[]{"avhrr-n19", "v01.3", "2015", "11", "06", "20151106225800-ESACCI-L1C-AVHRR19_G-fv01.0.nc"}, false);
        final File file = new File(testDataDirectory, testFilePath);
        assertTrue(file.isFile());
        return file;
    }
}
