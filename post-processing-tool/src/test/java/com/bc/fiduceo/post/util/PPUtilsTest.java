package com.bc.fiduceo.post.util;

import org.junit.Test;
import ucar.ma2.Array;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.*;

public class PPUtilsTest {

    @Test
    public void convertToFitTheRangeMinus180to180() {
        final int farOutside = 360 * 200;

        final double[] lons = {
                -180,
                -179.999999999,
                 179.999999999,
                 180,
                -190,
                 190,
                -30 - farOutside,
                 40 + farOutside,
                Double.NaN,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY
        };

        final Array lonArray = Array.makeFromJavaArray(lons);
        PPUtils.convertToFitTheRangeMinus180to180(lonArray);

        final double[] expected = {
                /* Before:  -180                       after -> */   -180,
                /* Before:  -179.999999999             after -> */   -179.999999999,
                /* Before:   179.999999999             after -> */    179.999999999,
                /* Before:   180                       after -> */    180,
                /* Before:  -190                       after -> */    170,
                /* Before:   190                       after -> */   -170,
                /* Before:  -30 - farOutside           after -> */   -30,
                /* Before:   40 + farOutside           after -> */    40,
                /* Before:  Double.NaN                 after -> */   Double.NaN,
                /* Before:  Double.NEGATIVE_INFINITY   after -> */   Double.NEGATIVE_INFINITY,
                /* Before:  Double.POSITIVE_INFINITY   after -> */   Double.POSITIVE_INFINITY
        };

        assertArrayEquals(expected, (double[]) lonArray.getStorage(), 0.000001);
    }
}