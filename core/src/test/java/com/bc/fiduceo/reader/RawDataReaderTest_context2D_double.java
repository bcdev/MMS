package com.bc.fiduceo.reader;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.util.NetCDFUtils;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;

import java.io.IOException;

import static org.junit.Assert.*;

public class RawDataReaderTest_context2D_double {

    private Interval windowSize;
    private Number fillValue;
    private double fv;
    private Array rawArray;

    @Before
    public void setUp() throws Exception {
        windowSize = new Interval(3, 3);
        fillValue = -2;
        fv = fillValue.doubleValue();
        rawArray = getDoubleRawArray();
    }

    @Test
    public void testWindowCenter() throws Exception {
        final Array array = RawDataReader.read(3, 3, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {22, 32, 42, 23, 33, 43, 24, 34, 44};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopRightWindowOut() throws Exception {
        final Array array = RawDataReader.read(9, 0, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, fv, fv, 80, 90, fv, 81, 91, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testTopLeftWindowOut() throws Exception {
        final Array array = RawDataReader.read(0, 0, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, fv, fv, fv, 0, 10, fv, 1, 11};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomLeftWindowOut() throws Exception {
        final Array array = RawDataReader.read(0, 9, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {fv, 8, 18, fv, 9, 19, fv, fv, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testBottomRightWindowOut() throws Exception {
        final Array array = RawDataReader.read(9, 9, windowSize, fillValue, rawArray, new Dimension("size", 10, 0));

        assertNotNull(array);
        assertEquals(double.class, array.getElementType());
        assertEquals(9, array.getSize());
        final double[] expecteds = {88, 98, fv, 89, 99, fv, fv, fv, fv};
        final double[] actuals = (double[]) array.get1DJavaArray(array.getElementType());
        assertArrayEquals(expecteds, actuals, 1e-8);
    }

    @Test
    public void testRawArrayHasMoreThanTwoDimensions() throws IOException {
        final Array rawArray = Array.makeFromJavaArray(new double[][][]{
                {{11, 12, 13}, {14, 15, 16}, {17, 18, 19},},
                {{21, 22, 23}, {24, 25, 26}, {27, 28, 29},},
                {{31, 32, 33}, {34, 35, 36}, {37, 38, 39},}
        });

        try {
            RawDataReader.read(1, 1, new Interval(3, 3), -4d, rawArray, new Dimension("size", 10, 0));
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
        }
    }

    private Array getDoubleRawArray() {
        return NetCDFUtils.create(new double[][]{
                {0, 10, 20, 30, 40, 50, 60, 70, 80, 90},
                {1, 11, 21, 31, 41, 51, 61, 71, 81, 91},
                {2, 12, 22, 32, 42, 52, 62, 72, 82, 92},
                {3, 13, 23, 33, 43, 53, 63, 73, 83, 93},
                {4, 14, 24, 34, 44, 54, 64, 74, 84, 94},
                {5, 15, 25, 35, 45, 55, 65, 75, 85, 95},
                {6, 16, 26, 36, 46, 56, 66, 76, 86, 96},
                {7, 17, 27, 37, 47, 57, 67, 77, 87, 97},
                {8, 18, 28, 38, 48, 58, 68, 78, 88, 98},
                {9, 19, 29, 39, 49, 59, 69, 79, 89, 99}
        });

    }
}