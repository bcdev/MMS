package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.core.IntRange;

import static com.bc.fiduceo.post.plugin.era5.Era5PostProcessing.DATA_ARRAY_WIDTH;

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

    void setXRanges(IntRange[] xRanges) {
        this.xRanges = xRanges;
        initialize();
    }

    private void initialize() {
        int activeXRange = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final BilinearInterpolator interpolator = interpolators[y][x];
                if (interpolator != null) {
                    int xMin = interpolator.getXMin();
                    int xMax = (xMin + 1) % DATA_ARRAY_WIDTH;

                    if (xMax == 0 && xMin > xMax && activeXRange == 0) {
                        // we wrap around the antimeridian
                        xRanges[activeXRange].setMax(1439);
                        activeXRange++;
                        xRanges[activeXRange] = new IntRange(0, 1);
                    } else {
                        if (xRanges[activeXRange].getMin() > xMin) {
                            xRanges[activeXRange].setMin(xMin);
                        }
                        if (xRanges[activeXRange].getMax() < xMax) {
                            xRanges[activeXRange].setMax(xMax);
                        }
                    }

                    int yMin = interpolator.getYMin();
                    int yMax = yMin + 1;
                    if (yRange.getMin() > yMin) {
                        yRange.setMin(yMin);
                    }
                    if (yRange.getMax() < yMax) {
                        yRange.setMax(yMax);
                    }
                }
            }
        }

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
