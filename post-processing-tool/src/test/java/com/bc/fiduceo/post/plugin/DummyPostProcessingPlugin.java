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
 */
package com.bc.fiduceo.post.plugin;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom2.Element;

public class DummyPostProcessingPlugin implements PostProcessingPlugin {

    public static final String DUMMY_POST_PROCESSING_NAME = "dummy-post-processing";

    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        return new DummyPostProcessing(element.getValue());
    }

    @Override
    public String getPostProcessingName() {
        return DUMMY_POST_PROCESSING_NAME;
    }
}
