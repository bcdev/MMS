/*
 * Copyright (C) 2025 Brockmann Consult GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package com.bc.fiduceo.matchup.screening;

import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DepthScreening implements Screening {

    private static final String DEFAULT_DEPTH_NAME = "depth";
    private static final Double[] DEFAULT_LEVELS = new Double[]{0.0, 10.0, 20.0, 30.0, 50.0, 75.0, 100.0, 125.0,
            150.0, 200.0, 250.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0, 1100.0, 1200.0, 1300.0,
            1400.0, 1500.0, 1750.0, 2000.0, 2500.0, 3000.0, 3500.0, 4000.0, 4500.0, 5000.0, 5500.0};
    private final Interval singlePixel = new Interval(1, 1);
    private DepthScreening.Configuration configuration;
    private String objectiveDepthName;
    private String referenceDepthName;
    private Double[] levels;

    DepthScreening() {
        configuration = new DepthScreening.Configuration();
    }

    private static String getDepthName(String name) {
        return StringUtils.isNotNullAndNotEmpty(name) ? name : DEFAULT_DEPTH_NAME;
    }

    @Override
    public void apply(MatchupSet matchupSet,
                      Reader primaryReader,
                      Map<String, Reader> secondaryReader,
                      ScreeningContext context) throws IOException, InvalidRangeException {
        final List<SampleSet> resultSet = new ArrayList<>();
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        for (final SampleSet sampleSet : sampleSets) {
            final Reader referenceReader;
            final Reader objectiveReader;
            if (Boolean.TRUE.equals(configuration.primaryIsNominal)) {
                referenceReader = primaryReader;
                objectiveReader = secondaryReader.get(SampleSet.getOnlyOneSecondaryKey());
            } else {
                referenceReader = secondaryReader.get(SampleSet.getOnlyOneSecondaryKey());
                objectiveReader = primaryReader;
            }
            if (keep(sampleSet, referenceReader, objectiveReader)) {
                resultSet.add(sampleSet);
            }
        }
        matchupSet.setSampleSets(resultSet);
        sampleSets.clear();
    }

    void configure(DepthScreening.Configuration configuration) {
        this.configuration = configuration;
        if (Boolean.TRUE.equals(configuration.primaryIsNominal)) {
            this.objectiveDepthName = getDepthName(configuration.secondaryDepthName);
            this.referenceDepthName = getDepthName(configuration.primaryDepthName);
        } else {
            this.objectiveDepthName = getDepthName(configuration.primaryDepthName);
            this.referenceDepthName = getDepthName(configuration.secondaryDepthName);
        }
        if (configuration.levels == null) {
            this.levels = DEFAULT_LEVELS;
        }
    }

    private boolean keep(SampleSet sampleSet,
                         Reader referenceReader,
                         Reader objectiveReader) throws IOException, InvalidRangeException {
        final double objectiveDepth = getObjectiveDepth(sampleSet, objectiveReader);

        if (isOutOfRange(objectiveDepth)) {
            return false;
        }

        final double referenceDepth = getReferenceDepth(sampleSet, referenceReader);

        if (isOutOfRange(referenceDepth)) {
            return false;
        }

        final int l = Arrays.binarySearch(levels, referenceDepth);
        if (l < 0) {
            return false;  // reference depth is not nominal
        }

        if (objectiveDepth < (0.5 * (referenceDepth + levels[max(l - 1, 0)]))) {
            return false;  // objective depth is too shallow
        }
        //noinspection RedundantIfStatement
        if (objectiveDepth > (0.5 * (referenceDepth + levels[min(l + 1, levels.length - 1)]))) {
            return false;  // objective depth is too deep
        }

        return true;
    }

    private double getObjectiveDepth(SampleSet sampleSet, Reader reader) throws IOException, InvalidRangeException {
        final Sample sample = sampleSet.getPrimary();
        final Array depthArray = reader.readScaled(sample.getX(), sample.getY(), singlePixel, objectiveDepthName);
        final IndexIterator indexIterator = depthArray.getIndexIterator();

        return indexIterator.getDoubleNext();
    }

    private double getReferenceDepth(SampleSet sampleSet, Reader reader) throws IOException, InvalidRangeException {
        final Sample sample = sampleSet.getSecondary(SampleSet.getOnlyOneSecondaryKey());
        final Array depthArray = reader.readScaled(sample.getX(), sample.getY(), singlePixel, referenceDepthName);
        final IndexIterator indexIterator = depthArray.getIndexIterator();

        return indexIterator.getDoubleNext();
    }

    private boolean isOutOfRange(double depth) {
        return depth < DEFAULT_LEVELS[0] || depth > levels[DEFAULT_LEVELS.length - 1];
    }

    static class Configuration {
        String primaryDepthName;
        String secondaryDepthName;
        Boolean primaryIsNominal;
        Double[] levels;
    }
}
