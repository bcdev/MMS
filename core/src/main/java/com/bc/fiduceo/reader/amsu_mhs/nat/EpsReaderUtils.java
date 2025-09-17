package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.ReaderUtils;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class EpsReaderUtils {

    /**
     * reads eps data types "byte", "boolean", "enumerated" from buffer
     */
    public static byte readInt8(ByteBuffer buffer, int offset) {
        return buffer.get(offset);
    }

    /**
     * reads eps data types "u-byte" from buffer
     */
    public static short readUInt8(ByteBuffer buffer, int offset) {
        return (short) Byte.toUnsignedInt(buffer.get(offset));
    }

    /**
     * reads eps data types "integer2" from buffer
     */
    public static short readInt16(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset);
    }

    /**
     * reads eps data types "u-integer2" from buffer
     */
    public static int readUInt16(ByteBuffer buffer, int offset) {
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    /**
     * reads eps data types "integer-4" from buffer
     */
    public static int readInt32(ByteBuffer buffer, int offset) {
        return buffer.getInt(offset);
    }

    /**
     * reads eps data types "u-integer4" from buffer
     */
    public static long readUInt32(ByteBuffer buffer, int offset) {
        return Integer.toUnsignedLong(buffer.getInt(offset));
    }

    /**
     * reads eps data types "integer8" from buffer
     */
    public static long readInt64(ByteBuffer buffer, int offset) {
        return buffer.getLong(offset);
    }

    /**
     * reads eps data types "u-integer8" from buffer
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

    public static Array scale(Array array, double scaleFactor) {
        if (ReaderUtils.mustScale(scaleFactor, 0.0)) {
            final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, 0.0);
            return MAMath.convert2Unpacked(array, scaleOffset);
        }
        return array;
    }

    public static Array initializeArray(int dataType, int numScanLines, int numFOVs) {
        Array array;

        switch (dataType) {
            case ProductData.TYPE_INT8:
                array = new ArrayByte.D2(numScanLines, numFOVs, false);
                break;
            case ProductData.TYPE_UINT8:
                array = new ArrayByte.D2(numScanLines, numFOVs, true);
                break;
            case ProductData.TYPE_INT16:
                array = new ArrayShort.D2(numScanLines, numFOVs, false);
                break;
            case ProductData.TYPE_UINT16:
                array = new ArrayShort.D2(numScanLines, numFOVs, true);
                break;
            case ProductData.TYPE_INT32:
                array = new ArrayInt.D2(numScanLines, numFOVs, false);
                break;
            case ProductData.TYPE_UINT32:
                array = new ArrayInt.D2(numScanLines, numFOVs, true);
                break;
            case ProductData.TYPE_INT64:
                array = new ArrayLong.D2(numScanLines, numFOVs, false);
                break;
            case ProductData.TYPE_UINT64:
                array = new ArrayLong.D2(numScanLines, numFOVs, true);
                break;
            default:
                array = new ArrayDouble.D2(numScanLines, numFOVs);
        }

        if (numFOVs == 1) {
            array = array.reduce();
        }
        return array;
    }

    public static Number getFillValue(String dataType) {
        switch (dataType) {
            case "byte":
                return Byte.MIN_VALUE;
            case "u-byte":
                return 255;
            case "integer2":
                return Short.MIN_VALUE;
            case "u-integer2":
                return 65535;
            case "integer4":
                return Integer.MIN_VALUE;
            case "u-integer4":
                return 4294967295L;
            case "integer8":
                return Long.MIN_VALUE;
            case "u-integer8":
                return Long.MAX_VALUE;  // @todo 2 tb/* check how we can handle uint64 in Java anyways 2025-09-16
            default:
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }
}
