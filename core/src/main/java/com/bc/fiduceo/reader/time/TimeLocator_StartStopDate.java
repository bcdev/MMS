package com.bc.fiduceo.reader.time;

import java.util.Date;

public class TimeLocator_StartStopDate implements TimeLocator {

    private final long startTime;
    private final double increment;

    public TimeLocator_StartStopDate(Date startTime, Date stopTime, int numLines) {
        this.startTime = startTime.getTime();
        double timeDelta = (stopTime.getTime() - this.startTime);
        increment = timeDelta / (numLines - 1);
    }

    @Override
    public long getTimeFor(int x, int y) {
        return Math.round((startTime + y * increment));
    }
}
