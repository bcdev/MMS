package com.bc.fiduceo.reader.insitu.generic;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class StationDatabaseTest {

    StationDatabase db;

    @Before
    public void setUp() throws Exception {
        GenericVariable var1 = new GenericVariable();
        GenericVariable var2 = new GenericVariable();
        GenericVariable var3 = new GenericVariable();
        var1.setName("id");
        var1.setType("string");
        var2.setName("latitude");
        var2.setType("long");
        var3.setName("station");
        var3.setType("string");

        List<Object> station1 = new ArrayList<>();
        List<Object> station2 = new ArrayList<>();
        List<Object> station3 = new ArrayList<>();
        station1.add("id1");
        station1.add((long) 23.0);
        station1.add("stat1");
        station2.add("id2");
        station2.add(-48.4);
        station2.add("stat2");
        station3.add("id1");
        station3.add((long) -54.4);
        station3.add("stat3");

        db = new StationDatabase();
        db.setPrimaryId("id");
        db.setVariables(Arrays.asList(var1, var2, var3));
        db.setStations(Arrays.asList(station1, station2, station3));
    }

    @Test
    public void extractRecord_noSecondaryId_success() {
        GenericRecord record = db.extractRecord("id1", null);

        assertNotNull(record);
        assertEquals(3, record.getValues().size());
        Object recordId = record.getValues().get("id");
        Object recordLatitude = record.getValues().get("latitude");
        Object recordStation = record.getValues().get("station");
        assertNotNull(recordId);
        assertNotNull(recordLatitude);
        assertNotNull(recordStation);
        assertEquals("id1", recordId);
        assertEquals((long) 23.0, recordLatitude);
        assertEquals("stat1", recordStation);
    }

    @Test
    public void extractRecord_withSecondaryId_success() {
        db.setSecondaryId("station");
        GenericRecord record = db.extractRecord("id1", "stat3");

        assertNotNull(record);
        assertEquals(3, record.getValues().size());
        Object recordId = record.getValues().get("id");
        Object recordLatitude = record.getValues().get("latitude");
        Object recordStation = record.getValues().get("station");
        assertNotNull(recordId);
        assertNotNull(recordLatitude);
        assertNotNull(recordStation);
        assertEquals("id1", recordId);
        assertEquals((long) -54.4, recordLatitude);
        assertEquals("stat3", recordStation);
    }


    @Test
    public void extractRecord_error_doesNotContainVariable() {
        db.setPrimaryId("invalidIdentifier");
        try {
            db.extractRecord("id2", null);
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Station database does not contain variable 'invalidIdentifier'.", e.getMessage());
        }
    }

    @Test
    public void extractRecord_error_doesNotContainSecondaryVariable() {
        db.setPrimaryId("id");
        db.setSecondaryId("invalidIdentifier");
        try {
            db.extractRecord("id1", "station");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Station database does not contain variable 'invalidIdentifier'.", e.getMessage());
        }
    }

    @Test
    public void extractRecord_error_doesNotStationForId() {
        try {
            db.extractRecord("id3", "stat4");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Station database does not contain site/station with ids 'id'/'null.", e.getMessage());
        }
    }
}