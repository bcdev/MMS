package com.bc.fiduceo.post.plugin;


import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.esa.snap.core.util.StringUtils;
import org.jdom2.Element;

import java.util.List;

/* The XML template for this post processing class looks like:

    <elevation-to-solzen-angle>
        <convert source-name = "elevation_angle_1" target-name = "zenith_angle_1" remove-source = "false"/>
        <convert source-name = "elevation_angle_2" target-name = "zenith_angle_2" remove-source = "true"/>
    </elevation-to-solzen-angle>
 */

public class ElevationToSolZenAnglePlugin implements PostProcessingPlugin {

    private static final String ROOT_TAG_NAME = "elevation-to-solzen-angle";

    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        final ElevationToSolZenAngle.Configuration configuration = createConfiguration(element);
        return new ElevationToSolZenAngle(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return ROOT_TAG_NAME;
    }

    // package access for testing only tb 2017-06-01
    static ElevationToSolZenAngle.Configuration createConfiguration(Element rootElement) {
        if (!ROOT_TAG_NAME.equals(rootElement.getName())) {
            throw new RuntimeException("Illegal XML Element. Tagname '" + ROOT_TAG_NAME + "' expected.");
        }

        final ElevationToSolZenAngle.Configuration configuration = new ElevationToSolZenAngle.Configuration();

        final List<Element> convertElements = JDomUtils.getMandatoryChildren(rootElement, "convert");
        for (final Element convertElement : convertElements) {

            final String sourceName = JDomUtils.getValueFromAttributeMandatory(convertElement, "source-name");
            final String targetName = JDomUtils.getValueFromAttributeMandatory(convertElement, "target-name");

            final String removeSource = convertElement.getAttributeValue("remove-source");
            final boolean remove = StringUtils.isNullOrEmpty(removeSource) || Boolean.parseBoolean(removeSource);

            configuration.conversions.add(new ElevationToSolZenAngle.Conversion(sourceName, targetName, remove));
        }
        return configuration;
    }
}
