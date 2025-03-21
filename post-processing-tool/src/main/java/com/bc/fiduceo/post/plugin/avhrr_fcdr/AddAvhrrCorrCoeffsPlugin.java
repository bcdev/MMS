package com.bc.fiduceo.post.plugin.avhrr_fcdr;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom2.Element;

public class AddAvhrrCorrCoeffsPlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        final AddAvhrrCorrCoeffs.Configuration configuration = AddAvhrrCorrCoeffs.createConfiguration(element);
        return new AddAvhrrCorrCoeffs(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return "add-avhrr-corr-coeffs";
    }
}
