package com.bc.fiduceo.post.plugin.gruan_uleic;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import org.jdom2.Element;

public class AddGruanSourcePlugin implements PostProcessingPlugin {

    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        final AddGruanSource.Configuration configuration = AddGruanSource.parseConfiguration(element);
        return new AddGruanSource(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return "add-gruan-source";
    }
}
