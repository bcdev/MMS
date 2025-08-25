package com.bc.fiduceo.reader.amsu_mhs.nat.record_types;

import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.Record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MPHR extends Record {

    public static final int PRODUCT_NAME_OFFSET = 20;
    public static final int STRING_SIZE = 100;
    private final HashMap<String, String> attributesMap;

    public MPHR(GENERIC_RECORD_HEADER header, byte[] payload) {
        super(header, payload);
        attributesMap = new HashMap<>();
    }

    public Date getDate(String key) throws IOException {
        String attributeValue = attributesMap.get(key);
        if (attributeValue == null) {
            int offset;
            if (key.equals("SENSING_START")) {
                offset = 700;
            } else if (key.equals("SENSING_END")) {
                offset = 748;
            } else  {
                throw new IllegalStateException("Unknown attribute key: " + key);
            }
            attributeValue = extractStringAttribute(offset, 48);
            attributesMap.put(key, attributeValue);
        }

        return parseDate(attributeValue);
        // @todo 3 tb/* make this a lazy process, i.e. put fields already decoded into a HashMap and use this as cache 2025-08-25
        /*String content = new String(getPayload(), StandardCharsets.US_ASCII);
        String[] lines = content.split("\n");

        for (String line : lines) {
            String[] parts = line.split("=");
            if (parts.length == 2 && parts[0].trim().equals(key)) {
                String value = parts[1].trim();
                return parseDate(value, key);
            }
        }
        throw new IOException(key + " not found in MPHR payload");*/
    }


    private Date parseDate(String date) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            throw new IOException("Could not parse time: " + date, e);
        }
    }

    public String getProductName() {
        String productName = attributesMap.get("PRODUCT_NAME");
        if (productName == null) {
            productName = extractStringAttribute(PRODUCT_NAME_OFFSET, STRING_SIZE);
            attributesMap.put("PRODUCT_NAME", productName);
        }
        return productName;
    }

    private String extractStringAttribute(int offset, int size) {
        final byte[] nameBuffer = new byte[100];
        System.arraycopy(getPayload(), offset, nameBuffer, 0, size);
        final String attributeString = new String(nameBuffer, StandardCharsets.US_ASCII).trim();
        final String[] parts = attributeString.split("=");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid attribute formatting: " + attributeString);
        }
        return parts[1].trim();
    }
}
