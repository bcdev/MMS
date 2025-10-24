package com.bc.fiduceo.reader.insitu.generic;

import org.junit.Test;
import ucar.ma2.DataType;

import java.io.File;

import static org.junit.Assert.*;


public class GenericCsvHelperTest {

    @Test
    public void getResourceKeyFromPath_NDBC_SM() {
        String path_ob = "/some/path/ndbc-sm-ob/to/file";
        String path_cb = "/some/path/ndbc-sm-cb/to/file";
        String path_lb = "/some/path/ndbc-sm-lb/to/file";
        String path_os = "/some/path/ndbc-sm-os/to/file";
        String path_cs = "/some/path/ndbc-sm-cs/to/file";
        String path_ls = "/some/path/ndbc-sm-ls/to/file";

        String key_ob = GenericCsvHelper.getResourceKeyFromPath(path_ob);
        String key_cb = GenericCsvHelper.getResourceKeyFromPath(path_cb);
        String key_lb = GenericCsvHelper.getResourceKeyFromPath(path_lb);
        String key_os = GenericCsvHelper.getResourceKeyFromPath(path_os);
        String key_cs = GenericCsvHelper.getResourceKeyFromPath(path_cs);
        String key_ls = GenericCsvHelper.getResourceKeyFromPath(path_ls);

        String expected = GenericCsvHelper.RESOURCE_KEY_NDBC_SM;

        assertNotNull(key_ob);
        assertNotNull(key_cb);
        assertNotNull(key_lb);
        assertNotNull(key_os);
        assertNotNull(key_cs);
        assertNotNull(key_ls);
        assertEquals(expected, key_ob);
        assertEquals(expected, key_cb);
        assertEquals(expected, key_lb);
        assertEquals(expected, key_os);
        assertEquals(expected, key_cs);
        assertEquals(expected, key_ls);
    }

    @Test
    public void getResourceKeyFromPath_NDBC_CW() {
        String path_ob = "/some/path/ndbc-cw-ob/to/file";
        String path_cb = "/some/path/ndbc-cw-cb/to/file";
        String path_lb = "/some/path/ndbc-cw-lb/to/file";
        String path_os = "/some/path/ndbc-cw-os/to/file";
        String path_cs = "/some/path/ndbc-cw-cs/to/file";
        String path_ls = "/some/path/ndbc-cw-ls/to/file";

        String key_ob = GenericCsvHelper.getResourceKeyFromPath(path_ob);
        String key_cb = GenericCsvHelper.getResourceKeyFromPath(path_cb);
        String key_lb = GenericCsvHelper.getResourceKeyFromPath(path_lb);
        String key_os = GenericCsvHelper.getResourceKeyFromPath(path_os);
        String key_cs = GenericCsvHelper.getResourceKeyFromPath(path_cs);
        String key_ls = GenericCsvHelper.getResourceKeyFromPath(path_ls);

        String expected = GenericCsvHelper.RESOURCE_KEY_NDBC_CW;

        assertNotNull(key_ob);
        assertNotNull(key_cb);
        assertNotNull(key_lb);
        assertNotNull(key_os);
        assertNotNull(key_cs);
        assertNotNull(key_ls);
        assertEquals(expected, key_ob);
        assertEquals(expected, key_cb);
        assertEquals(expected, key_lb);
        assertEquals(expected, key_os);
        assertEquals(expected, key_cs);
        assertEquals(expected, key_ls);
    }

    @Test
    public void getResourceKeyFromPath_GBOV() {
        String path = "/some/path/gbov/to/file";
        String key = GenericCsvHelper.getResourceKeyFromPath(path);

        String expected = GenericCsvHelper.RESOURCE_KEY_GBOV;

        assertNotNull(key);
        assertEquals(expected, key);
    }

    @Test
    public void getResourceKeyFromPath_notSupportedFormat() {
        String path = "/some/path/to/file";

        try {
            GenericCsvHelper.getResourceKeyFromPath(path);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported format for file: /some/path/to/file", e.getMessage());
        }

    }

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
}