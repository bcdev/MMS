package com.bc.fiduceo.post.plugin.era5;

class InterpolationContext {

    private final BilinearInterpolator[][] interpolators;
    private final int width;
    private final int height;

    InterpolationContext(int x, int y) {
        width = x;
        height = y;
        interpolators = new BilinearInterpolator[y][x];
    }

    BilinearInterpolator get(int x, int y) {
        checkBoundaries(x, y);
        return interpolators[y][x];
    }

    public void set(int x, int y, BilinearInterpolator interpolator) {
        checkBoundaries(x, y);
        interpolators[y][x] = interpolator;
    }

    private void checkBoundaries(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Access interpolator out of raster: " + x + ", " + y);
        }
    }

    public int[] getMinMaxY() {
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        for (BilinearInterpolator[] bilinearInterpolators : interpolators) {
            for (BilinearInterpolator interpolator : bilinearInterpolators) {
                if (interpolator==null) continue;
                final int min = interpolator.getYMin();
                final int max = min + 1;
                if (min < yMin) {
                    yMin = min;
                }
                if (max > yMax) {
                    yMax = max;
                }
            }
        }
        return new int[]{yMin, yMax};
    }
}
