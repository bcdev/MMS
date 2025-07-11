package com.bc.fiduceo.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntRangeTest {

    @Test
    public void testConstruction() {
        final IntRange intRange = new IntRange(2, 7);

        assertEquals(2, intRange.getMin());
        assertEquals(7, intRange.getMax());
    }

    @Test
    public void testDefaultConstruction() {
        final IntRange intRange = new IntRange();

        assertEquals(Integer.MAX_VALUE, intRange.getMin());
        assertEquals(Integer.MIN_VALUE, intRange.getMax());
    }

    @Test
    public void testSetGetMinMax() {
        final IntRange intRange = new IntRange(4, 8);

        assertEquals(4, intRange.getMin());
        intRange.setMin(5);
        assertEquals(5, intRange.getMin());

        assertEquals(8, intRange.getMax());
        intRange.setMax(6);
        assertEquals(6, intRange.getMax());
    }

    @Test
    public void testGetLength() {
        final IntRange intRange = new IntRange(3, 8);

        assertEquals(6, intRange.getLength());
    }

    @Test
    public void testContains() {
        final IntRange intRange = new IntRange(4, 9);

        assertFalse(intRange.contains(3));
        assertTrue(intRange.contains(4));
        assertTrue(intRange.contains(9));
        assertFalse(intRange.contains(10));
    }
}
