/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.post.plugin.iasi;

import com.bc.fiduceo.post.PostProcessing;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AddIASISpectrumPluginTest {

    private AddIASISpectrumPlugin plugin;

    @Before
    public void setUp() {
        plugin = new AddIASISpectrumPlugin();
    }

    @Test
    public void testGetPostProcessingName() {
        assertEquals("add-iasi-spectrum", plugin.getPostProcessingName());
    }

    @Test
    public void testCreatePostProcessing() throws JDOMException, IOException {
        final Element rootElement = AddIASISpectrumTest.createFullConfigElement();

        final PostProcessing postProcessing = plugin.createPostProcessing(rootElement, null);
        assertTrue(postProcessing instanceof AddIASISpectrum);
    }
}
