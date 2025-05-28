package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.post.PostProcessing;
import com.bc.fiduceo.post.PostProcessingContext;
import com.bc.fiduceo.post.PostProcessingPlugin;
import com.bc.fiduceo.util.JDomUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.esa.snap.core.util.StringUtils.isNotNullAndNotEmpty;

public class Era5PostProcessingPlugin implements PostProcessingPlugin {

    static final String ERA5_POST_PROCESSING_GENERAL_INFO_XML = "era5-post-processing-general-info.xml";

    private static final String TAG_NAME_SATELLITE_FIELDS = "satellite-fields";

    private static final String ATT_NAME_UNITS = "units";
    private static final String ATT_NAME_LONG_NAME = "long_name";
    private static final String ATT_NAME_STANDARD_NAME = "standard_name";
    private static final String ATT_NAME_FILL_VALUE = "_FillValue";

    private static final Set<String> KNOWN_ATTRIB_NAMES = new TreeSet<>(
            Arrays.asList(ATT_NAME_UNITS, ATT_NAME_LONG_NAME, ATT_NAME_STANDARD_NAME, ATT_NAME_FILL_VALUE));

    static Configuration createConfiguration(Element rootElement, PostProcessingContext context) {
        final Configuration configuration = new Configuration();

        final String nwpAuxDirValue = JDomUtils.getMandatoryChildTextTrim(rootElement, "nwp-aux-dir");
        configuration.setNWPAuxDir(nwpAuxDirValue);

        final Element era5CollectionElement = rootElement.getChild("era5-collection");
        if (era5CollectionElement != null) {
            final String value = era5CollectionElement.getValue();
            if (isNotNullAndNotEmpty(value)) {
                configuration.setEra5Collection(value);
            }
        }

        final Element variableTranslationElement = rootElement.getChild("variable-translation");
        if (variableTranslationElement != null) {
            final String value = variableTranslationElement.getTextTrim();
            final HashSet<String> known = new HashSet<>(Arrays.asList("off", "false", "no", "dont", "not", "do not"));
            configuration.setTranslateVariableNameToFileAccessName(!known.contains(value.toLowerCase()));
        }

        parseGeneralizedInformation(configuration, context.getConfigDirectory());
        parseSatelliteFields(rootElement, configuration);
        parseMatchupFields(rootElement, configuration);

        return configuration;
    }


    static void parseSatelliteFields(Element rootElement, Configuration configuration) {
        final Element satelliteFieldsElement = rootElement.getChild(TAG_NAME_SATELLITE_FIELDS);
        if (satelliteFieldsElement != null) {
            final SatelliteFieldsConfiguration satelliteFieldsConfiguration;
            if (configuration.getSatelliteFields() != null) {
                satelliteFieldsConfiguration = configuration.getSatelliteFields();
            } else {
                satelliteFieldsConfiguration = new SatelliteFieldsConfiguration();
            }

            final Element xDimElement = satelliteFieldsElement.getChild("x_dim");
            if (xDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(xDimElement, "name");
                satelliteFieldsConfiguration.set_x_dim_name(nameElement.getValue());
                final Attribute lengthElement = JDomUtils.getMandatoryAttribute(xDimElement, "length");
                satelliteFieldsConfiguration.set_x_dim(Integer.parseInt(lengthElement.getValue()));
            }

            final Element yDimElement = satelliteFieldsElement.getChild("y_dim");
            if (yDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(yDimElement, "name");
                satelliteFieldsConfiguration.set_y_dim_name(nameElement.getValue());
                final Attribute lengthElement = JDomUtils.getMandatoryAttribute(yDimElement, "length");
                satelliteFieldsConfiguration.set_y_dim(Integer.parseInt(lengthElement.getValue()));
            }

            final Element zDimElement = satelliteFieldsElement.getChild("z_dim");
            if (zDimElement != null) {
                final Attribute nameElement = JDomUtils.getMandatoryAttribute(zDimElement, "name");
                satelliteFieldsConfiguration.set_z_dim_name(nameElement.getValue());
                final Attribute lengthElement = JDomUtils.getMandatoryAttribute(zDimElement, "length");
                satelliteFieldsConfiguration.set_z_dim(Integer.parseInt(lengthElement.getValue()));
            }

            final Element sensorRefElement = satelliteFieldsElement.getChild("sensor-ref");
            if (sensorRefElement != null) {
                satelliteFieldsConfiguration.setSensorRef(sensorRefElement.getValue());
            }

            final Set<String> varNameKeys = satelliteFieldsConfiguration.getVarNameKeys();
            for (String varNameKey : varNameKeys) {
                final Element varNameElement = satelliteFieldsElement.getChild(varNameKey);
                if (varNameElement != null) {
                    satelliteFieldsConfiguration.setVarName(varNameKey, getElementValueTrimmed(varNameElement));
                }
            }

            final Element era5TimeElement = satelliteFieldsElement.getChild("era5_time_variable");
            if (era5TimeElement != null) {
                satelliteFieldsConfiguration.set_nwp_time_variable_name(getElementValueTrimmed(era5TimeElement));
            }

            final Element lonElement = satelliteFieldsElement.getChild("longitude_variable");
            if (lonElement != null) {
                satelliteFieldsConfiguration.set_longitude_variable_name(getElementValueTrimmed(lonElement));
            }

            final Element latElement = satelliteFieldsElement.getChild("latitude_variable");
            if (latElement != null) {
                satelliteFieldsConfiguration.set_latitude_variable_name(getElementValueTrimmed(latElement));
            }

            final Element timeElement = satelliteFieldsElement.getChild("time_variable");
            if (timeElement != null) {
                satelliteFieldsConfiguration.set_time_variable_name(getElementValueTrimmed(timeElement));
            }

            configuration.setSatelliteFields(satelliteFieldsConfiguration);
        }
    }

