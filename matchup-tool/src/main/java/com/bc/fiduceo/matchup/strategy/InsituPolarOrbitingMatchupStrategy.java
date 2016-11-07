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

package com.bc.fiduceo.matchup.strategy;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.UseCaseConfig;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.Point;
import com.bc.fiduceo.matchup.MatchupCollection;
import com.bc.fiduceo.matchup.Sample;
import com.bc.fiduceo.matchup.condition.ConditionEngine;
import com.bc.fiduceo.matchup.condition.ConditionEngineContext;
import com.bc.fiduceo.matchup.screening.ScreeningEngine;
import com.bc.fiduceo.math.TimeInterval;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderFactory;
import com.bc.fiduceo.tool.ToolContext;
import com.bc.fiduceo.util.TimeUtils;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

class InsituPolarOrbitingMatchupStrategy extends AbstractMatchupStrategy {

    private static final Interval singlePixel = new Interval(1,1);

    InsituPolarOrbitingMatchupStrategy(Logger logger) {
        super(logger);
    }

    @Override
    public MatchupCollection createMatchupCollection(ToolContext context) throws SQLException, IOException, InvalidRangeException {
        final MatchupCollection matchupCollection = new MatchupCollection();
        final UseCaseConfig useCaseConfig = context.getUseCaseConfig();

        final ConditionEngine conditionEngine = new ConditionEngine();
        final ConditionEngineContext conditionEngineContext = ConditionEngine.createContext(context);
        conditionEngine.configure(useCaseConfig);

        final ScreeningEngine screeningEngine = new ScreeningEngine();
        screeningEngine.configure(useCaseConfig);

        final GeometryFactory geometryFactory = context.getGeometryFactory();
        final ReaderFactory readerFactory = ReaderFactory.get(geometryFactory);

        final long timeDeltaInMillis = conditionEngine.getMaxTimeDeltaInMillis();
        final int timeDeltaSeconds = (int) (timeDeltaInMillis / 1000);
        final TimeInterval processingInterval = new TimeInterval(context.getStartDate(), context.getEndDate());

        final List<SatelliteObservation> insituObservations = getPrimaryObservations(context);
        if (insituObservations.size() == 0) {
            logger.warning("No insitu data in time interval:" + context.getStartDate() + " - " + context.getEndDate());
            return  matchupCollection;
        }

        final Date searchTimeStart = TimeUtils.addSeconds(-timeDeltaSeconds, context.getStartDate());
        final Date searchTimeEnd = TimeUtils.addSeconds(timeDeltaSeconds, context.getEndDate());
        final List<SatelliteObservation> secondaryObservations = getSecondaryObservations(context, searchTimeStart, searchTimeEnd);

        for (final SatelliteObservation insituObservation : insituObservations) {
            try (final Reader insituReader = readerFactory.getReader(insituObservation.getSensor().getName())) {
                insituReader.open(insituObservation.getDataFilePath().toFile());

                final List<Sample> insituSamples = getInsituSamples(processingInterval, insituReader);
                for(final Sample insituSample: insituSamples) {
                    final List<SatelliteObservation> candidatesByTime = getCandidatesByTime(secondaryObservations, new Date(insituSample.time), timeDeltaInMillis);
                    //System.out.println("candidatesByTime = " + candidatesByTime.size());

                    final List<SatelliteObservation> candidatesByGeometry = getCandidatesByGeometry(candidatesByTime, geometryFactory.createPoint(insituSample.lon, insituSample.lat));
                    if (candidatesByGeometry.size() > 0) {
                        System.out.println("------------------ FOUND GEOMETRIC INTERSECTION -----------------------");
                        System.out.println("insitu: " + insituObservation.getDataFilePath());

                        System.out.println("satellite: " + candidatesByGeometry.get(0).getDataFilePath());

                    }

                    // @todo 1 tb/tb getSecondaryGedöns
                }
            }
        }

        return matchupCollection;
    }

    private List<Sample> getInsituSamples(TimeInterval processingInterval, Reader insituReader) throws IOException, InvalidRangeException {
        final List<Sample> insituSamples = new ArrayList<>();
        final Dimension productSize = insituReader.getProductSize();
        final int height = productSize.getNy();
        for (int i = 0; i < height; i++) {
            final ArrayInt.D2 acquisitionTimeArray = insituReader.readAcquisitionTime(0, i, singlePixel);
            final int acquisitionTime = acquisitionTimeArray.getInt(0);
            final Date acquisitionDate = TimeUtils.create(acquisitionTime * 1000L);
            if (processingInterval.contains(acquisitionDate)) {
                // @todo 3 tb/tb this is SST-CCI specific - generalise the geolocation access 2016-11-07
                final Array lon = insituReader.readRaw(0, i, singlePixel, "insitu.lon");
                final Array lat = insituReader.readRaw(0, i, singlePixel, "insitu.lat");

                final Sample sample = new Sample(0, i, lon.getDouble(0), lat.getDouble(0), acquisitionDate.getTime());
                insituSamples.add(sample);
            }
        }

        return insituSamples;
    }

    static List<SatelliteObservation> getCandidatesByTime(List<SatelliteObservation> satelliteObservations, Date insituTime, long timeDeltaInMillis) {
        final List<SatelliteObservation> candidateList = new ArrayList<>();
        for (final SatelliteObservation observation: satelliteObservations) {
            final Date startTime = new Date(observation.getStartTime().getTime() - timeDeltaInMillis);
            final Date stopTime = new Date(observation.getStopTime().getTime() + timeDeltaInMillis);
            final TimeInterval observationInterval = new TimeInterval(startTime, stopTime);
            if (observationInterval.contains(insituTime)) {
                candidateList.add(observation);
            }
        }
        return candidateList;
    }

    static List<SatelliteObservation> getCandidatesByGeometry(List<SatelliteObservation> satelliteObservations, Point point) {
        final List<SatelliteObservation> candidateList = new ArrayList<>();
        for (final SatelliteObservation observation: satelliteObservations) {
            final Geometry geoBounds = observation.getGeoBounds();
            if (!geoBounds.getIntersection(point).isEmpty()) {
                candidateList.add(observation);
            }
        }
        return candidateList;
    }
}
