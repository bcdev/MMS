package com.bc.fiduceo.post.util;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

public class PPUtils {

    public static void convertToFitTheRangeMinus180to180(Array lonArray) {
        final IndexIterator indexIterator = lonArray.getIndexIterator();
        while (indexIterator.hasNext()) {
            double lonD = indexIterator.getDoubleNext();
            if (Double.isFinite(lonD)) {
                while (lonD > 180) {
                    lonD -= 360;
                }
                while (lonD < -180) {
                    lonD += 360;
                }
                indexIterator.setDoubleCurrent(lonD);
            }
        }
    }

}
