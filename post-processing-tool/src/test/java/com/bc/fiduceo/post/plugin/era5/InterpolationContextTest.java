package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.core.IntRange;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class InterpolationContextTest {

    @Test
    public void testConstructEmpty() {
        final InterpolationContext context = new InterpolationContext(3, 5);

        assertNull(context.get(1, 2));
    }

    @Test
    public void testConstruct_setGet() {
        final InterpolationContext context = new InterpolationContext(3, 4);

        final BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 6, 7);
        context.set(0, 0, interpolator);

        assertSame(interpolator, context.get(0, 0));
    }

    @Test
    public void testSet_outOfBounds() {
        final InterpolationContext context = new InterpolationContext(4, 3);
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.4, 0.4, 5, 5);

        try {
            context.set(-1, 2, interpolator);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.set(4, 2, interpolator);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.set(1, -1, interpolator);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.set(1, 3, interpolator);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGet_outOfBounds() {
        final InterpolationContext context = new InterpolationContext(5, 3);

        try {
            context.get(-1, 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.get(5, 1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.get(2, -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            context.get(2, 3);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetXRanges_emptyContext() {
        final InterpolationContext context = new InterpolationContext(5, 3);

        final IntRange[] xRanges = context.getXRanges();
        assertEquals(1, xRanges.length);
        assertEquals(Integer.MAX_VALUE, xRanges[0].getMin());
        assertEquals(Integer.MIN_VALUE, xRanges[0].getMax());
    }

    @Test
    public void testGetXRanges_oneInterpolator() {
        final InterpolationContext context = new InterpolationContext(4, 5);

        final BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 6, 7);
        context.set(0, 0, interpolator);

        final IntRange[] xRanges = context.getXRanges();
        assertEquals(1, xRanges.length);
        assertEquals(6, xRanges[0].getMin());
        assertEquals(7, xRanges[0].getMax());

        assertEquals(0, interpolator.getRelXMin());
        assertEquals(0, interpolator.getRelYMin());
    }

    @Test
    public void testGetXRanges_threeInterpolators() {
        final InterpolationContext context = new InterpolationContext(4, 5);

        BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 6, 7);
        context.set(0, 0, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 7, 8);
        context.set(1, 1, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 8, 9);
        context.set(2, 2, interpolator);

        final IntRange[] xRanges = context.getXRanges();
        assertEquals(1, xRanges.length);
        assertEquals(6, xRanges[0].getMin());
        assertEquals(9, xRanges[0].getMax());
    }

    @Test
    public void testGetXRanges_threeInterpolators_antimeridianCase() {
        final InterpolationContext context = new InterpolationContext(4, 5);

        BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 1438, 7);
        context.set(0, 0, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 1439, 8);
        context.set(1, 1, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 0, 9);
        context.set(2, 2, interpolator);

        final IntRange[] xRanges = context.getXRanges();
        assertEquals(2, xRanges.length);
        assertEquals(1438, xRanges[0].getMin());
        assertEquals(1439, xRanges[0].getMax());

        assertEquals(0, xRanges[1].getMin());
        assertEquals(1, xRanges[1].getMax());
    }

    @Test
    public void testGetYRange_emptyContext() {
        final InterpolationContext context = new InterpolationContext(5, 3);

        final IntRange yRange = context.getYRange();
        assertEquals(Integer.MAX_VALUE, yRange.getMin());
        assertEquals(Integer.MIN_VALUE, yRange.getMax());
    }

    @Test
    public void testGetYRange_oneInterpolator() {
        final InterpolationContext context = new InterpolationContext(4, 5);

        final BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 6, 7);
        context.set(0, 0, interpolator);

        final IntRange yRange = context.getYRange();
        assertEquals(7, yRange.getMin());
        assertEquals(8, yRange.getMax());
    }

    @Test
    public void testGetYRange_threeInterpolators() {
        final InterpolationContext context = new InterpolationContext(4, 5);

        BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 6, 7);
        context.set(0, 0, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 7, 7);
        context.set(1, 0, interpolator);

        interpolator = new BilinearInterpolator(0.3, 0.5, 7, 8);
        context.set(1, 1, interpolator);

        final IntRange yRange = context.getYRange();
        assertEquals(7, yRange.getMin());
        assertEquals(9, yRange.getMax());
    }

    @Test
    public void testGetXRange() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 100, 7);

        final IntRange currentRange = new IntRange();
        final ArrayList<IntRange> xRanges = new ArrayList<>();
        IntRange xRange = InterpolationContext.getXRange(interpolator, currentRange, xRanges);
        assertEquals(100, xRange.getMin());
        assertEquals(101, xRange.getMax());

        // nothing added, we're not splitting at anti-meridian tb 2025-10-08
        assertEquals(0, xRanges.size());
    }

    @Test
    public void testGetXRange_antiMeridianCase() {
        final BilinearInterpolator interpolator = new BilinearInterpolator(0.3, 0.5, 1439, 7);

        final IntRange currentRange = new IntRange();
        final ArrayList<IntRange> xRanges = new ArrayList<>();
        IntRange xRange = InterpolationContext.getXRange(interpolator, currentRange, xRanges);
        assertEquals(0, xRange.getMin());
        assertEquals(1, xRange.getMax());

        assertEquals(1, xRanges.size());
        xRange = xRanges.get(0);
        assertEquals(1438, xRange.getMin());
        assertEquals(1439, xRange.getMax());
    }
}
