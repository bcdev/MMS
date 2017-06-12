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

package com.bc.fiduceo.reader.insitu;

import com.bc.fiduceo.util.VariablePrototype;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

import java.util.ArrayList;
import java.util.List;

import static com.bc.fiduceo.util.NetCDFUtils.CF_FILL_VALUE_NAME;
import static com.bc.fiduceo.util.NetCDFUtils.CF_LONG_NAME;

class UniqueIdVariable extends VariablePrototype {

    private static final String VARIABLE_NAME = "insitu.id";

    @Override
    public String getFullName() {
        return VARIABLE_NAME;
    }

    @Override
    public String getShortName() {
        return VARIABLE_NAME;
    }

    @Override
    public DataType getDataType() {
        return DataType.LONG;
    }

    @Override
    public List<Attribute> getAttributes() {
        final ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute(CF_FILL_VALUE_NAME, -32768L));
        attributes.add(new Attribute(CF_LONG_NAME, "unique matchup ID"));
        attributes.add(new Attribute("comment", "this unique id is generated by combining YEAR, MONTH and mohc_id"));
        return attributes;
    }
}
