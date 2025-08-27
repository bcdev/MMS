package com.bc.fiduceo.reader.amsu_mhs.nat;

import org.esa.snap.core.datamodel.ProductData;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class EpsReaderUtils {

    /**
        reads eps data types "byte", "boolean", "enumerated" from buffer
     */
    public static byte readInt8(ByteBuffer buffer, int offset) {
        return buffer.get(offset);
    }

    /**
        reads eps data types "u-byte" from buffer
     */
    public static short readUInt8(ByteBuffer buffer, int offset) {
        return (short) Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     reads eps data types "integer2" from buffer
     */
    public static short readInt16(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset);
    }

    /**
     reads eps data types "u-integer2" from buffer
     */
    public static int readUInt16(ByteBuffer buffer, int offset) {
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     reads eps data types "integer-4" from buffer
     */
    public static int readInt32(ByteBuffer buffer, int offset) {
        return buffer.getInt(offset);
    }

    /**
     reads eps data types "u-integer4" from buffer
     */
    public static long readUInt32(ByteBuffer buffer, int offset) {
        return Integer.toUnsignedLong(buffer.getInt(offset));
    }

    /**
        reads eps data types "integer8" from buffer
     */
    public static long readInt64(ByteBuffer buffer, int offset) {
        return buffer.getLong(offset);
    }

    /**
        reads eps data types "u-integer8" from buffer
     */
    public static BigInteger readUInt64(ByteBuffer buffer, int offset) {
        byte[] bytes = new byte[8];
        buffer.position(offset);
        buffer.get(bytes);
        return new BigInteger(1, bytes); // unsigned interpretation
    }

    public static int mapToProductData(String value) {
        switch (value.toLowerCase()) {
            case "byte":
            case "boolean":
            case "enumerated":
                return ProductData.TYPE_INT8;
            case "u-byte":
                return ProductData.TYPE_UINT8;
            case "integer2":
                return ProductData.TYPE_INT16;
            case "u-integer2":
                return ProductData.TYPE_UINT16;
            case "integer4":
                return ProductData.TYPE_INT32;
            case "u-integer4":
                return ProductData.TYPE_UINT32;
            case "integer8":
                return ProductData.TYPE_INT64;
            case "u-integer8":
                return ProductData.TYPE_UINT64;
            default:
                throw new IllegalArgumentException("Unknown data type: " + value);
        }
    }
}
