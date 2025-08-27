package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

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
}