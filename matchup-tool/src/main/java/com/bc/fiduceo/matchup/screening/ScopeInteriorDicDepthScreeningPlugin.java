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

/* The XML template for this screening class looks like:

    <scope-interior-dic-depth>
        <!-- omit if name of primary depth is `depth` -->
        <primary-depth-variable name="depth1" />

        <!-- omit if name of secondary depth is `depth` -->
        <secondary-depth-variable name="depth2" />

        <!-- omit if primary depth is not provided at nominal discrete depth levels -->
        <primary-is-nominal>true</primary-is-nominal>
    </scope-interior-dic-depth>
 */

public class ScopeInteriorDicDepthScreeningPlugin implements ScreeningPlugin {

    // package access for testing only
    static ScopeInteriorDicDepthScreening.Configuration createConfiguration(Element element) {
        final ScopeInteriorDicDepthScreening.Configuration configuration =
                new ScopeInteriorDicDepthScreening.Configuration();

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

        return configuration;
    }

    @Override
    public Screening createScreening(Element element) {
        final ScopeInteriorDicDepthScreening.Configuration configuration = createConfiguration(element);
        final ScopeInteriorDicDepthScreening screening = new ScopeInteriorDicDepthScreening();
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "scope-interior-dic-depth";
    }

}
