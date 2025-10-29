package com.bc.fiduceo.reader.insitu.generic;

import org.junit.Test;
import ucar.ma2.DataType;

import java.io.File;

import static org.junit.Assert.*;


public class GenericCsvHelperTest {

    @Test
    public void testExtractYearMonthDayFromFilename_NDBC_success() {
        int[] ymd = GenericCsvHelper.extractYearMonthDayFromFilename("12345h2026.txt", GenericCsvHelper.RESOURCE_KEY_NDBC_CW);
        assertEquals(3, ymd.length);
        assertEquals(2026, ymd[0]);
        assertEquals(1, ymd[1]);
        assertEquals(1, ymd[2]);
    }

    @Test
    public void testExtractYearMonthDayFromFilename_GBOV_success() {
        int[] ymd = GenericCsvHelper.extractYearMonthDayFromFilename("GBOV__site--name__station--name__20021023T000000Z__20021031T235900Z.csv", GenericCsvHelper.RESOURCE_KEY_GBOV);
        assertEquals(3, ymd.length);
        assertEquals(2002, ymd[0]);
        assertEquals(10, ymd[1]);
        assertEquals(23, ymd[2]);
    }

    @Test
    public void testExtractYearMonthDayFromFilename_error() {
        try {
            GenericCsvHelper.extractYearMonthDayFromFilename("12345h2026.txt", "not_supported_format_key");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Unsupported format for file: '12345h2026.txt' and resourceKey 'not_supported_format_key'.", e.getMessage());
        }
    }

    @Test
    public void testGetPrimaryIdFromFilename_NDBC_success() {
        File file = new File("54321c2017.txt");
        String resKey1 = GenericCsvHelper.RESOURCE_KEY_NDBC_SM;
        String resKey2 = GenericCsvHelper.RESOURCE_KEY_NDBC_CW;

        String stationId1 = GenericCsvHelper.getPrimaryIdFromFilename(file, resKey1);
        String stationId2 = GenericCsvHelper.getPrimaryIdFromFilename(file, resKey2);

        String expected = "54321";
        assertEquals(expected, stationId1);
        assertEquals(expected, stationId2);
    }

    @Test
    public void testGetPrimaryIdFromFilename_NDBC_error_filenameTooShort() {
        File file = new File("5432");
        String resKey = GenericCsvHelper.RESOURCE_KEY_NDBC_SM;

        try {
            GenericCsvHelper.getPrimaryIdFromFilename(file, resKey);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported format for file:"));
        }
    }

    @Test
    public void testGetPrimaryIdFromFilename_GBOV_success() {
        File file = new File("GBOV__site--name__station--name__20021023T000000Z__20021031T235900Z.csv");
        String resKey = GenericCsvHelper.RESOURCE_KEY_GBOV;

        String siteId = GenericCsvHelper.getPrimaryIdFromFilename(file, resKey);

        String expected = "site name";
        assertEquals(expected, siteId);
    }

    @Test
    public void testGetPrimaryIdFromFilename_error_unsupportedFormat() {
        File file = new File("54321c2017.txt");
        String resKey = "unsupported_format_key";

        try {
            GenericCsvHelper.getPrimaryIdFromFilename(file, resKey);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unsupported format for file:"));
        }
    }

    @Test
    public void testGetSecondaryIdFromFilename_GBOV_success() {
        File file = new File("GBOV__site--name__station--name__20021023T000000Z__20021031T235900Z.csv");
        String resKey = GenericCsvHelper.RESOURCE_KEY_GBOV;

        String stationId = GenericCsvHelper.getSecondaryIdFromFilename(file, resKey);

        String expected = "station name";
        assertEquals(expected, stationId);
    }

    @Test
    public void testGetSecondaryIdFromFilename_noSecondaryId() {
        File file = new File("someName.txt");

        String id1 = GenericCsvHelper.getSecondaryIdFromFilename(file, null);
        String id2 = GenericCsvHelper.getSecondaryIdFromFilename(file, GenericCsvHelper.RESOURCE_KEY_NDBC_CW);
        String id3 = GenericCsvHelper.getSecondaryIdFromFilename(file, GenericCsvHelper.RESOURCE_KEY_NDBC_SM);

        assertNull(id1);
        assertNull(id2);
        assertNull(id3);
    }

    @Test
    public void testGetNcDataType() {
        assertEquals(DataType.BYTE, GenericCsvHelper.getNcDataType("byte"));
        assertEquals(DataType.SHORT, GenericCsvHelper.getNcDataType("short"));
        assertEquals(DataType.INT, GenericCsvHelper.getNcDataType("int"));
        assertEquals(DataType.LONG, GenericCsvHelper.getNcDataType("long"));
        assertEquals(DataType.FLOAT, GenericCsvHelper.getNcDataType("float"));
        assertEquals(DataType.DOUBLE, GenericCsvHelper.getNcDataType("double"));
        assertEquals(DataType.STRING, GenericCsvHelper.getNcDataType("string"));
        assertEquals(DataType.STRING, GenericCsvHelper.getNcDataType("lalaaa"));
    }

    @Test
    public void testGetFillValueClass() {
        assertEquals(byte.class, GenericCsvHelper.getFillValueClass("byte"));
        assertEquals(short.class, GenericCsvHelper.getFillValueClass("short"));
        assertEquals(int.class, GenericCsvHelper.getFillValueClass("int"));
        assertEquals(long.class, GenericCsvHelper.getFillValueClass("long"));
        assertEquals(float.class, GenericCsvHelper.getFillValueClass("float"));
        assertEquals(double.class, GenericCsvHelper.getFillValueClass("double"));

        try {

            assertEquals(byte.class, GenericCsvHelper.getFillValueClass("string"));
            fail();
        } catch (RuntimeException ignore) {}
    }

    @Test
    public void testCastFillValue() {
        Number num = GenericCsvHelper.castFillValue(1.0, "byte");
        assertEquals((byte) 1, num);
        num = GenericCsvHelper.castFillValue(1.0, "short");
        assertEquals((short) 1, num);
        num = GenericCsvHelper.castFillValue(1.0, "int");
        assertEquals(1, num);
        num = GenericCsvHelper.castFillValue(1.0, "long");
        assertEquals((long) 1, num);
        num = GenericCsvHelper.castFillValue(1.0, "float");
        assertEquals((float) 1, num);
        num = GenericCsvHelper.castFillValue(1.0, "double");
        assertEquals(1.0, num);
        num = GenericCsvHelper.castFillValue(1.0, "sdyhgsr");
        assertEquals(1.0, num);
    }
}