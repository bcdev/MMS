package com.bc.fiduceo.reader.insitu.scope;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all SCOPE reader tests together.
 * This ensures all reader implementations get coverage when run as a group.
 *
 * To run with coverage in IDE:
 * 1. Right-click this class and select "Run with Coverage"
 * 2. Make sure VM options include: -Dcom.bc.fiduceo.product.tests.execute=true
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ScopeGenericReaderTest.class,
    ScopeGenericReader_IO_Test.class,
    ScopeReaderPluginTest.class,
    ScopePPReader_IO_Test.class,
    ScopeDOCReader_IO_Test.class,
    ScopeCDOCReader_IO_Test.class,
    ScopePhytoReader_IO_Test.class,
    ScopePICReader_IO_Test.class,
    ScopePOCReader_IO_Test.class
})
public class AllScopeReaderTests {
    // This class remains empty, it is used only as a holder for the above annotations
}
