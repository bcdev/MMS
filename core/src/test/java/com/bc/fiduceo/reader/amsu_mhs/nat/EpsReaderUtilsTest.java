package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.sun.jna.platform.win32.WinDef;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class EpsReaderUtilsTest {

    @Test
    public void testReadInt32() {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0, 123456789);
        assertEquals(123456789, EpsReaderUtils.readInt32(buffer, 0));
    }

    @Test
    public void testReadUInt32() {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0, -1);
        assertEquals(4294967295L, EpsReaderUtils.readUInt32(buffer, 0));
    }

    @Test
    public void testReadInt16() {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(0, (short) -12345);
        assertEquals(-12345, EpsReaderUtils.readInt16(buffer, 0));
    }

    @Test
    public void testReadUInt16() {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(0, (short) 0xFFFF);
        assertEquals(65535, EpsReaderUtils.readUInt16(buffer, 0));
    }

    @Test
    public void testReadInt8() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(0, (byte) -100);
        assertEquals(-100, EpsReaderUtils.readInt8(buffer, 0));
    }

    @Test
    public void testReadUInt8() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(0, (byte) 0xFF);
        assertEquals(255, EpsReaderUtils.readUInt8(buffer, 0));
    }

    @Test
    public void testReadInt64() {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(0, 9876543210L);
        assertEquals(9876543210L, EpsReaderUtils.readInt64(buffer, 0));
    }

    @Test
    public void testReadUInt64() {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(0, -1L);
        BigInteger expected = new BigInteger("18446744073709551615");
        assertEquals(expected, EpsReaderUtils.readUInt64(buffer, 0));
    }

    @Test
    public void testMapToProductDataType() {
        int type_byte = EpsReaderUtils.mapToProductData("byte");
        int type_boolean = EpsReaderUtils.mapToProductData("boolean");
        int type_enumerated = EpsReaderUtils.mapToProductData("enumerated");
        int type_u_byte = EpsReaderUtils.mapToProductData("u-byte");
        int type_integer2 = EpsReaderUtils.mapToProductData("integer2");
        int type_u_integer2 = EpsReaderUtils.mapToProductData("u-integer2");
        int type_integer4 = EpsReaderUtils.mapToProductData("integer4");
        int type_u_integer4 = EpsReaderUtils.mapToProductData("u-integer4");
        int type_integer8 = EpsReaderUtils.mapToProductData("integer8");
        int type_u_integer8 = EpsReaderUtils.mapToProductData("u-integer8");

        assertEquals(ProductData.TYPE_INT8, type_byte);
        assertEquals(ProductData.TYPE_INT8, type_boolean);
        assertEquals(ProductData.TYPE_INT8, type_enumerated);
        assertEquals(ProductData.TYPE_UINT8, type_u_byte);
        assertEquals(ProductData.TYPE_INT16, type_integer2);
        assertEquals(ProductData.TYPE_UINT16, type_u_integer2);
        assertEquals(ProductData.TYPE_INT32, type_integer4);
        assertEquals(ProductData.TYPE_UINT32, type_u_integer4);
        assertEquals(ProductData.TYPE_INT64, type_integer8);
        assertEquals(ProductData.TYPE_UINT64, type_u_integer8);
    }

    @Test
    public void testMapToProductDataType_invalid() {
        try {
            EpsReaderUtils.mapToProductData("invalid");
            fail("Exception expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Unknown data type: invalid", expected.getMessage());
        }
    }

    @Test
    public void test_scale_appliesFactor() {
        int[] values = {10, -20, 30};
        Array input = Array.factory(DataType.INT, new int[]{values.length}, values);
        double scaleFactor = 0.1;

        Array result = EpsReaderUtils.scale(input, scaleFactor);

        assertEquals(1.0, result.getDouble(0), 1e-6);
        assertEquals(-2.0, result.getDouble(1), 1e-6);
        assertEquals(3.0, result.getDouble(2), 1e-6);
    }

    @Test
    public void test_scale_factorIsOne_returnsSameArray() {
        int[] values = {5, 10, 15};
        Array input = Array.factory(DataType.INT, new int[]{values.length}, values);
        double scaleFactor = 1.0;

        Array result = EpsReaderUtils.scale(input, scaleFactor);

        assertSame(input, result);
    }

    @Test
    public void testInitializeArray_int8() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_INT8, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayByte.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_uint8() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_UINT8, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayByte.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_int16() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_INT16, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayShort.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_uint16() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_UINT16, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayShort.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_int32() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_INT32, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayInt.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_uint32() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_UINT32, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayInt.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_int64() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_INT64, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayLong.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_uint64() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(ProductData.TYPE_UINT64, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayLong.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testInitializeArray_default() {
        int numScanLines = 4;
        int numFOVs = 20;
        Array array = EpsReaderUtils.initializeArray(9999, numScanLines, numFOVs);
        assertTrue(array instanceof ArrayDouble.D2);
        assertArrayEquals(new int[]{numScanLines, numFOVs}, array.getShape());
    }

    @Test
    public void testGetFillValue() {
        assertEquals(Byte.MIN_VALUE, EpsReaderUtils.getFillValue("byte"));
        assertEquals(255, EpsReaderUtils.getFillValue("u-byte"));
        assertEquals(Short.MIN_VALUE, EpsReaderUtils.getFillValue("integer2"));
        assertEquals(65535, EpsReaderUtils.getFillValue("u-integer2"));
        assertEquals(Integer.MIN_VALUE, EpsReaderUtils.getFillValue("integer4"));
        assertEquals(4294967295L, EpsReaderUtils.getFillValue("u-integer4"));
        assertEquals(Long.MIN_VALUE, EpsReaderUtils.getFillValue("integer8"));
        assertEquals(Long.MAX_VALUE, EpsReaderUtils.getFillValue("u-integer8"));
    }
}