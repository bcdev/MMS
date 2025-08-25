package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;
import org.apache.commons.lang3.Conversion;

public class MDR extends Record {

    public MDR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }

    public int[] parseVariable(String variableName) {
        byte[] payload = getPayload();
        int offset = 0;
        int count = 0;
        int stride = 0;
        int size = 0;
        if (variableName.equals("latitude")) {
            offset = 2082;
            count = 30;
            stride = 2;
            size = 4;
        } else if (variableName.equals("longitude")) {
            offset = 2086;
            count = 30;
            stride = 2;
            size = 4;
        }


        int[] variableRawData = new int[count];
        for (int i = 0; i < count; i++) {
            variableRawData[i] = Conversion.byteArrayToInt(payload, offset + stride * i * size, variableRawData[i], 0, size);
        }
        return variableRawData;
    }
}
