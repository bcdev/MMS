package com.bc.fiduceo.reader.scope_sat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all SCOPE satellite reader tests together.
 * <p>
 * This suite includes:
 * - Plugin test: Validates sensor key registration
 * - Monthly reader test: Validates wp23-26, wpPIC, wpPOC
 * - Time series reader test: Validates wp21-22
 * <p>
 * To run with coverage in IDE:
 * 1. Right-click this class and select "Run with Coverage"
 * 2. Make sure VM options include: -Dcom.bc.fiduceo.product.tests.execute=true
 * <p>
 * To run from command line:
 * mvn test -Dtest=AllScopeSatReaderTests -Dcom.bc.fiduceo.product.tests.execute=true
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ScopeSatReaderPluginTest.class, ScopeSatReaderIOTest.class})
public class AllScopeSatReaderTests {
    // This class remains empty, it is used only as a holder for the above annotations
}
