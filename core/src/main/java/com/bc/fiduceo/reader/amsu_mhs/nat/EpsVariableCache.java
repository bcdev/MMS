package com.bc.fiduceo.reader.amsu_mhs.nat;

import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MDR;
import com.bc.fiduceo.reader.amsu_mhs.nat.record_types.MPHR;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EpsVariableCache {

    private final int numFOVs;
    private final List<Record> records;
    private final VariableRegistry registry;

    private final Map<String, Array> rawDataCache;

    public EpsVariableCache(byte[] rawDataBuffer, VariableRegistry registry, int numFOVs) {
        this.records = RecordFactory.parseRecords(rawDataBuffer);
        this.registry = registry;
        this.rawDataCache = new HashMap<>();
        this.numFOVs = numFOVs;
    }

    public void clear() {
        rawDataCache.clear();
    }

    public MPHR getMPHR() {
        return MdrUtilities.getMphr(records);
    }

    public List<MDR> getMdrs() {
        return MdrUtilities.getMdrList(records);
    }

    public Array getRaw(String variableName) {
        Array array = rawDataCache.get(variableName);

        if (array == null) {
            VariableDefinition varDef = registry.getVariableDef(variableName);
            List<MDR> mdrs = getMdrs();
            int numScanLines = mdrs.size();
            int stride = varDef.getStride();
            int offset = varDef.getOffset();
            int dataType = varDef.getData_type();
            int size = ProductData.getElemSize(dataType);

            array = EpsReaderUtils.initializeArray(dataType, numScanLines, numFOVs);

            for (int yy = 0; yy < numScanLines; yy++) {
                MDR mdr = mdrs.get(yy);
                byte[] payload = mdr.getPayload();

                for (int xx = 0; xx < numFOVs; xx++) {
                    int valueSpecificOffset = offset + xx * stride * size;
                    double value = readValue(payload, valueSpecificOffset, varDef);

                    array.setDouble(array.getIndex().set(yy, xx), value);
                }
            }
            rawDataCache.put(variableName, array);
        }

        return array;
    }

    private double readValue(byte[] payload, int offset, VariableDefinition def) {
        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN);
        int type = def.getData_type();

        switch (type) {
            case ProductData.TYPE_INT8:
                return EpsReaderUtils.readInt8(buffer, offset);
            case ProductData.TYPE_UINT8:
                return EpsReaderUtils.readUInt8(buffer, offset);
            case ProductData.TYPE_INT16:
                return EpsReaderUtils.readInt16(buffer, offset);
            case ProductData.TYPE_UINT16:
                return EpsReaderUtils.readUInt16(buffer, offset);
            case ProductData.TYPE_INT32:
                return EpsReaderUtils.readInt32(buffer, offset);
            case ProductData.TYPE_UINT32:
                return EpsReaderUtils.readUInt32(buffer, offset);
            case ProductData.TYPE_INT64:
                return EpsReaderUtils.readInt64(buffer, offset);
            case ProductData.TYPE_UINT64:
                return EpsReaderUtils.readUInt64(buffer, offset).doubleValue();
            default:
                throw new IllegalArgumentException("Unsupported data type: " + type);
        }
    }
}
