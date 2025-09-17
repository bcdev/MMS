package com.bc.fiduceo.reader.amsu_mhs;

import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.amsu_mhs.nat.GENERIC_RECORD_HEADER;
import com.bc.fiduceo.reader.amsu_mhs.nat.VariableDefinition;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("resource")
public class AMSUA_L1B_ReaderTest {

    @Test
    public void testEnsureMdrVersionSupported() {
        final byte[] data = {8, 1, 2, 3, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
        } catch (IllegalStateException e) {
            fail("no exception expected");
        }
    }

    @Test
    public void testEnsureMdrVersionSupported_wrongVersion() {
        // ----------------------- ! ----------------
        final byte[] data = {8, 1, 3, 3, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testEnsureMdrVersionSupported_wrongSubVersion() {
        // -------------------------- ! -------------
        final byte[] data = {8, 1, 2, 5, 0, 0, 0, 100};
        final GENERIC_RECORD_HEADER recordHeader = GENERIC_RECORD_HEADER.parse(data);

        try {
            AMSUA_L1B_Reader.ensureMdrVersionSupported(recordHeader);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testGetRegEx() {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        final String regEx = reader.getRegEx();
        assertEquals("AMSA_[A-Z0-9x]{3}_1B_M0[123]_[0-9]{14}Z_[0-9]{14}Z_[A-Z0-9x]{1}_[A-Z0-9x]{1}_[0-9]{14}Z\\.nat", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("ABCD_xxx_1C_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1B_M03_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("MHSx_xxx_1B_M04_20250820060350Z_20250820074550Z_N_O_20250820074043Z.nat");
        assertFalse(matcher.matches());
    }

    @Test
    public void testGetLongitudeVariableName()  {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        assertEquals("longitude", reader.getLongitudeVariableName());
    }

    @Test
    public void testGetLatitudeVariableName()  {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        assertEquals("latitude", reader.getLatitudeVariableName());
    }

    @Test
    public void testExtractYearMonthDayFromFilename()  {
        final AMSUA_L1B_Reader reader = new AMSUA_L1B_Reader(new ReaderContext());

        assertArrayEquals(new int[]{2016, 1, 1}, reader.extractYearMonthDayFromFilename("AMSA_xxx_1B_M01_20160101234924Z_20160102013124Z_N_O_20160102003323Z.nat"));
    }

    @Test
    public void testExtractCFAttributes()  {
        final VariableDefinition definition = new VariableDefinition();
        definition.setUnits("m/s");
        definition.setScale_factor(1.8);
        definition.setData_type("integer4");
        definition.setFlag_meanings("hans frida bert agathe mümmelmann");
        definition.setFlag_values("1, 2, 4, 8, 16");
        definition.setStandard_name("gamble_man");

        final List<Attribute> attributes = AMSUA_L1B_Reader.extractCFAttributes(definition);
        assertEquals(7, attributes.size());

        Attribute attribute = attributes.get(0);
        assertEquals("units", attribute.getShortName());
        assertEquals("m/s", attribute.getStringValue());

        attribute = attributes.get(1);
        assertEquals("scale_factor", attribute.getShortName());
        assertEquals(1.8, attribute.getNumericValue());

        attribute = attributes.get(2);
        assertEquals("add_offset", attribute.getShortName());
        assertEquals(0.0, attribute.getNumericValue());

        attribute = attributes.get(3);
        assertEquals("_FillValue", attribute.getShortName());
        assertEquals(-2147483648, attribute.getNumericValue());

        attribute = attributes.get(4);
        assertEquals("flag_meanings", attribute.getShortName());
        assertEquals("hans frida bert agathe mümmelmann", attribute.getStringValue());

        attribute = attributes.get(5);
        assertEquals("flag_values", attribute.getShortName());
        Array values = attribute.getValues();
        assertEquals(5, values.getSize());

        attribute = attributes.get(6);
        assertEquals("standard_name", attribute.getShortName());
        assertEquals("gamble_man", attribute.getStringValue());
    }

    @Test
    public void testToValuesArray()  {
        final String valuesString = "1, 2, 4, 8, 16";

        final Array intArray = AMSUA_L1B_Reader.toValuesArray(valuesString, "integer4");
        assertEquals(DataType.INT, intArray.getDataType());
        assertEquals(5, intArray.getSize());
        assertEquals(1, intArray.getInt(0));
        assertEquals(8, intArray.getInt(3));
    }
}
