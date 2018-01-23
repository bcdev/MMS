/*
 * Copyright (C) 2016 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.hirs;

import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.reader.DataType;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderPlugin;

public class HIRS_L1C_ReaderPlugin implements ReaderPlugin {

    // abbreviations found in the filename        M2         M1         NA          NC          NE          NF          NG          NH          ND          NJ          NK          NL          NM          NN          NP          TN
    private static final String[] SENSOR_KEYS = {"hirs-ma", "hirs-mb", "hirs-n06", "hirs-n07", "hirs-n08", "hirs-n09", "hirs-n10", "hirs-n11", "hirs-n12", "hirs-n14", "hirs-n15", "hirs-n16", "hirs-n17", "hirs-n18", "hirs-n19", "hirs-tn"};

    @Override
    public Reader createReader(ReaderContext readerContext) {
        return new HIRS_L1C_Reader(readerContext);
    }

    @Override
    public String[] getSupportedSensorKeys() {
        return SENSOR_KEYS;
    }

    @Override
    public DataType getDataType() {
        return DataType.POLAR_ORBITING_SATELLITE;
    }
}
