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

import com.bc.fiduceo.core.Sample;
import com.bc.fiduceo.matchup.MatchupSet;
import com.bc.fiduceo.matchup.SampleSet;
import com.bc.fiduceo.reader.Reader;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DepthScreeningTest {

    private DepthScreening screening;
    private DepthScreening.Configuration configuration;
    private String secondarySensorName;
    private HashMap<String, Reader> secondaryReaderMap;

    private static Array createArray(double d) {
        final Array depthArray = mock(Array.class);
        final IndexIterator depthIterator = mock(IndexIterator.class);
        when((depthIterator.getDoubleNext())).thenReturn(d);
        when(depthArray.getIndexIterator()).thenReturn(depthIterator);
        return depthArray;
    }

    @Before
    public void setUp() {
        screening = new DepthScreening();
        configuration = new DepthScreening.Configuration();
        screening.configure(configuration);
        secondarySensorName = SampleSet.getOnlyOneSecondaryKey();
        secondaryReaderMap = new HashMap<>();
    }

    @Test
    public void testApply_emptyInputSet() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();
        final Reader primaryReader = mock(Reader.class);
        final Reader secondaryReader = mock(Reader.class);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        assertEquals(0, matchupSet.getNumObservations());

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        assertEquals(0, matchupSet.getNumObservations());
    }

    @Test
    public void testApply() throws IOException, InvalidRangeException {
        final MatchupSet matchupSet = new MatchupSet();

        final List<SampleSet> sampleSets = matchupSet.getSampleSets();
        sampleSets.add(createSampleSet(0, 0, 0, 0));
        sampleSets.add(createSampleSet(1, 1, 0, 0));
        sampleSets.add(createSampleSet(2, 2, 0, 0));
        sampleSets.add(createSampleSet(3, 3, 0, 0));
        sampleSets.add(createSampleSet(4, 4, 1, 1));
        sampleSets.add(createSampleSet(5, 5, 1, 1));
        sampleSets.add(createSampleSet(6, 6, 1, 1));
        sampleSets.add(createSampleSet(7, 7, 1, 1));
        sampleSets.add(createSampleSet(8, 8, 1, 1));

        final Array depth30 = createArray(30.0);
        final Array depth50 = createArray(50.0);

        final Array depth10 = createArray(10.0);
        final Array depth20 = createArray(20.0);
        final Array depth25 = createArray(25.0);
        final Array depth40 = createArray(40.0);
        final Array depth45 = createArray(45.0);
        final Array depth65 = createArray(65.0);

        final Reader primaryReader = mock(Reader.class);
        // ok
        when(primaryReader.readScaled(eq(0), eq(0), any(), eq("depth"))).thenReturn(depth25);
        // ok
        when(primaryReader.readScaled(eq(1), eq(1), any(), eq("depth"))).thenReturn(depth40);
        // too deep
        when(primaryReader.readScaled(eq(2), eq(2), any(), eq("depth"))).thenReturn(depth45);
        // too shallow
        when(primaryReader.readScaled(eq(3), eq(3), any(), eq("depth"))).thenReturn(depth20);
        // ok
        when(primaryReader.readScaled(eq(4), eq(4), any(), eq("depth"))).thenReturn(depth40);
        // ok
        when(primaryReader.readScaled(eq(5), eq(5), any(), eq("depth"))).thenReturn(depth45);
        // too deep
        when(primaryReader.readScaled(eq(6), eq(6), any(), eq("depth"))).thenReturn(depth65);
        // too shallow
        when(primaryReader.readScaled(eq(7), eq(7), any(), eq("depth"))).thenReturn(depth10);
        // ok
        when(primaryReader.readScaled(eq(8), eq(8), any(), eq("depth"))).thenReturn(depth50);

        final Reader secondaryReader = mock(Reader.class);
        when(secondaryReader.readScaled(eq(0), eq(0), any(), eq("depth"))).thenReturn(depth30);
        when(secondaryReader.readScaled(eq(1), eq(1), any(), eq("depth"))).thenReturn(depth50);
        secondaryReaderMap.put(secondarySensorName, secondaryReader);

        screening.configure(configuration);
        screening.apply(matchupSet, primaryReader, secondaryReaderMap, null);

        final List<SampleSet> selectedSampleSets = matchupSet.getSampleSets();
        assertEquals(5, selectedSampleSets.size());
        assertEquals(0, selectedSampleSets.get(0).getPrimary().getX());
        assertEquals(1, selectedSampleSets.get(1).getPrimary().getX());
        assertEquals(4, selectedSampleSets.get(2).getPrimary().getX());
        assertEquals(5, selectedSampleSets.get(3).getPrimary().getX());
        assertEquals(8, selectedSampleSets.get(4).getPrimary().getX());
    }

    private SampleSet createSampleSet(int primaryX, int primaryY, int secondaryX, int secondaryY) {
        final SampleSet sampleSet = new SampleSet();
        final Sample primary = new Sample(primaryX, primaryY, 0.0, 0.0, 0);
        sampleSet.setPrimary(primary);
        final Sample secondary = new Sample(secondaryX, secondaryY, 0.0, 0.0, 0);
        sampleSet.setSecondary(SampleSet.getOnlyOneSecondaryKey(), secondary);

        return sampleSet;
    }
}
