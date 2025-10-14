package com.bc.fiduceo.reader.scope_sat;

import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

/**
 * Plugin for SCOPE satellite data reader.
 *
 * Uses a generic reader that auto-detects file type (monthly vs time-series).
 *
 * Supported products:
 * - wp21 (Fugacity of CO2) - time series
 * - wp22 (Dissolved Inorganic Carbon) - time series with depth
 * - wp23 (Coastal Dissolved Organic Carbon) - monthly composites
 * - wp24 (Dissolved Organic Carbon) - monthly composites
 * - wp25 (Phytoplankton Carbon) - monthly composites
 * - wp26 (Primary Production) - monthly composites
 * - wpPIC (Particulate Inorganic Carbon) - monthly composites
 * - wpPOC (Particulate Organic Carbon) - monthly composites
 */
public class ScopeSatReaderPlugin implements ReaderPlugin {

    // Sensor keys for different SCOPE satellite products
    private static final String[] SUPPORTED_SENSOR_KEYS = {
            "scope-sat-fco2",             // wp21 - Fugacity of CO2
            "scope-sat-dic",              // wp22 - Dissolved Inorganic Carbon
            "scope-sat-coastal-doc",      // wp23 - Coastal Dissolved Organic Carbon
            "scope-sat-doc",              // wp24 - Dissolved Organic Carbon
            "scope-sat-phytoplankton",    // wp25 - Phytoplankton Carbon
            "scope-sat-pp",               // wp26 - Primary Production
            "scope-sat-pic",              // wpPIC - Particulate Inorganic Carbon
            "scope-sat-poc"               // wpPOC - Particulate Organic Carbon
    };

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new ScopeSatGenericReader(readerContext);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SUPPORTED_SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
