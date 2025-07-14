package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.core.IntRange;

import java.util.ArrayList;
import java.util.List;

class InterpolationContext {

    private final BilinearInterpolator[][] interpolators;
    private final int width;
    private final int height;
    private final IntRange yRange;
    private IntRange[] xRanges;
    private boolean mustInitialise;

    InterpolationContext(int width, int height) {
        this.width = width;
        this.height = height;
        interpolators = new BilinearInterpolator[height][width];
        yRange = new IntRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
        mustInitialise = true;
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

    public IntRange[] getXRanges() {
        if (mustInitialise) {
            initialize();
        }
        return xRanges;
    }

    private void initialize() {
        final List<IntRange> ranges = new ArrayList<>();
        IntRange current = new IntRange();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (interpolators[y][x] != null) {
                    int min = interpolators[y][x].getXMin();
                    int max = (min + 1) % 1440;

                    if (max == 0 && min > 0) {
                        // we wrap around antimeridian
                        // finish current range with min = 1439
                        current.setMax(min);
                        ranges.add(current);
                        // start new range with xmin = xmax = 0
                        current = new IntRange(0, 0);
                    }

                    if (current.getMin() > min) {
                        current.setMin(min);
                    }
                    if (current.getMax() < max) {
                        current.setMax(max);
                    }

                    min = interpolators[y][x].getYMin();
                    max = min + 1;
                    if (yRange.getMin() > min) {
                        yRange.setMin(min);
                    }
                    if (yRange.getMax() < max) {
                        yRange.setMax(max);
                    }
                }
            }
        }
        ranges.add(current);
        xRanges = ranges.toArray(new IntRange[0]);

        setRelativeInterpolatorOffsets();

        mustInitialise = false;
    }

    private void setRelativeInterpolatorOffsets() {
        final int yMinValue = yRange.getMin();


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final BilinearInterpolator interpolator = interpolators[y][x];
                if (interpolator == null) {
                    continue;
                }

                interpolator.setRelYMin(interpolator.getYMin() - yMinValue);

                int xMin = interpolator.getXMin();
                if (xRanges[0].contains(xMin)) {
                    int xMinValue = xRanges[0].getMin();
                    interpolator.setRelXMin(interpolator.getXMin() - xMinValue);
                } else if (xRanges.length > 1) {
                    final int xOffset = xRanges[0].getLength();
                    if (xRanges[1].contains(xMin)) {
                        int xMinValue = xRanges[1].getMin();
                        interpolator.setRelXMin(interpolator.getXMin() - xMinValue + xOffset);
                    }
                }
            }
        }
    }

    public IntRange getYRange() {
        if (mustInitialise) {
            initialize();
        }
        return yRange;
    }
}
