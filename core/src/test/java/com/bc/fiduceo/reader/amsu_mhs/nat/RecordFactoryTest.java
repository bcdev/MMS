package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;

public class RecordFactoryTest {

    @Test
    public void parseRecordsForIngestion() {
        byte[] recordMphr = createDummyRecord(RECORD_CLASS.MPHR, 24, (byte) 0x11);
        byte[] recordReserved = createDummyRecord(RECORD_CLASS.RESERVED, 28, (byte) 0x22);
        byte[] recordMdr1 = createDummyRecord(RECORD_CLASS.MDR, 64, (byte) 0x33);
        byte[] recordMdr2 = createDummyRecord(RECORD_CLASS.MDR, 128, (byte) 0x44);

        byte[] allBytes = new byte[recordMphr.length + recordReserved.length + recordMdr1.length + recordMdr2.length];
        System.arraycopy(recordMphr, 0, allBytes, 0, recordMphr.length);
        System.arraycopy(recordReserved, 0, allBytes, recordMphr.length, recordReserved.length);
        System.arraycopy(recordMdr1, 0, allBytes, recordMphr.length + recordReserved.length, recordMdr1.length);
        System.arraycopy(recordMdr2, 0, allBytes, recordMphr.length + recordReserved.length + recordMdr1.length, recordMdr2.length);

        List<Record> records = RecordFactory.parseRecordsForIngestion(allBytes);

        assertEquals(3, records.size());
        assertTrue(records.get(0) instanceof MPHR);
        assertTrue(records.get(1) instanceof MDR);
        assertTrue(records.get(2) instanceof MDR);
    }

    @Test
    public void createRecord() {
        GENERIC_RECORD_HEADER headerMPHR = new GENERIC_RECORD_HEADER(RECORD_CLASS.MPHR, INSTRUMENT_GROUP.MHS, 42);
        GENERIC_RECORD_HEADER headerMDR = new GENERIC_RECORD_HEADER(RECORD_CLASS.MDR, INSTRUMENT_GROUP.MHS, 42);
        GENERIC_RECORD_HEADER headerRecord = new GENERIC_RECORD_HEADER(RECORD_CLASS.RESERVED, INSTRUMENT_GROUP.MHS, 42);
        byte[] payload = new byte[22];

        Record recordMPHR = RecordFactory.createRecord(headerMPHR, payload);
        Record recordMDR = RecordFactory.createRecord(headerMDR, payload);
        Record recordDefault = RecordFactory.createRecord(headerRecord, payload);

        assertTrue(recordMPHR instanceof MPHR);
        assertTrue(recordMDR instanceof MDR);
        assertTrue(!(recordDefault instanceof MPHR) && !(recordDefault instanceof MDR));

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