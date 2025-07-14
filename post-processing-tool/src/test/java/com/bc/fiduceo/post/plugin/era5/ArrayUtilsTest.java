package com.bc.fiduceo.post.plugin.era5;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

import static org.junit.Assert.*;

public class ArrayUtilsTest {

    @Test
    public void testMergeALongX_3D() {
        final int[] dataLeft = new int[]{0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                // ------------
                12, 13, 14, 15,
                16, 17, 18, 19,
                20, 21, 22, 23};
        final int[] shapeLeft = new int[]{2, 3, 4};

        final Array left = Array.factory(DataType.INT, shapeLeft, dataLeft);

        final int[] dataRight = new int[]{100, 101, 102,
                104, 105, 106,
                108, 109, 110,
                // ----------
                112, 113, 114,
                116, 117, 118,
                120, 121, 122};
        final int[] shapeRight = new int[]{2, 3, 3};
        final Array right = Array.factory(DataType.INT, shapeRight, dataRight);

        final Array merged = ArrayUtils.mergeAlongX(left, right);
        int[] shape = merged.getShape();
        assertArrayEquals(new int[]{2, 3, 7}, shape);

        final Index index = merged.getIndex();
        index.set(0, 0, 0);
        assertEquals(0, merged.getInt(index));
        index.set(0, 0, 3);
        assertEquals(3, merged.getInt(index));
        index.set(0, 0, 4);
        assertEquals(100, merged.getInt(index));
        index.set(0, 0, 6);
        assertEquals(102, merged.getInt(index));

        index.set(1, 2, 0);
        assertEquals(20, merged.getInt(index));
        index.set(1, 2, 3);
        assertEquals(23, merged.getInt(index));
        index.set(1, 2, 4);
        assertEquals(120, merged.getInt(index));
        index.set(1, 2, 6);
        assertEquals(122, merged.getInt(index));
    }

    @Test
    public void testMergeALongX_4D() {
        final int[] dataLeft = new int[]{0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                // ------------
                12, 13, 14, 15,
                16, 17, 18, 19,
                20, 21, 22, 23,
                // ============
                30, 31, 32, 33,
                34, 35, 36, 37,
                38, 39, 40, 41,
                // ------------
                42, 43, 44, 45,
                46, 47, 48, 49,
                50, 51, 52, 53};
        final int[] shapeLeft = new int[]{2, 2, 3, 4};

        final Array left = Array.factory(DataType.INT, shapeLeft, dataLeft);

        final int[] dataRight = new int[]{100, 101, 102,
                104, 105, 106,
                108, 109, 110,
                // ----------
                112, 113, 114,
                116, 117, 118,
                120, 121, 122,
                // ==========
                200, 201, 202,
                204, 205, 206,
                208, 209, 210,
                // ----------
                212, 213, 214,
                216, 217, 218,
                220, 221, 222};
        final int[] shapeRight = new int[]{2, 2, 3, 3};
        final Array right = Array.factory(DataType.INT, shapeRight, dataRight);

        final Array merged = ArrayUtils.mergeAlongX(left, right);
        int[] shape = merged.getShape();
        assertArrayEquals(new int[]{2, 2, 3, 7}, shape);

        final Index index = merged.getIndex();
        index.set(0, 0, 0, 0);
        assertEquals(0, merged.getInt(index));
        index.set(0, 0, 0, 3);
        assertEquals(3, merged.getInt(index));
        index.set(0, 0, 0, 4);
        assertEquals(100, merged.getInt(index));
        index.set(0, 0, 0, 6);
        assertEquals(102, merged.getInt(index));

        index.set(1, 1, 2, 0);
        assertEquals(50, merged.getInt(index));
        index.set(1, 1, 2, 3);
        assertEquals(53, merged.getInt(index));
        index.set(1, 1, 2, 4);
        assertEquals(220, merged.getInt(index));
        index.set(1, 1, 2, 6);
        assertEquals(222, merged.getInt(index));
    }

    @Test
    public void testMergeALongX_invalidDimension() {
        Array left = Array.factory(DataType.FLOAT, new int[]{4, 6});
        Array right = Array.factory(DataType.FLOAT, new int[]{4, 6});

        try {
            ArrayUtils.mergeAlongX(left, right);
         fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }

        left = Array.factory(DataType.FLOAT, new int[]{2, 2, 2, 4, 6});
        right = Array.factory(DataType.FLOAT, new int[]{2, 2, 2, 4, 6});

        try {
            ArrayUtils.mergeAlongX(left, right);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
