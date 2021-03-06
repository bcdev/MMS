package com.bc.fiduceo.reader.amsr.amsr2;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PixelDataQualityVariable extends VariablePrototype {

    private final Variable originalVariable;

    PixelDataQualityVariable(Variable originalVariable) {
        this.originalVariable = originalVariable;
    }

    @Override
    public String getShortName() {
        return originalVariable.getShortName();
    }

    @Override
    public DataType getDataType() {
        return DataType.SHORT;
    }

    @Override
    public Array read() throws IOException {
        final Array originalArray = originalVariable.read();
        final int[] shape = originalArray.getShape();
        shape[1] = shape[1] / 2;
        final Array resultArray = Array.factory(DataType.SHORT, shape);

        final int size = (int) resultArray.getSize();
        for (int i = 0; i < size; i++) {
            final int origIndex = 2 * i;
            final byte lsb = originalArray.getByte(origIndex);
            final byte msb = originalArray.getByte(origIndex + 1);
            final short mergedValue = (short) ((msb << 8) | lsb);
            resultArray.setShort(i, mergedValue);
        }

        return resultArray;
    }

    @Override
    public List<Attribute> getAttributes() {
        final ArrayList<Attribute> newAttributes = new ArrayList<>(originalVariable.getAttributes());
        newAttributes.add(new Attribute("flag_values", "2, 3, 8, 12, 32, 48, 128, 192, 32768"));
        newAttributes.add(new Attribute("flag_meanings", "rfi_pos_6V rfi_cont_6V rfi_pos_6H rfi_cont_6H rfi_pos_7V rfi_cont_7V rfi_pos_7H rfi_cont_7H data_drop_7H"));
        return newAttributes;
    }
}
