package com.bc.fiduceo.reader.time;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TimeLocator_StartStopDateTest {

    @Test
    public void testGetTimeFor() {
        final Date startDate = new Date(1547653120000L);
        final Date stopDate = new Date(1547653130000L);

        final TimeLocator_StartStopDate timeLocator = new TimeLocator_StartStopDate(startDate, stopDate, 100);

        assertEquals(1547653120000L, timeLocator.getTimeFor(3, 0));
        assertEquals(1547653120101L, timeLocator.getTimeFor(7, 1));
        assertEquals(1547653120909L, timeLocator.getTimeFor(106, 9));
        assertEquals(1547653129899L, timeLocator.getTimeFor(106, 98));
        assertEquals(1547653130000L, timeLocator.getTimeFor(106, 99));
    }
}
