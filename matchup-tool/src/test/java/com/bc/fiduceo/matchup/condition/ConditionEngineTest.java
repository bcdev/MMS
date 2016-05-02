/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
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

package com.bc.fiduceo.matchup.condition;

import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.core.UseCaseConfigBuilder;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.MatchupToolUseCaseConfigBuilder;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.SampleSet;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConditionEngineTest {

    private ConditionEngine conditionEngine;
    private MatchupSet matchupSet;
    private ConditionsContext context;

    @Before
    public void setUp() {
        conditionEngine = new ConditionEngine();
        matchupSet = new MatchupSet();
        context = new ConditionsContext();
        final Date startDate = new Date(Long.MIN_VALUE);
        final Date endDate = new Date(Long.MAX_VALUE);
        context.setStartDate(startDate);
        context.setEndDate(endDate);
    }

    @Test
    public void testApply_noConditions_emptySet() {
        conditionEngine.process(matchupSet, context);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_noConditions_oneMatchup() {
        matchupSet.getSampleSets().add(createSampleSet(2000, 30000));

        conditionEngine.process(matchupSet, context);

        assertEquals(1, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_noConditions_threeMatchup() {
        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(234, 556));
        sampleSets.add(createSampleSet(19887, 3668));
        sampleSets.add(createSampleSet(8837, 662));

        conditionEngine.process(matchupSet, context);

        assertEquals(3, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_timeDeltaCondition() {

        final InputStream stream = new MatchupToolUseCaseConfigBuilder("name")
                    .withTimeDeltaSeconds(20)
                    .getStream();
        final UseCaseConfig useCaseConfig = UseCaseConfig.load(stream);

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(100000, 100100));
        sampleSets.add(createSampleSet(100200, 100500));
        sampleSets.add(createSampleSet(200200, 100500));    // <- this one gets removed

        conditionEngine.configure(useCaseConfig);
        conditionEngine.process(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_timeRangeCondition() {
        final long oneDayMillis = 1000 * 60 * 60 * 24;
        final long fiveDays = 5 * oneDayMillis;
        final long twelveDays = 12 * oneDayMillis;
        final Date startDate = new Date();
        final long startTime = startDate.getTime();
        final Date endDate = new Date(startTime + twelveDays);
        final long endTime = endDate.getTime();

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(startTime - 1, 100500));    // <- this one gets removed
        sampleSets.add(createSampleSet(startTime, 100100));
        sampleSets.add(createSampleSet(startTime + fiveDays, 100500));
        sampleSets.add(createSampleSet(endTime, 100500));
        sampleSets.add(createSampleSet(endTime + 1, 100500));    // <- this one gets removed

        context.setStartDate(startDate);
        context.setEndDate(endDate);
        conditionEngine.configure(UseCaseConfig.load(UseCaseConfigBuilder.build("name").getStream()));
        conditionEngine.process(matchupSet, context);

        assertEquals(3, matchupSet.getNumObservations());
        final List<SampleSet> resultSet = matchupSet.getSampleSets();

        assertEquals(startTime, resultSet.get(0).getPrimary().time);
        assertEquals(startTime + fiveDays, resultSet.get(1).getPrimary().time);
        assertEquals(endTime, resultSet.get(2).getPrimary().time);
    }

    @Test
    public void testLoad__timeDelta() {
        final String useCaseXml = "<use-case-config name=\"use-case 20\">" +
                                  "  <conditions>" +
                                  "    <time-delta>" +
                                  "      <time-delta-seconds>300</time-delta-seconds>" +
                                  "    </time-delta>" +
                                  "  </conditions>" +
                                  "</use-case-config>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(useCaseXml.getBytes());

        final UseCaseConfig useCaseConfig = UseCaseConfig.load(inputStream);
        conditionEngine.configure(useCaseConfig);
        assertEquals("use-case 20", useCaseConfig.getName());
        assertEquals(300000, conditionEngine.getMaxTimeDeltaInMillis());
    }

    @Test
    public void testApply_distanceCondition() {
        final UseCaseConfig useCaseConfig = new MatchupToolUseCaseConfigBuilder("name")
                    .withMaxPixelDistanceKm(4)
                    .withTimeDeltaSeconds(1)
                    .createConfig();

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(4.5, 5.6, 4.50001, 5.60001));
        sampleSets.add(createSampleSet(20.0, 14.0, 20.002, 13.998));
        sampleSets.add(createSampleSet(1.0, 2.0, 3.0, 4.0));    // <- this one gets removed

        conditionEngine.configure(useCaseConfig);
        conditionEngine.process(matchupSet, context);

        assertEquals(2, matchupSet.getNumObservations());
    }

    @Test
    public void testApply_distanceAndTimeDeltaCondition() {
        final InputStream stream = new MatchupToolUseCaseConfigBuilder("name")
                    .withTimeDeltaSeconds(10)
                    .withMaxPixelDistanceKm(4)
                    .getStream();
        final UseCaseConfig useCaseConfig = UseCaseConfig.load(stream);

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(4.5, 5.6, 4.50001, 5.60001));
        sampleSets.add(createSampleSet(200000, 100100));    // <- this one gets removed
        sampleSets.add(createSampleSet(20.0, 14.0, 20.002, 13.998));
        sampleSets.add(createSampleSet(100200, 100500));
        sampleSets.add(createSampleSet(100200, 100500));
        sampleSets.add(createSampleSet(1.0, 2.0, 3.0, 4.0));    // <- this one gets removed

        conditionEngine.configure(useCaseConfig);
        conditionEngine.process(matchupSet, context);

        assertEquals(4, matchupSet.getNumObservations());
    }

    private SampleSet createSampleSet(long primaryTime, long secondaryTime) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, 0, 0, primaryTime));
        sampleSet.setSecondary(new Sample(0, 0, 0, 0, secondaryTime));
        return sampleSet;
    }

    private SampleSet createSampleSet(double primaryLon, double primaryLat, double secondaryLon, double secondaryLat) {
        final SampleSet sampleSet = new SampleSet();
        sampleSet.setPrimary(new Sample(0, 0, primaryLon, primaryLat, 0));
        sampleSet.setSecondary(new Sample(0, 0, secondaryLon, secondaryLat, 0));
        return sampleSet;
    }
}
