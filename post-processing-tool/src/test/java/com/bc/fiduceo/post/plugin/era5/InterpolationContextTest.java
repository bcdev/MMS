package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;

import java.awt.*;

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
    public void testGetMinMaxY() {
        final InterpolationContext interpolationContext = new InterpolationContext(3, 3);
        interpolationContext.set(0, 0, new BilinearInterpolator(0, 0, 0, 121));
//        interpolationContext.set(1, 0, new BilinearInterpolator(0, 0, 0, 128));
        interpolationContext.set(2, 0, new BilinearInterpolator(0, 0, 0, 118));
        interpolationContext.set(0, 1, new BilinearInterpolator(0, 0, 0, 313));
        interpolationContext.set(1, 1, new BilinearInterpolator(0, 0, 0, 215));
//        interpolationContext.set(2, 1, new BilinearInterpolator(0, 0, 0, 256));
        interpolationContext.set(0, 2, new BilinearInterpolator(0, 0, 0, 244));
//        interpolationContext.set(1, 2, new BilinearInterpolator(0, 0, 0, 170));
        interpolationContext.set(2, 2, new BilinearInterpolator(0, 0, 0, 199));

        assertArrayEquals(new int[]{118, 314}, interpolationContext.getMinMaxY());
    }
}
