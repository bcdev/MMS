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

package com.bc.fiduceo.post.plugin.caliop.flag;

import static com.bc.fiduceo.util.JDomUtils.*;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom2.Element;


public class CALIOP_L2_VFM_FLAGS_PPPlugin implements PostProcessingPlugin {

    static final String TAG_POST_PROCESSING_NAME = "caliop-level2-vfm-flags";
    static final String TAG_MMD_SOURCE_FILE_VARIABE_NAME = "mmd-source-file-variable-name";
    static final String TAG_MMD_PROCESSING_VERSION_VARIABE_NAME = "mmd-processing-version-variable-name";
    static final String TAG_MMD_Y_VARIABE_NAME = "mmd-y-variable-name";
    static final String TAG_TARGET_FCF_VARIABLE_NAME = "target-fcf-variable-name";


    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        if (!getPostProcessingName().equals(element.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + getPostProcessingName() + "' expected.");
        }

        final String srcVariableName_fileName = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_SOURCE_FILE_VARIABE_NAME);
        final String srcVariableName_processingVersion = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_PROCESSING_VERSION_VARIABE_NAME);
        final String srcVariableName_y = getMandatoryChildMandatoryTextTrim(element, TAG_MMD_Y_VARIABE_NAME);
        final String targetVariableName = getMandatoryChildMandatoryTextTrim(element, TAG_TARGET_FCF_VARIABLE_NAME);


        return new CALIOP_L2_VFM_FLAGS_PP(srcVariableName_fileName,
                                          srcVariableName_processingVersion,
                                          srcVariableName_y,
                                          targetVariableName);
    }

    @Override
    public String getPostProcessingName() {
        return TAG_POST_PROCESSING_NAME;
    }
}