    private static void parseMatchupFields(Element rootElement, Configuration configuration) {
        final Element matchupFieldsElements = rootElement.getChild("matchup-fields");
        if (matchupFieldsElements != null) {
            final MatchupFieldsConfiguration matchupFieldsConfiguration = new MatchupFieldsConfiguration();

            final Element insituRefElment = matchupFieldsElements.getChild("insitu-ref");
            if (insituRefElment != null) {
                matchupFieldsConfiguration.setInsituRef(insituRefElment.getValue());
            }

            final Element windUElement = matchupFieldsElements.getChild("an_sfc_u10");
            if (windUElement != null) {
                matchupFieldsConfiguration.set_an_u10_name(getElementValueTrimmed(windUElement));
            }

            final Element windVElement = matchupFieldsElements.getChild("an_sfc_v10");
            if (windVElement != null) {
                matchupFieldsConfiguration.set_an_v10_name(getElementValueTrimmed(windVElement));
            }

            final Element siconcElement = matchupFieldsElements.getChild("an_sfc_siconc");
            if (siconcElement != null) {
                matchupFieldsConfiguration.set_an_siconc_name(getElementValueTrimmed(siconcElement));
            }

            final Element sstElement = matchupFieldsElements.getChild("an_sfc_sst");
            if (sstElement != null) {
                matchupFieldsConfiguration.set_an_sst_name(getElementValueTrimmed(sstElement));
            }

            final Element metssElement = matchupFieldsElements.getChild("fc_sfc_metss");
            if (metssElement != null) {
                matchupFieldsConfiguration.set_fc_metss_name(getElementValueTrimmed(metssElement));
            }

            final Element mntssElement = matchupFieldsElements.getChild("fc_sfc_mntss");
            if (mntssElement != null) {
                matchupFieldsConfiguration.set_fc_mntss_name(getElementValueTrimmed(mntssElement));
            }

            final Element mslhfElement = matchupFieldsElements.getChild("fc_sfc_mslhf");
            if (mslhfElement != null) {
                matchupFieldsConfiguration.set_fc_mslhf_name(getElementValueTrimmed(mslhfElement));
            }

            final Element msnlwrfElement = matchupFieldsElements.getChild("fc_sfc_msnlwrf");
            if (msnlwrfElement != null) {
                matchupFieldsConfiguration.set_fc_msnlwrf_name(getElementValueTrimmed(msnlwrfElement));
            }

            final Element msnswrfElement = matchupFieldsElements.getChild("fc_sfc_msnswrf");
            if (msnswrfElement != null) {
                matchupFieldsConfiguration.set_fc_msnswrf_name(getElementValueTrimmed(msnswrfElement));
            }

            final Element msshfElement = matchupFieldsElements.getChild("fc_sfc_msshf");
            if (msshfElement != null) {
                matchupFieldsConfiguration.set_fc_msshf_name(getElementValueTrimmed(msshfElement));
            }

            final Element timeStepsPastElement = matchupFieldsElements.getChild("time_steps_past");
            if (timeStepsPastElement != null) {
                final String value = timeStepsPastElement.getValue();
                matchupFieldsConfiguration.set_time_steps_past(Integer.parseInt(value));
            }

            final Element timeStepsFutureElement = matchupFieldsElements.getChild("time_steps_future");
            if (timeStepsFutureElement != null) {
                final String value = timeStepsFutureElement.getValue();
                matchupFieldsConfiguration.set_time_steps_future(Integer.parseInt(value));
            }

            final Element timeDimNameElement = matchupFieldsElements.getChild("time_dim_name");
            if (timeDimNameElement != null) {
                matchupFieldsConfiguration.set_time_dim_name(getElementValueTrimmed(timeDimNameElement));
            }

            final Element timeVarNameElement = matchupFieldsElements.getChild("time_variable");
            if (timeVarNameElement != null) {
                matchupFieldsConfiguration.set_time_variable_name(getElementValueTrimmed(timeVarNameElement));
            }

            final Element lonVarNameElement = matchupFieldsElements.getChild("longitude_variable");
            if (lonVarNameElement != null) {
                matchupFieldsConfiguration.set_longitude_variable_name(getElementValueTrimmed(lonVarNameElement));
            }

            final Element latVarNameElement = matchupFieldsElements.getChild("latitude_variable");
            if (latVarNameElement != null) {
                matchupFieldsConfiguration.set_latitude_variable_name(getElementValueTrimmed(latVarNameElement));
            }

            final Element nwpTimeVarNameElement = matchupFieldsElements.getChild("era5_time_variable");
            if (nwpTimeVarNameElement != null) {
                matchupFieldsConfiguration.set_nwp_time_variable_name(getElementValueTrimmed(nwpTimeVarNameElement));
            }

            configuration.setMatchupFields(matchupFieldsConfiguration);
        }
    }

