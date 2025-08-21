package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MPHR extends Record {

    public MPHR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
    }

    public Date getDate(String key) throws IOException {
        String content = new String(getPayload(), StandardCharsets.US_ASCII);
        String[] lines = content.split("\n");

        for (String line : lines) {
            String[] parts = line.split("=");
            if (parts.length == 2 && parts[0].trim().equals(key)) {
                String value = parts[1].trim();
                return parseDate(value, key);
            }
        }
        throw new IOException(key + " not found in MPHR payload");
    }

    private Date parseDate(String date, String key) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new IOException("Could not parse " + key + " time: " + date, e);
        }
    }
}
