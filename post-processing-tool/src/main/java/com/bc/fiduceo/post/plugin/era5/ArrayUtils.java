package com.bc.fiduceo.post.plugin.era5;

import ucar.ma2.Array;

class ArrayUtils {

    static Array mergeAlongX(Array left, Array right) {
        final int[] leftShape = left.getShape();
        final int[] rightShape = right.getShape();
        final int rank = left.getRank();

        final Object leftStorage = left.getStorage();
        final Object rightStorage = right.getStorage();

        Array targetArray;
        if (rank == 3) {
            final int width = leftShape[2] + rightShape[2];
            final int[] targetShape = new int[]{leftShape[0], leftShape[1], width};
            targetArray = Array.factory(left.getDataType(), targetShape);
            final Object targetStorage = targetArray.getStorage();

            int srcOffsetLeft = 0;
            int srcOffsetRight = 0;
            int destOffset = 0;
            for (int z = 0; z < leftShape[0]; z++) {
                for (int y = 0; y < leftShape[1]; y++) {
                    System.arraycopy(leftStorage, srcOffsetLeft, targetStorage, destOffset, leftShape[2]);
                    destOffset += leftShape[2];
                    srcOffsetLeft += leftShape[2];
                    System.arraycopy(rightStorage, srcOffsetRight, targetStorage, destOffset, rightShape[2]);
                    srcOffsetRight += rightShape[2];
                    destOffset += rightShape[2];
                }
            }
        } else if (rank == 4) {
            final int width = leftShape[3] + rightShape[3];
            final int[] targetShape = new int[]{leftShape[0], leftShape[1], leftShape[2], width};
            targetArray = Array.factory(left.getDataType(), targetShape);
            final Object targetStorage = targetArray.getStorage();

            int srcOffsetLeft = 0;
            int srcOffsetRight = 0;
            int destOffset = 0;
            for (int layer = 0; layer < leftShape[0]; layer++) {
                for (int z = 0; z < leftShape[1]; z++) {
                    for (int y = 0; y < leftShape[2]; y++) {
                        System.arraycopy(leftStorage, srcOffsetLeft, targetStorage, destOffset, leftShape[3]);
                        destOffset += leftShape[3];
                        srcOffsetLeft += leftShape[3];
                        System.arraycopy(rightStorage, srcOffsetRight, targetStorage, destOffset, rightShape[3]);
                        srcOffsetRight += rightShape[3];
                        destOffset += rightShape[3];
                    }
                }
            }

        } else {
            throw new IllegalArgumentException("invalid input data rank");
        }
        return targetArray;
    }
}
