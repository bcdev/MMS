package com.bc.fiduceo.reader.netcdf;

import java.util.stream.IntStream;

public class NcIndex {

    private static final int MAX_DIMS = 4;

    private final int[] offsets;

    private NcIndex(int dimensions) {
        offsets = new int[dimensions];
        IntStream.range(0, dimensions).forEach(i -> offsets[i] = -1);
    }

    public static NcIndex create(int dimensions) {
        if (dimensions <= 0 || dimensions > MAX_DIMS) {
            throw new IllegalArgumentException("invalid number of dimensions: " + dimensions);
        }
        return new NcIndex(dimensions);
    }

    public int get(int dimension) {
        return offsets[dimension];
    }

    public int getRank() {
        return offsets.length;
    }

    public void setDim(int dimension, int value) {
        if (dimension < 0 || dimension > offsets.length - 1) {
            throw new IllegalArgumentException("invalid dimension index: " + dimension);
        }
        offsets[dimension] = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final NcIndex other = (NcIndex) obj;
        if (other.getRank() != offsets.length) {
            return false;
        }
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] != other.get(i)) {
                return false;
            }
        }
        return true;
    }
}
