package com.bc.fiduceo.core;

public class IntRange {

    private int min;
    private int max;

    public IntRange() {
        this(Integer.MAX_VALUE, Integer.MIN_VALUE);
    }

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getLength() {
        return max - min + 1;
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }
}
