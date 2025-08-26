package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.*;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RecordFactoryTest {

    @Test
    public void parseRecords() {
        byte[] recordMphr = createDummyRecord(RECORD_CLASS.MPHR, 24, (byte) 0x11);
        byte[] recordReserved = createDummyRecord(RECORD_CLASS.RESERVED, 28, (byte) 0x22);
        byte[] recordSPHR = createDummyRecord(RECORD_CLASS.SPHR, 28, (byte) 0x55);
        byte[] recordMdr1 = createDummyRecord(RECORD_CLASS.MDR, 64, (byte) 0x33);
        byte[] recordMdr2 = createDummyRecord(RECORD_CLASS.MDR, 128, (byte) 0x44);

        byte[] allBytes = new byte[recordMphr.length + recordReserved.length + recordSPHR.length + recordMdr1.length + recordMdr2.length];
        System.arraycopy(recordMphr, 0, allBytes, 0, recordMphr.length);
        System.arraycopy(recordReserved, 0, allBytes, recordMphr.length, recordReserved.length);
        System.arraycopy(recordSPHR, 0, allBytes, recordMphr.length + recordReserved.length, recordSPHR.length);
        System.arraycopy(recordMdr1, 0, allBytes, recordMphr.length + recordReserved.length + recordSPHR.length, recordMdr1.length);
        System.arraycopy(recordMdr2, 0, allBytes, recordMphr.length + recordReserved.length + recordSPHR.length + recordMdr1.length, recordMdr2.length);

        List<Record> records = RecordFactory.parseRecords(allBytes);

        assertEquals(5, records.size());
        assertTrue(records.get(0) instanceof MPHR);
        assertTrue(records.get(1).getClass() == Record.class);
        assertTrue(records.get(2) instanceof SPHR);
        assertTrue(records.get(3) instanceof MDR);
        assertTrue(records.get(4) instanceof MDR);
    }

    @Test
    public void createRecord() {
        GENERIC_RECORD_HEADER headerMPHR = new GENERIC_RECORD_HEADER(RECORD_CLASS.MPHR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerSPHR = new GENERIC_RECORD_HEADER(RECORD_CLASS.SPHR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerIPR = new GENERIC_RECORD_HEADER(RECORD_CLASS.IPR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerGEADR = new GENERIC_RECORD_HEADER(RECORD_CLASS.GEADR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerGIADR = new GENERIC_RECORD_HEADER(RECORD_CLASS.GIADR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerVEADR = new GENERIC_RECORD_HEADER(RECORD_CLASS.VEADR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerVIADR = new GENERIC_RECORD_HEADER(RECORD_CLASS.VIADR, INSTRUMENT_GROUP.MHS, (byte) 3, (byte) 6, 42);
        GENERIC_RECORD_HEADER headerMDR = new GENERIC_RECORD_HEADER(RECORD_CLASS.MDR, INSTRUMENT_GROUP.MHS, (byte) 4, (byte) 7, 42);
        GENERIC_RECORD_HEADER headerRecord = new GENERIC_RECORD_HEADER(RECORD_CLASS.RESERVED, INSTRUMENT_GROUP.MHS, (byte) 5, (byte) 8, 42);
        byte[] payload = new byte[22];

        Record recordMPHR = RecordFactory.createRecord(headerMPHR, payload);
        Record recordSPHR = RecordFactory.createRecord(headerSPHR, payload);
        Record recordIPR = RecordFactory.createRecord(headerIPR, payload);
        Record recordGEADR = RecordFactory.createRecord(headerGEADR, payload);
        Record recordGIADR = RecordFactory.createRecord(headerGIADR, payload);
        Record recordVEADR = RecordFactory.createRecord(headerVEADR, payload);
        Record recordVIADR = RecordFactory.createRecord(headerVIADR, payload);
        Record recordMDR = RecordFactory.createRecord(headerMDR, payload);
        Record recordDefault = RecordFactory.createRecord(headerRecord, payload);

        assertTrue(recordMPHR instanceof MPHR);
        assertTrue(recordSPHR instanceof SPHR);
        assertTrue(recordIPR instanceof IPR);
        assertTrue(recordGEADR instanceof GEADR);
        assertTrue(recordGIADR instanceof GIADR);
        assertTrue(recordVEADR instanceof VEADR);
        assertTrue(recordVIADR instanceof VIADR);
        assertTrue(recordMDR instanceof MDR);
        assertTrue(recordDefault.getClass() == Record.class);

        assertEquals(payload.length, recordMPHR.getPayload().length);
    }

    private byte[] createDummyRecord(RECORD_CLASS recordClass, int size, byte fillValue) {
        byte[] record = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(record);

        buffer.put((byte) recordClass.ordinal());  // RECORD_CLASS
        buffer.put((byte) 0);                      // INSTRUMENT_GROUP dummy
        buffer.put((byte) 0);                      // RECORD_SUBCLASS
        buffer.put((byte) 1);                      // RECORD_SUBCLASS_VERSION
        buffer.putInt(size);                       // RECORD_SIZE

        for (int i = 20; i < size; i++) {
            record[i] = fillValue;
        }
        return record;
    }

}