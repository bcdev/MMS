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

package com.bc.fiduceo.matchup;

import java.util.ArrayList;
import java.util.List;

public class MatchupCollection {

    private final List<MatchupSet> matchupSets;

    public MatchupCollection() {
        matchupSets = new ArrayList<>();
    }

    public void add(MatchupSet matchupSet) {
        matchupSets.add(matchupSet);
    }

    public List<MatchupSet> getSets() {
        return matchupSets;
    }

    public int getNumMatchups() {
        int numMatchups = 0;
        for(final MatchupSet set : matchupSets) {
            numMatchups += set.getNumObservations();
        }
        return numMatchups;
    }

    public MatchupSet getFirst() {
        if (matchupSets.size() > 0) {
            return matchupSets.get(0);
        }
        return null;
    }
}