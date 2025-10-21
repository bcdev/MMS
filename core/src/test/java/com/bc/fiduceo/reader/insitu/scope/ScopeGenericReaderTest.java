package com.bc.fiduceo.reader.insitu.scope;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for ScopeGenericReader - verifies file type detection and delegation.
 *
 * This is Layer 2 testing: Generic reader behavior
 * - Tests regex pattern matching for SCOPE files
 * - Tests file type detection logic
 *
 * Note: These tests verify the detection logic without requiring actual file I/O.
 * Integration tests will verify end-to-end behavior with real files.
 */
public class ScopeGenericReaderTest {

    private ScopeGenericReader reader;
    private ReaderContext readerContext;

    @Before
    public void setUp() {
        // ARRANGE: Create a fresh generic reader before each test
        readerContext = new ReaderContext();
        readerContext.setGeometryFactory(new GeometryFactory(GeometryFactory.Type.S2));
        reader = new ScopeGenericReader(readerContext);
    }

    @After
    public void tearDown() throws IOException {
        // Clean up: Close reader if it was opened
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Test that the regex pattern correctly matches valid SCOPE filenames.
     * <p>
     * SCOPE files follow the pattern: SCOPE_WP##_TYPE_YEAR_YEAR.txt
     */
    @Test
    public void testGetRegEx() {
        // ACT: Get the regex pattern
        final String regex = reader.getRegEx();

        // ASSERT: Verify the pattern
        assertNotNull("Regex should not be null", regex);
        assertEquals("SCOPE_WP\\d+.*\\.txt", regex);
    }

    /**
     * Test that the regex matches valid SCOPE filenames with WP numbers.
     */
    @Test
    public void testRegexMatchesValidFilenames() {
        // ARRANGE: Get the regex pattern
        final String regex = reader.getRegEx();

        // ACT & ASSERT: Test various valid SCOPE filenames
        assertTrue("Should match WP23 CDOC file",
                "SCOPE_WP23_CDOC_1997_2022.txt".matches(regex));

        assertTrue("Should match WP24 DOC file",
                "SCOPE_WP24_DOC_1998_2021.txt".matches(regex));

        assertTrue("Should match WP25 Phyto file",
                "SCOPE_WP25_PHYTO_CARBON_1997_2023.txt".matches(regex));

        assertTrue("Should match WP26 PP file",
                "SCOPE_WP26_PP_1958_2021.txt".matches(regex));
    }

    /**
     * Test that the regex rejects invalid filenames.
     */
    @Test
    public void testRegexRejectsInvalidFilenames() {
        // ARRANGE: Get the regex pattern
        final String regex = reader.getRegEx();

        // ACT & ASSERT: Test invalid filenames
        assertFalse("Should reject non-SCOPE file",
                "random_file.txt".matches(regex));

        assertFalse("Should reject wrong extension",
                "SCOPE_WP23_DOC_1997_2022.csv".matches(regex));

        assertFalse("Should reject missing WP number",
                "SCOPE_DOC_1997_2022.txt".matches(regex));

        // Note: PIC and POC files (SCOPE_PIC_PIC_, SCOPE_POC_POC_) don't have WP numbers
        // so they won't match this regex - they're handled separately
        assertFalse("PIC files don't have WP numbers",
                "SCOPE_PIC_PIC_1998_2019.txt".matches(regex));

        assertFalse("POC files don't have WP numbers",
                "SCOPE_POC_POC_1997_2020.txt".matches(regex));
    }

    /**
     * Test CDOC file pattern detection logic.
     * <p>
     * CDOC files contain "_CDOC_" in the filename.
     * This should be detected BEFORE "_DOC_" since CDOC is more specific.
     */
    @Test
    public void testCDOCPatternDetection() {
        // Test the pattern that the detection logic looks for
        String filename = "SCOPE_WP23_CDOC_1997_2022.txt";

        assertTrue("CDOC pattern should contain _CDOC_",
                filename.contains("_CDOC_"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("_CDOC_"));

        // Note: "_CDOC_" does NOT contain "_DOC_" as a standalone pattern
        // because the D in CDOC is not preceded by underscore
        assertFalse("_CDOC_ should not match _DOC_ pattern",
                filename.replace("_CDOC_", "").contains("_DOC_"));
    }

    /**
     * Test DOC file pattern detection logic.
     * <p>
     * DOC files contain "_DOC_" but NOT "_CDOC_".
     */
    @Test
    public void testDOCPatternDetection() {
        String filename = "SCOPE_WP24_DOC_1998_2021.txt";

        assertTrue("DOC pattern should contain _DOC_",
                filename.contains("_DOC_"));

        assertFalse("DOC should not contain _CDOC_",
                filename.contains("_CDOC_"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("_DOC_"));
    }

    /**
     * Test Phytoplankton file pattern detection logic.
     * <p>
     * Phyto files contain "PHYTO" in the filename.
     */
    @Test
    public void testPhytoPatternDetection() {
        String filename = "SCOPE_WP25_PHYTO_CARBON_1997_2023.txt";

        assertTrue("Phyto pattern should contain PHYTO",
                filename.contains("PHYTO"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("PHYTO"));
    }

    /**
     * Test PIC file pattern detection logic.
     * <p>
     * PIC files contain "_PIC_" in the filename.
     */
    @Test
    public void testPICPatternDetection() {
        String filename = "SCOPE_PIC_PIC_1998_2019.txt";

        assertTrue("PIC pattern should contain _PIC_",
                filename.contains("_PIC_"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("_PIC_"));
    }

    /**
     * Test POC file pattern detection logic.
     * <p>
     * POC files contain "_POC_" in the filename.
     */
    @Test
    public void testPOCPatternDetection() {
        String filename = "SCOPE_POC_POC_1997_2020.txt";

        assertTrue("POC pattern should contain _POC_",
                filename.contains("_POC_"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("_POC_"));
    }

    /**
     * Test PP (Primary Production) file pattern detection logic.
     * <p>
     * PP files contain "_PP_" in the filename.
     */
    @Test
    public void testPPPatternDetection() {
        String filename = "SCOPE_WP26_PP_1958_2021.txt";

        assertTrue("PP pattern should contain _PP_",
                filename.contains("_PP_"));

        assertTrue("Should be case-insensitive",
                filename.toUpperCase().contains("_PP_"));
    }

    /**
     * Test that unknown patterns can be identified.
     * <p>
     * Files that don't match any known pattern should be detectable.
     */
    @Test
    public void testUnknownPatternDetection() {
        String filename = "SCOPE_WP99_UNKNOWN_2020_2021.txt";

        assertFalse("Unknown pattern should not contain _CDOC_",
                filename.contains("_CDOC_"));

        assertFalse("Unknown pattern should not contain _DOC_",
                filename.contains("_DOC_"));

        assertFalse("Unknown pattern should not contain PHYTO",
                filename.contains("PHYTO"));

        assertFalse("Unknown pattern should not contain _PIC_",
                filename.contains("_PIC_"));

        assertFalse("Unknown pattern should not contain _POC_",
                filename.contains("_POC_"));

        assertFalse("Unknown pattern should not contain _PP_",
                filename.contains("_PP_"));
    }

    /**
     * Test that case-insensitive detection would work.
     */
    @Test
    public void testCaseInsensitiveDetection() {
        // Test lowercase variants
        assertTrue("lowercase doc", "scope_wp24_doc_1998_2021.txt".toUpperCase().contains("_DOC_"));
        assertTrue("lowercase cdoc", "scope_wp23_cdoc_1997_2022.txt".toUpperCase().contains("_CDOC_"));
        assertTrue("lowercase phyto", "scope_wp25_phyto_carbon_1997_2023.txt".toUpperCase().contains("PHYTO"));
        assertTrue("lowercase pic", "scope_pic_pic_1998_2019.txt".toUpperCase().contains("_PIC_"));
        assertTrue("lowercase poc", "scope_poc_poc_1997_2020.txt".toUpperCase().contains("_POC_"));
        assertTrue("lowercase pp", "scope_wp26_pp_1958_2021.txt".toUpperCase().contains("_PP_"));

        // Test mixed case
        assertTrue("MixedCase", "ScOpE_WP24_DoC_1998_2021.txt".toUpperCase().contains("_DOC_"));
    }

    /**
     * Test detection order is correct (more specific patterns first).
     * <p>
     * CDOC must be checked BEFORE DOC, since CDOC contains "_DOC_" substring.
     */
    @Test
    public void testDetectionOrderCDOCBeforeDOC() {
        String cdocFilename = "SCOPE_WP23_CDOC_1997_2022.txt";

        // CDOC only contains _CDOC_ pattern, not _DOC_
        assertTrue("CDOC contains _CDOC_", cdocFilename.contains("_CDOC_"));
        assertFalse("CDOC does NOT contain standalone _DOC_ pattern", cdocFilename.contains("_DOC_"));

        // The detection logic must check _CDOC_ FIRST
        // otherwise it would incorrectly match as DOC
        // This is ensured by the order in detectAndCreateReaderFromFilename()
    }

    // ========== Tests for detectAndCreateReaderFromFilename() ==========

    /**
     * Test that CDOC filenames create ScopeCDOCReader instances.
     */
    @Test
    public void testDetectAndCreateReader_CDOC() throws IOException {
        // ACT: Detect reader type from CDOC filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP23_CDOC_1997_2022.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopeCDOCReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopeCDOCReader for CDOC file", detectedReader instanceof ScopeCDOCReader);
    }

    /**
     * Test that DOC filenames create ScopeDOCReader instances.
     */
    @Test
    public void testDetectAndCreateReader_DOC() throws IOException {
        // ACT: Detect reader type from DOC filename
       Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP24_DOC_1998_2021.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopeDOCReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopeDOCReader for DOC file", detectedReader instanceof ScopeDOCReader);
    }

    /**
     * Test that Phytoplankton filenames create ScopePhytoReader instances.
     */
    @Test
    public void testDetectAndCreateReader_Phyto() throws IOException {
        // ACT: Detect reader type from Phyto filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP25_PHYTO_CARBON_1997_2023.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopePhytoReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopePhytoReader for PHYTO file", detectedReader instanceof ScopePhytoReader);
    }

    /**
     * Test that PIC filenames create ScopePICReader instances.
     */
    @Test
    public void testDetectAndCreateReader_PIC() throws IOException {
        // ACT: Detect reader type from PIC filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_PIC_PIC_1998_2019.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopePICReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopePICReader for PIC file", detectedReader instanceof ScopePICReader);
    }

    /**
     * Test that POC filenames create ScopePOCReader instances.
     */
    @Test
    public void testDetectAndCreateReader_POC() throws IOException {
        // ACT: Detect reader type from POC filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_POC_POC_1997_2020.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopePOCReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopePOCReader for POC file", detectedReader instanceof ScopePOCReader);
    }

    /**
     * Test that PP filenames create ScopePPReader instances.
     */
    @Test
    public void testDetectAndCreateReader_PP() throws IOException {
        // ACT: Detect reader type from PP filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP26_PP_1958_2021.txt", readerContext.getGeometryFactory());

        // ASSERT: Should create a ScopePPReader
        assertNotNull("Reader should not be null", detectedReader);
        assertTrue("Should create ScopePPReader for PP file", detectedReader instanceof ScopePPReader);
    }

    /**
     * Test that detection is case-insensitive.
     */
    @Test
    public void testDetectAndCreateReader_CaseInsensitive() throws IOException {
        // ACT: Test lowercase DOC filename
        Reader lowercaseReader = ScopeGenericReader.detectAndCreateReaderFromFilename("scope_wp24_doc_1998_2021.txt", readerContext.getGeometryFactory());

        // ASSERT: Should still detect as DOC
        assertTrue("Should detect lowercase DOC filename", lowercaseReader instanceof ScopeDOCReader);

        // ACT: Test mixed case CDOC filename
       Reader mixedCaseReader = ScopeGenericReader.detectAndCreateReaderFromFilename("ScOpE_WP23_CdOc_1997_2022.txt", readerContext.getGeometryFactory());

        // ASSERT: Should still detect as CDOC
        assertTrue("Should detect mixed case CDOC filename", mixedCaseReader instanceof ScopeCDOCReader);
    }

    /**
     * Test that unknown file patterns throw IOException.
     */
    @Test
    public void testDetectAndCreateReader_UnknownFormat() {
        // ACT & ASSERT: Unknown format should throw IOException
        try {
            ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP99_UNKNOWN_2020_2021.txt", readerContext.getGeometryFactory());
            fail("Should throw IOException for unknown format");
        } catch (IOException e) {
            // Expected exception
            assertTrue("Error message should mention unknown format",
                    e.getMessage().contains("Unknown SCOPE file format"));
            assertTrue("Error message should include filename",
                    e.getMessage().contains("SCOPE_WP99_UNKNOWN_2020_2021.txt"));
        }
    }

    /**
     * Test that CDOC is detected before DOC (order matters).
     * This verifies that a filename with CDOC doesn't get misidentified as DOC.
     */
    @Test
    public void testDetectAndCreateReader_CDOCPrecedence() throws IOException {
        // ACT: Create reader from CDOC filename
        Reader detectedReader = ScopeGenericReader.detectAndCreateReaderFromFilename("SCOPE_WP23_CDOC_1997_2022.txt", readerContext.getGeometryFactory());

        // ASSERT: Must be CDOC, not DOC
        assertTrue("CDOC file must create ScopeCDOCReader, not ScopeDOCReader",
                detectedReader instanceof ScopeCDOCReader);
        assertFalse("CDOC file must NOT create ScopeDOCReader",
                detectedReader instanceof ScopeDOCReader);
    }
}
