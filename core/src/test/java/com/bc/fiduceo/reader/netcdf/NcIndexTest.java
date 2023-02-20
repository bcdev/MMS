package com.bc.fiduceo.reader.netcdf;

import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings({"SimplifiableAssertion", "EqualsWithItself"})
public class NcIndexTest {

    @Test
    public void testCreate() {
        NcIndex index = NcIndex.create(1);
        assertEquals(1, index.getRank());
        assertEquals(-1, index.get(0));

        index = NcIndex.create(2);
        assertEquals(2, index.getRank());
        assertEquals(-1, index.get(0));
        assertEquals(-1, index.get(1));

        index = NcIndex.create(3);
        assertEquals(3, index.getRank());
        assertEquals(-1, index.get(0));
        assertEquals(-1, index.get(1));
        assertEquals(-1, index.get(2));

        index = NcIndex.create(4);
        assertEquals(4, index.getRank());
        assertEquals(-1, index.get(0));
        assertEquals(-1, index.get(1));
        assertEquals(-1, index.get(2));
        assertEquals(-1, index.get(3));
    }

    @Test
    public void testCreate_invalidNumberOfDimensions() {
        try {
            NcIndex.create(0);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected) {
        }

        try {
            NcIndex.create(5);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSetDimGet() {
        NcIndex index = NcIndex.create(1);

        index.setDim(0, 78);
        assertEquals(78, index.get(0));

        index = NcIndex.create(2);

        index.setDim(0, 79);
        index.setDim(1, 80);
        assertEquals(79, index.get(0));
        assertEquals(80, index.get(1));

        index = NcIndex.create(3);

        index.setDim(0, 81);
        index.setDim(1, 82);
        index.setDim(2, 83);
        assertEquals(81, index.get(0));
        assertEquals(82, index.get(1));
        assertEquals(83, index.get(2));

        index = NcIndex.create(4);

        index.setDim(0, 84);
        index.setDim(1, 85);
        index.setDim(2, 86);
        index.setDim(3, 87);
        assertEquals(84, index.get(0));
        assertEquals(85, index.get(1));
        assertEquals(86, index.get(2));
        assertEquals(87, index.get(3));
    }

    @Test
    public void testSetDims_invalid_dimension() {
        final NcIndex index = NcIndex.create(3);
        try {
            index.setDim(-1, 22);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected) {
        }

        try {
            index.setDim(3, 23);
            fail("IllegalArgumentException expected");
        } catch(IllegalArgumentException expected) {
        }
    }

    @Test
    public void testEquals_1D() {
        final NcIndex idx_1 = NcIndex.create(1);
        final NcIndex idx_2 = NcIndex.create(1);

        assertTrue(idx_1.equals(idx_1));
        assertTrue(idx_1.equals(idx_2));
        assertTrue(idx_2.equals(idx_1));

        idx_1.setDim(0, 6);
        idx_2.setDim(0, 7);

        assertFalse(idx_1.equals(idx_2));

        idx_1.setDim(0, 11);
        idx_2.setDim(0, 11);
        assertTrue(idx_2.equals(idx_1));
        assertTrue(idx_1.equals(idx_2));
    }

    @Test
    public void testEquals_3D() {
        final NcIndex idx_1 = NcIndex.create(3);
        final NcIndex idx_2 = NcIndex.create(3);

        assertTrue(idx_1.equals(idx_1));
        assertTrue(idx_1.equals(idx_2));
        assertTrue(idx_2.equals(idx_1));

        idx_1.setDim(0, 6);
        idx_1.setDim(1, 6);
        idx_1.setDim(2, 6);
        idx_2.setDim(0, 7);
        idx_2.setDim(1, 6);
        idx_2.setDim(2, 8);

        assertFalse(idx_1.equals(idx_2));

        idx_1.setDim(0, 11);
        idx_1.setDim(1, 11);
        idx_1.setDim(2, 11);
        idx_2.setDim(0, 11);
        idx_2.setDim(1, 11);
        idx_2.setDim(2, 11);
        assertTrue(idx_2.equals(idx_1));
        assertTrue(idx_1.equals(idx_2));
    }

    @Test
    public void testEquals_1D_invalid_class() {
        final NcIndex idx_1 = NcIndex.create(1);

        assertFalse(idx_1.equals(new Integer(idx_1.get(0))));
    }

    @Test
    public void testEquals_different_dimensions() {
        final NcIndex idx_1 = NcIndex.create(2);
        final NcIndex idx_2 = NcIndex.create(3);

        assertFalse(idx_1.equals(idx_2));
        assertFalse(idx_2.equals(idx_1));
    }
}
