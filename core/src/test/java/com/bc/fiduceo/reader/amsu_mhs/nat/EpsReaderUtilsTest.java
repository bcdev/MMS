package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.junit.Test;

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
}