    // package access for testing only se 2024-02-21
    static void parseGeneralizedInformation(Configuration config, Path configPath) {
        final Path infoPath = configPath.resolve(ERA5_POST_PROCESSING_GENERAL_INFO_XML);
        if (!Files.exists(infoPath)) {
            return;
        }
        final SAXBuilder saxBuilder = new SAXBuilder();
        try (InputStream inputStream = Files.newInputStream(infoPath)) {
            final Document document = saxBuilder.build(inputStream);
            parseGeneralizedInformation(document.getRootElement(), config);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create input stream from " + infoPath + ".", e);
        } catch (JDOMException e) {
            throw new RuntimeException("XML document " + infoPath + " could not be read in.", e);
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage() + " in " + infoPath, e);
        }
    }

    // package access for testing only se 2024-02-21
    static void parseGeneralizedInformation(Element root, Configuration config) throws Throwable {
        if (root == null || !"era5".equals(root.getName())) {
            throw new Throwable("Root tag <era5> expected");
        }
        final Element satFieldsElem = root.getChild(TAG_NAME_SATELLITE_FIELDS);
        if (satFieldsElem != null) {
            final Map<String, TemplateVariable> templateVariables = new LinkedHashMap<>();
            final List<Element> collections = satFieldsElem.getChildren("collection");
            for (Element collection : collections) {
                final String collectionName = JDomUtils.getValueFromNameAttributeMandatory(collection).trim();
                final Element is3d_E = collection.getChild("is3d");
                final boolean is3d = is3d_E != null && "true".equalsIgnoreCase(is3d_E.getTextTrim());
                final List<Element> variables = collection.getChildren("var");
                for (Element var_E : variables) {
                    final HashMap<String, String> attributes = new HashMap<>();
                    final String varName = JDomUtils.getValueFromNameAttributeMandatory(var_E);
                    final List<Element> attribElems = var_E.getChildren("att");
                    for (Element attrib_E : attribElems) {
                        final String attName = JDomUtils.getValueFromNameAttributeMandatory(attrib_E).trim();
                        if (!KNOWN_ATTRIB_NAMES.contains(attName)) {
                            throw new Throwable("Unknown attribute name " + attName);
                        }
                        final String value = attrib_E.getTextTrim();
                        if (isNotNullAndNotEmpty(attName) && isNotNullAndNotEmpty(value)) {
                            attributes.put(attName, value);
                        }
                    }
                    final String templateKey = collectionName + "_" + varName;
                    final TemplateVariable templateVariable = new TemplateVariable(
                            varName,
                            attributes.containsKey(ATT_NAME_UNITS) ? attributes.get(ATT_NAME_UNITS) : "~",
                            attributes.get(ATT_NAME_LONG_NAME),
                            attributes.get(ATT_NAME_STANDARD_NAME),
                            is3d);
                    if (attributes.containsKey(ATT_NAME_FILL_VALUE)) {
                        templateVariable.setFill_value(Float.valueOf(attributes.get(ATT_NAME_FILL_VALUE)));
                    }
                    templateVariables.put(
                            templateKey,
                            templateVariable
                    );
                }
            }
            if (!templateVariables.isEmpty()) {
                SatelliteFieldsConfiguration satFields = config.getSatelliteFields();
                if (satFields == null) {
                    satFields = new SatelliteFieldsConfiguration();
                }
                satFields.setGeneralizedVariables(templateVariables);
                config.setSatelliteFields(satFields);
            }
        }
    }

    private static String getElementValueTrimmed(Element element) {
        return element.getValue().trim();
    }

    @Override
    public PostProcessing createPostProcessing(Element element, PostProcessingContext context) {
        final Configuration configuration = createConfiguration(element, context);
        return new Era5PostProcessing(configuration);
    }

    @Override
    public String getPostProcessingName() {
        return "era5";
    }
}
