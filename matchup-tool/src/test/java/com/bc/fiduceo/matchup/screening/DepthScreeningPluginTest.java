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

import com.bc.fiduceo.TestUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DepthScreeningPluginTest {

    @Test
    public void testGetScreeningName() {
        final DepthScreeningPlugin plugin = new DepthScreeningPlugin();

        assertEquals("depth", plugin.getScreeningName());
    }

    @Test
    public void testCreateScreening() throws JDOMException, IOException {
        final String XML = "<depth />";
        final Element rootElement = TestUtil.createDomElement(XML);
        assertNotNull(rootElement);
        final DepthScreeningPlugin plugin = new DepthScreeningPlugin();
        final Screening screening = plugin.createScreening(rootElement);
        assertNotNull(screening);
        assertTrue(screening instanceof DepthScreening);
    }

    @Test
    public void testCreateConfiguration_primaryName() throws JDOMException, IOException {
        final String XML = "<depth><primary-depth-variable name=\"depth1\" /></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final DepthScreening.Configuration configuration =
                DepthScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertEquals("depth1", configuration.primaryDepthName);
        assertNull(configuration.secondaryDepthName);
        assertNull(configuration.primaryIsNominal);
    }

    @Test
    public void testCreateConfiguration_secondaryName() throws JDOMException, IOException {
        final String XML = "<depth><secondary-depth-variable name=\"depth2\" /></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final DepthScreening.Configuration configuration =
                DepthScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryDepthName);
        assertEquals("depth2", configuration.secondaryDepthName);
        assertNull(configuration.primaryIsNominal);
    }

    @Test
    public void testCreateConfiguration_primaryNominal() throws JDOMException, IOException {
        final String XML =
                "<depth><primary-is-nominal>true</primary-is-nominal></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final DepthScreening.Configuration configuration =
                DepthScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryDepthName);
        assertNull(configuration.secondaryDepthName);
        assertNotNull(configuration.primaryIsNominal);
        assertTrue(configuration.primaryIsNominal);
    }

    @Test
    public void testCreateConfiguration_levels() throws JDOMException, IOException {
        final String XML =
                "<depth><levels>0.0, 10.0, 20.0, 40.0</levels></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final DepthScreening.Configuration configuration =
                DepthScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryDepthName);
        assertNull(configuration.secondaryDepthName);
        assertNull(configuration.primaryIsNominal);
        assertNotNull(configuration.levels);
        assertArrayEquals(new Double[]{0.0, 10.0, 20.0, 40.0}, configuration.levels);
    }

    @Test
    public void testCreateConfiguration_levelsNotSorted() throws JDOMException, IOException {
        final String XML =
                "<depth><levels>10.0, 0.0, 20.0, 40.0</levels></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        final DepthScreening.Configuration configuration =
                DepthScreeningPlugin.createConfiguration(rootElement);
        assertNotNull(configuration);
        assertNull(configuration.primaryDepthName);
        assertNull(configuration.secondaryDepthName);
        assertNull(configuration.primaryIsNominal);
        assertNotNull(configuration.levels);
        assertArrayEquals(new Double[]{0.0, 10.0, 20.0, 40.0}, configuration.levels);
    }

    @Test
    public void testCreateConfiguration_levelsEmpty() throws JDOMException, IOException {
        final String XML =
                "<depth><levels></levels></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            DepthScreeningPlugin.createConfiguration(rootElement);
            fail();
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testCreateConfiguration_levelsNotEnough() throws JDOMException, IOException {
        final String XML =
                "<depth><levels>10.0</levels></depth>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            DepthScreeningPlugin.createConfiguration(rootElement);
            fail();
        } catch (NumberFormatException expected) {
        }
    }
}
