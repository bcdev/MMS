/*
 * Copyright (C) 2025 Brockmann Consult GmbH
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
package com.bc.fiduceo.matchup.screening;

import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.Arrays;
import java.util.stream.IntStream;

/* The XML template for this screening class looks like:

    <depth>
        <!-- omit if name of primary depth is `depth` -->
        <primary-depth-variable name="depth1" />

        <!-- omit if name of secondary depth is `depth` -->
        <secondary-depth-variable name="depth2" />

        <!-- omit if primary depth is not provided at nominal depth levels -->
        <primary-is-nominal>true</primary-is-nominal>

        <!-- omit to use default nominal depth levels (listed below, for instance) -->
        <levels>0.0, 10.0, 20.0, 30.0, 50.0, 75.0, 100.0, 125.0, 150.0, 200.0, 250.0, 300.0, 400.0, 500.0, 600.0,
        700.0, 800.0, 900.0, 1000.0, 1100.0, 1200.0, 1300.0, 1400.0, 1500.0, 1750.0, 2000.0, 2500.0, 3000.0, 3500.0,
        4000.0, 4500.0, 5000.0, 5500.0</levels>
    </depth>
 */

public class DepthScreeningPlugin implements ScreeningPlugin {

    // package access for testing only
    static DepthScreening.Configuration createConfiguration(Element element) {
        final DepthScreening.Configuration configuration = new DepthScreening.Configuration();

        final Element primaryDepthVariable = element.getChild("primary-depth-variable");
        if (primaryDepthVariable != null) {
            final Attribute name = primaryDepthVariable.getAttribute("name");
            if (name != null) {
                configuration.primaryDepthName = name.getValue();
            }
        }

        final Element secondaryDepthVariable = element.getChild("secondary-depth-variable");
        if (secondaryDepthVariable != null) {
            final Attribute name = secondaryDepthVariable.getAttribute("name");
            if (name != null) {
                configuration.secondaryDepthName = name.getValue();
            }
        }

        final Element primaryIsNominal = element.getChild("primary-is-nominal");
        if (primaryIsNominal != null) {
            configuration.primaryIsNominal = Boolean.parseBoolean(primaryIsNominal.getValue().trim());
        }

        final Element levels = element.getChild("levels");
        if (levels != null) {
            final String[] tokens = levels.getValue().split(",");
            if (tokens.length < 2) {
                throw new NumberFormatException(levels.getValue());
            }
            configuration.levels = new Double[tokens.length];
            IntStream.range(0, tokens.length).forEach(i -> configuration.levels[i] =
                    Double.parseDouble(tokens[i].trim()));
            Arrays.sort(configuration.levels);
        }

        return configuration;
    }

    @Override
    public Screening createScreening(Element element) {
        final DepthScreening.Configuration configuration = createConfiguration(element);
        final DepthScreening screening = new DepthScreening();
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "depth";
    }

}
