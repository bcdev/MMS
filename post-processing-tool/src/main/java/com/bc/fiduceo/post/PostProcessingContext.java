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
package com.bc.fiduceo.post;

import com.bc.fiduceo.tool.ToolContext;

import java.nio.file.Path;

public final class PostProcessingContext extends ToolContext {

    private PostProcessingConfig processingConfig;
    private Path mmdInputDirectory;
    private Path configDirectory;

    public PostProcessingConfig getProcessingConfig() {
        return processingConfig;
    }

    public void setProcessingConfig(PostProcessingConfig processingConfig) {
        this.processingConfig = processingConfig;
    }

    public Path getMmdInputDirectory() {
        return mmdInputDirectory;
    }

    public void setMmdInputDirectory(Path mmdInputDirectory) {
        this.mmdInputDirectory = mmdInputDirectory;
    }

    public void setConfigDirectory(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }
}
