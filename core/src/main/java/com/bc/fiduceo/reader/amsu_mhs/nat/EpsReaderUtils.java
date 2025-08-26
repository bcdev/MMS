package com.bc.fiduceo.reader.amsu_mhs.nat;

import java.nio.ByteBuffer;

public class EpsReaderUtils {

    public static int readInt32(ByteBuffer buffer, int offset) {
        return buffer.getInt(offset);
    }

    public static long readUInt32(ByteBuffer buffer, int offset) {
        return Integer.toUnsignedLong(buffer.getInt(offset));
    }

    public static short readInt16(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset);
    }

    public static int readUInt16(ByteBuffer buffer, int offset) {
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    public static byte readInt8(ByteBuffer buffer, int offset) {
        return buffer.get(offset);
    }

    public static short readUInt8(ByteBuffer buffer, int offset) {
        return (short) Byte.toUnsignedInt(buffer.get(offset));
    }

    public static long readInt64(ByteBuffer buffer, int offset) {
        return buffer.getLong(offset);
    }
}
