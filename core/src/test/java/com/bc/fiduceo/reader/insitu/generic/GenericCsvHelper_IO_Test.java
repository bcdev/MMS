package com.bc.fiduceo.reader.insitu.generic;

import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class GenericCsvHelper_IO_Test {


    @Test
    public void test_parsingSmFile() throws IOException {
        final File testFile = getSM();
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_SM);
        List<GenericRecord> records = GenericCsvHelper.parseData(testFile, config, GenericCsvHelper.RESOURCE_KEY_NDBC_SM);

        assertNotNull(records);
        assertEquals(41834, records.size());

        GenericRecord record = records.get(0);
        assertNotNull(record);
        assertEquals(14, record.getValues().size());
        assertEquals(1483228800, record.get("time"));
        assertEquals((short) 81, record.get("WDIR"));
        assertEquals(6.4f, record.get("WSPD"));
        assertEquals(9.5f, record.get("GST"));
        assertEquals(99.0f, record.get("WVHT"));
        assertEquals(99.0f, record.get("DPD"));
        assertEquals(99.0f, record.get("APD"));
        assertEquals((short) 999, record.get("MWD"));
        assertEquals(1015.0f, record.get("PRES"));
        assertEquals(27.2f, record.get("ATMP"));
        assertEquals(27.3f, record.get("WTMP"));
        assertEquals(20.3f, record.get("DEWP"));
        assertEquals(99.0f, record.get("VIS"));
        assertEquals(99.0f, record.get("TIDE"));

        record = records.get(41833);
        assertNotNull(record);
        assertEquals(14, record.getValues().size());
        assertEquals(1508374800, record.get("time"));
        assertEquals((short) 148, record.get("WDIR"));
        assertEquals(4.1f, record.get("WSPD"));
        assertEquals(11.0f, record.get("GST"));
        assertEquals(99.0f, record.get("WVHT"));
        assertEquals(99.0f, record.get("DPD"));
        assertEquals(99.0f, record.get("APD"));
        assertEquals((short) 999, record.get("MWD"));
        assertEquals(1014.0f, record.get("PRES"));
        assertEquals(26.9f, record.get("ATMP"));
        assertEquals(999.0f, record.get("WTMP"));
        assertEquals(23.3f, record.get("DEWP"));
        assertEquals(99.0f, record.get("VIS"));
        assertEquals(99.0f, record.get("TIDE"));
    }

    @Test
    public void test_parsingCwFile() throws IOException {
        final File testFile = getCW();
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_CW);
        List<GenericRecord> records = GenericCsvHelper.parseData(testFile, config, GenericCsvHelper.RESOURCE_KEY_NDBC_CW);

        assertNotNull(records);
        assertEquals(30516, records.size());

        GenericRecord record = records.get(0);
        assertNotNull(record);
        assertEquals(6, record.getValues().size());
        assertEquals(1464735600, record.get("time"));
        assertEquals((short) 999, record.get("WDIR"));
        assertEquals(5.7f, record.get("WSPD"));
        assertEquals((short) 999, record.get("GDR"));
        assertEquals(99.0f, record.get("GST"));
        assertEquals((short) 9999, record.get("GTIME"));

        record = records.get(30515);
        assertNotNull(record);
        assertEquals(6, record.getValues().size());
        assertEquals(1483224600, record.get("time"));
        assertEquals((short) 184, record.get("WDIR"));
        assertEquals(7.6f, record.get("WSPD"));
        assertEquals((short) 179, record.get("GDR"));
        assertEquals(9.8f, record.get("GST"));
        assertEquals((short) 2157, record.get("GTIME"));
    }

    @Test
    public void test_parsingGBOVFile() throws IOException {
        final File testFile = getGBOV();
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_GBOV);
        List<GenericRecord> records = GenericCsvHelper.parseData(testFile, config, GenericCsvHelper.RESOURCE_KEY_GBOV);

        assertNotNull(records);
        assertEquals(3, records.size());

        GenericRecord record = records.get(0);
        assertNotNull(record);
        assertEquals(61, record.getValues().size());
        assertEquals(1464739200, record.get("TIME_IS"));
        assertEquals(0.617f, record.get("FIPAR_down"));
        assertEquals(0.025f, record.get("FIPAR_down_err"));
        assertEquals(-999f, record.get("FIPAR_total"));
        assertEquals(-999f, record.get("FIPAR_total_err"));
        assertEquals(0.959f, record.get("FIPAR_up"));
        assertEquals(0.012f, record.get("FIPAR_up_err"));
        assertEquals((short) 68, record.get("RM6_down_flag"));
        assertEquals((short) 8, record.get("RM6_up_flag"));
        assertEquals(-999f, record.get("Clumping_Miller"));
        assertEquals(-999f, record.get("Clumping_Miller_err"));
        assertEquals(-999f, record.get("Clumping_Warren"));
        assertEquals(-999f, record.get("Clumping_Warren_err"));
        assertEquals(1.52f, record.get("LAI_Miller_down"));
        assertEquals(0.05f, record.get("LAI_Miller_down_err"));
        assertEquals(6.14f, record.get("LAI_Miller_up"));
        assertEquals(0.15f, record.get("LAI_Miller_up_err"));
        assertEquals(1.2f, record.get("LAI_Warren_down"));
        assertEquals(0.06f, record.get("LAI_Warren_down_err"));
        assertEquals(4.61f, record.get("LAI_Warren_up"));
        assertEquals(.15f, record.get("LAI_Warren_up_err"));
        assertEquals(-999f, record.get("LAI_down"));
        assertEquals(-999f, record.get("LAI_total_Miller"));
        assertEquals(-999f, record.get("LAI_total_Warren"));
        assertEquals(1.41f, record.get("LAIe_Miller_down"));
        assertEquals(.04f, record.get("LAIe_Miller_down_err"));
        assertEquals(4.95f, record.get("LAIe_Miller_up"));
        assertEquals(.13f, record.get("LAIe_Miller_up_err"));
        assertEquals(1.1f, record.get("LAIe_Warren_down"));
        assertEquals(.04f, record.get("LAIe_Warren_down_err"));
        assertEquals(3.63f, record.get("LAIe_Warren_up"));
        assertEquals(.14f, record.get("LAIe_Warren_up_err"));
        assertEquals(-999f, record.get("PAI_Miller"));
        assertEquals(-999f, record.get("PAI_Miller_err"));
        assertEquals(-999f, record.get("PAI_Warren"));
        assertEquals(-999f, record.get("PAI_Warren_err"));
        assertEquals(-999f, record.get("PAIe_Miller"));
        assertEquals(-999f, record.get("PAIe_Miller_err"));
        assertEquals(-999f, record.get("PAIe_Warren"));
        assertEquals(-999f, record.get("PAIe_Warren_err"));
        assertEquals(.93f, record.get("clumping_Miller_down"));
        assertEquals(.04f, record.get("clumping_Miller_down_err"));
        assertEquals(.805f , record.get("clumping_Miller_up"));
        assertEquals(.029f, record.get("clumping_Miller_up_err"));
        assertEquals(.92f, record.get("clumping_Warren_down"));
        assertEquals(.06f, record.get("clumping_Warren_down_err"));
        assertEquals(.79f, record.get("clumping_Warren_up"));
        assertEquals(.04f, record.get("clumping_Warren_up_err"));
        assertEquals((short) 68, record.get("RM7_down_flag"));
        assertEquals((short) 8, record.get("RM7_up_flag"));
        assertEquals(-999f, record.get("LSE"));
        assertEquals(-999f, record.get("LSE_STD"));
        assertEquals(-999f, record.get("LSR"));
        assertEquals(-999f, record.get("LSR_STD"));
        assertEquals((short) -999, record.get("QC_LSE"));
        assertEquals((short) -999, record.get("QC_LSR"));
        assertEquals(-999f, record.get("LST"));
        assertEquals(-999f, record.get("LST_STD"));
        assertEquals((short) -999, record.get("QC_LST"));
        assertEquals((short) -999, record.get("QC_SM_5"));
        assertEquals(-999.0, record.get("SM_5"));


        record = records.get(2);
        assertNotNull(record);
        assertEquals(61, record.getValues().size());
        assertEquals(1467072000, record.get("TIME_IS"));
        assertEquals(0.443f, record.get("FIPAR_down"));
        assertEquals(0.025f, record.get("FIPAR_down_err"));
        assertEquals(-999f, record.get("FIPAR_total"));
        assertEquals(-999f, record.get("FIPAR_total_err"));
        assertEquals(0.931f, record.get("FIPAR_up"));
        assertEquals(0.021f, record.get("FIPAR_up_err"));
        assertEquals((short) 8, record.get("RM6_down_flag"));
        assertEquals((short) 8, record.get("RM6_up_flag"));
        assertEquals(-999f, record.get("Clumping_Miller"));
        assertEquals(-999f, record.get("Clumping_Miller_err"));
        assertEquals(-999f, record.get("Clumping_Warren"));
        assertEquals(-999f, record.get("Clumping_Warren_err"));
        assertEquals(.96f, record.get("LAI_Miller_down"));
        assertEquals(.034f, record.get("LAI_Miller_down_err"));
        assertEquals(5.53f, record.get("LAI_Miller_up"));
        assertEquals(.23f, record.get("LAI_Miller_up_err"));
        assertEquals(.78f, record.get("LAI_Warren_down"));
        assertEquals(0.05f, record.get("LAI_Warren_down_err"));
        assertEquals(4.2f, record.get("LAI_Warren_up"));
        assertEquals(.4f, record.get("LAI_Warren_up_err"));
        assertEquals(-999f, record.get("LAI_down"));
        assertEquals(-999f, record.get("LAI_total_Miller"));
        assertEquals(-999f, record.get("LAI_total_Warren"));
        assertEquals(.898f, record.get("LAIe_Miller_down"));
        assertEquals(.03f, record.get("LAIe_Miller_down_err"));
        assertEquals(4.05f, record.get("LAIe_Miller_up"));
        assertEquals(.22f, record.get("LAIe_Miller_up_err"));
        assertEquals(.73f, record.get("LAIe_Warren_down"));
        assertEquals(.04f, record.get("LAIe_Warren_down_err"));
        assertEquals(2.9f, record.get("LAIe_Warren_up"));
        assertEquals(.4f, record.get("LAIe_Warren_up_err"));
        assertEquals(-999f, record.get("PAI_Miller"));
        assertEquals(-999f, record.get("PAI_Miller_err"));
        assertEquals(-999f, record.get("PAI_Warren"));
        assertEquals(-999f, record.get("PAI_Warren_err"));
        assertEquals(-999f, record.get("PAIe_Miller"));
        assertEquals(-999f, record.get("PAIe_Miller_err"));
        assertEquals(-999f, record.get("PAIe_Warren"));
        assertEquals(-999f, record.get("PAIe_Warren_err"));
        assertEquals(.94f, record.get("clumping_Miller_down"));
        assertEquals(.05f, record.get("clumping_Miller_down_err"));
        assertEquals(.73f , record.get("clumping_Miller_up"));
        assertEquals(.05f, record.get("clumping_Miller_up_err"));
        assertEquals(.94f, record.get("clumping_Warren_down"));
        assertEquals(.07f, record.get("clumping_Warren_down_err"));
        assertEquals(.7f, record.get("clumping_Warren_up"));
        assertEquals(.12f, record.get("clumping_Warren_up_err"));
        assertEquals((short) 8, record.get("RM7_down_flag"));
        assertEquals((short) 8, record.get("RM7_up_flag"));
        assertEquals(-999f, record.get("LSE"));
        assertEquals(-999f, record.get("LSE_STD"));
        assertEquals(-999f, record.get("LSR"));
        assertEquals(-999f, record.get("LSR_STD"));
        assertEquals((short) -999, record.get("QC_LSE"));
        assertEquals((short) -999, record.get("QC_LSR"));
        assertEquals(-999f, record.get("LST"));
        assertEquals(-999f, record.get("LST_STD"));
        assertEquals((short) -999, record.get("QC_LST"));
        assertEquals((short) -999, record.get("QC_SM_5"));
        assertEquals(-999.0, record.get("SM_5"));
    }

    private static File getSM() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-sm-cb", "v1", "2017", "42088h2017.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getCW() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "ndbc", "ndbc-cw-ob", "v1", "2016", "42002c2016.txt"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }

    private static File getGBOV() throws IOException {
        final String relativePath = TestUtil.assembleFileSystemPath(new String[]{"insitu", "gbov", "v1", "2016", "06", "GBOV__Bartlett--Experimental--Forest__BART_047__20160601T000000Z__20160628T000000Z.csv"}, false);
        return TestUtil.getTestDataFileAsserted(relativePath);
    }
}
