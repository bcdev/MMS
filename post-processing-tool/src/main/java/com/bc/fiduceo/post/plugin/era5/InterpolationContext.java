package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.core.IntRange;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.post.plugin.era5.Era5PostProcessing.DATA_ARRAY_WIDTH;

class InterpolationContext {

    private static final int EAST = 0;
    private static final int WEST = 1;

    private final BilinearInterpolator[][] interpolators;
    private final int width;
    private final int height;
    private final IntRange yRange;
    private final IntRange[] xRanges;
    private boolean mustInitialise;

    InterpolationContext(int width, int height) {
        this.width = width;
        this.height = height;
        interpolators = new BilinearInterpolator[height][width];
        yRange = new IntRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
        xRanges = new IntRange[2];
        xRanges[0] = new IntRange(Integer.MAX_VALUE, Integer.MIN_VALUE);
        xRanges[1] = null;
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
        final List<Integer> xStartPositions = new ArrayList<>();
        final List<Integer> yStartPositions = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final BilinearInterpolator interpolator = interpolators[y][x];
                if (interpolator != null) {
                    xStartPositions.add(interpolator.getXMin());
                    yStartPositions.add(interpolator.getYMin());
                }
            }
        }

        if (xStartPositions.isEmpty() || yStartPositions.isEmpty()) {
            return;
        }

        xStartPositions.sort(Integer::compare);
        final int largestX = xStartPositions.get(xStartPositions.size() - 1);
        if (largestX == DATA_ARRAY_WIDTH - 1) {
            // we are overlapping antimeridian, split xPositions int two groups, one east, one west
            xRanges[WEST] = new IntRange();
            xRanges[WEST].setMin(0);    // western section starts at anti-meridian
            xRanges[EAST].setMax(DATA_ARRAY_WIDTH - 1); // eastern section ends at anti-meridian
            int activeRange = WEST;
            boolean switchRange = false;
            for (int i = 0; i < xStartPositions.size() - 1; i++) {
                final int currentX = xStartPositions.get(i);
                final int delta = xStartPositions.get(i + 1) - currentX;
                if (delta > 1000) {
                    switchRange = true;
                }

                if (activeRange == WEST) {
                    if (currentX > xRanges[WEST].getMax()) {
                        xRanges[WEST].setMax(currentX);
                    }
                }
                if (activeRange == EAST) {
                    if (currentX < xRanges[EAST].getMin()) {
                        xRanges[EAST].setMin(currentX);
                    }
                }

                if (switchRange) {
                    activeRange = EAST;
                    switchRange = false;
                }
            }
            // don't forget to increase. All x-es are minimum values, we need one x more to be able to interpolate
            xRanges[WEST].setMax(xRanges[WEST].getMax() + 1);
        } else {
            xRanges[0].setMin(xStartPositions.get(0));
            xRanges[0].setMax(xStartPositions.get(xStartPositions.size() - 1) + 1);
            xRanges[1] = null;
        }

        yStartPositions.sort(Integer::compareTo);
        yRange.setMin(yStartPositions.get(0));
        yRange.setMax(yStartPositions.get(yStartPositions.size() - 1) + 1);

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
