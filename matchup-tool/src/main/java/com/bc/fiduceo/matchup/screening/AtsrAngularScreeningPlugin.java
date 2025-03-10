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

package com.bc.fiduceo.matchup.screening;

import org.esa.snap.core.util.StringUtils;
import org.jdom2.Element;

public class AtsrAngularScreeningPlugin implements ScreeningPlugin {

    @Override
    public Screening createScreening(Element element) {
        final AtsrAngularScreening.Configuration configuration = createConfiguration(element);
        final AtsrAngularScreening screening = new AtsrAngularScreening();
        screening.configure(configuration);
        return screening;
    }

    @Override
    public String getScreeningName() {
        return "atsr-angular";
    }

    static AtsrAngularScreening.Configuration createConfiguration(Element rootElement) {
        final AtsrAngularScreening.Configuration configuration = new AtsrAngularScreening.Configuration();

        final Element angleFwardDeltaElement = rootElement.getChild("angle-delta-fward");
        if (angleFwardDeltaElement != null) {
            final String value = angleFwardDeltaElement.getValue();
            if (StringUtils.isNotNullAndNotEmpty(value)) {
                configuration.angleDeltaFward = Double.parseDouble(value);
            }
        }

        final Element angleNadirDeltaElement = rootElement.getChild("angle-delta-nadir");
        if (angleNadirDeltaElement != null) {
            final String value = angleNadirDeltaElement.getValue();
            if (StringUtils.isNotNullAndNotEmpty(value)) {
                configuration.angleDeltaNadir = Double.parseDouble(value);
            }
        }
        return configuration;
    }
}
