package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.TestUtil;
import com.google.common.jimfs.Jimfs;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class Era5PostProcessingPluginTest_parseGeneralizedInformations {

    @Test
    public void testParseGeneralizedInformation_unableToCreateInputStream_mockIOException() {
        //preparation
        final Path configPathMock = Mockito.mock(Path.class);
        final Path infoPathMock = Mockito.mock(Path.class);
        when(configPathMock.resolve(Mockito.anyString())).thenReturn(infoPathMock);
        when(infoPathMock.toString()).thenReturn("somePathName");
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(infoPathMock)).thenReturn(true);
            filesMock.when(() -> Files.newInputStream(infoPathMock)).thenThrow(new IOException("mocked"));

            //execution
            Era5PostProcessingPlugin.parseGeneralizedInformation(new Configuration(), configPathMock);

            //verification
            fail("RuntimeException expected");
        } catch (Throwable e) {
            assertThat(e.getClass(), is(equalTo(RuntimeException.class)));
            assertEquals("Unable to create input stream from somePathName.", e.getMessage());
            final Throwable cause = e.getCause();
            assertThat(cause.getClass(), is(equalTo(IOException.class)));
            assertEquals("mocked", cause.getMessage());
        }
    }

    @Test
    public void testParseGeneralizedInformation_generalInfoIsNotValidXml() throws IOException {
        //preparation
        final String XML = "habi dubi \n" +
                           " babi   <satellite-fields>\n" +
                           "    </satellite-fields>\n" +
                           "</lof>";
        final Path configPath = Jimfs.newFileSystem().getPath("config");

        final Path xmlPath = configPath.resolve(Era5PostProcessingPlugin.ERA5_POST_PROCESSING_GENERAL_INFO_XML);
        Files.createDirectories(configPath);
        try (
                OutputStream os = Files.newOutputStream(xmlPath);
                PrintWriter pw = new PrintWriter(os)
        ) {
            pw.println(XML);
        }

        try {
            //execution
            Era5PostProcessingPlugin.parseGeneralizedInformation(new Configuration(), configPath);

            //verification
            fail("RuntimeException expected");
        } catch (Throwable e) {
            assertThat(e.getClass(), is(equalTo(RuntimeException.class)));
            assertEquals("XML document " + xmlPath + " could not be read in.", e.getMessage());
            final Throwable cause = e.getCause();
            assertThat(cause.getClass(), is(equalTo(JDOMParseException.class)));
            assertThat(cause.getMessage(), startsWith("Error on line 1:"));
        }
    }

    @Test
    public void testParseGeneralizedInformation_fromFile_rootTagIsNot_era5() throws IOException, JDOMException {
        //preparation
        final String XML = "<era5_>\n" +
                           "    <satellite-fields>\n" +
                           "    </satellite-fields>\n" +
                           "</era5_>";
        final Path configPath = output(XML);

        try {
            //execution
            Era5PostProcessingPlugin.parseGeneralizedInformation(new Configuration(), configPath);

            //verification
            fail("RuntimeException expected");
        } catch (Throwable e) {
            assertThat(e.getClass(), is(equalTo(RuntimeException.class)));
            assertEquals("Root tag <era5> expected in config\\era5-post-processing-general-info.xml", e.getMessage());
            final Throwable cause = e.getCause();
            assertThat(cause.getClass(), is(equalTo(Throwable.class)));
            assertEquals("Root tag <era5> expected", cause.getMessage());
        }
    }

    @Test
    public void testParseGeneralizedInformation_rootTagIsNot_era5() throws IOException, JDOMException {
        //preparation
        final String XML = "<era5_>\n" +
                           "    <satellite-fields>\n" +
                           "    </satellite-fields>\n" +
                           "</era5_>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            //execution
            Era5PostProcessingPlugin.parseGeneralizedInformation(rootElement, new Configuration());

            //verification
            fail("Throwable expected");
        } catch (Throwable e) {
            assertThat(e.getClass(), is(equalTo(Throwable.class)));
            assertEquals("Root tag <era5> expected", e.getMessage());
        }
    }

    @Test
    public void testParseGeneralizedInformation_unknownAttributeName() throws IOException, JDOMException {
        //preparation
        final String XML = "<era5>\n" +
                           "    <satellite-fields>\n" +
                           "        <collection name=\"an_pl\">\n" +
                           "            <is3d>true</is3d>\n" +
                           "            <var name=\"t\">\n" +
                           "                <att name=\"Pumpernikel\">K</att>\n" +
                           "            </var>\n" +
                           "        </collection>\n" +
                           "    </satellite-fields>\n" +
                           "</era5>";
        final Element rootElement = TestUtil.createDomElement(XML);

        try {
            //execution
            Era5PostProcessingPlugin.parseGeneralizedInformation(rootElement, new Configuration());

            //verification
            fail("Throwable expected");
        } catch (Throwable e) {
            assertThat(e.getClass(), is(equalTo(Throwable.class)));
            assertEquals("Unknown attribute name Pumpernikel", e.getMessage());
        }
    }

    @Test
    public void testParseGeneralizedInformation_noGenaralInformationFile() throws IOException, JDOMException {
        //preparation
        final Path configPath = Jimfs.newFileSystem().getPath("config");
        Files.createDirectories(configPath);
        final Configuration config = new Configuration();

        //execution
        Era5PostProcessingPlugin.parseGeneralizedInformation(config, configPath);

        //verification
        assertThat(config.getSatelliteFields(), is(nullValue()));
    }

    @Test
    public void testParseGeneralizedInformation_readFromFile() throws IOException, JDOMException {
        //preparation
        final String XML = "<era5>\n" +
                           "    <satellite-fields>\n" +
                           "        <collection name=\"an_pl\">\n" +
                           "            <is3d>true</is3d>\n" +
                           "            <var name=\"t\">\n" +
                           "                <att name=\"units\">K</att>\n" +
                           "                <att name=\"long_name\">Temperature</att>\n" +
                           "                <att name=\"standard_name\">air_temperature</att>\n" +
                           "                <att name=\"_FillValue\">24.3</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"q\">\n" +
                           "                <att name=\"units\">kg kg**-1</att>\n" +
                           "                <att name=\"long_name\">Specific humidity</att>\n" +
                           "                <att name=\"standard_name\">specific_humidity</att>\n" +
                           "                <att name=\"_FillValue\">24.3e-5</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"clwc\">\n" +
                           "                <att name=\"units\">kg kg**-1</att>\n" +
                           "                <att name=\"long_name\">Specific cloud liquid water content</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"crwc\">\n" +
                           "                <att name=\"units\">kg kg**-1</att>\n" +
                           "                <att name=\"long_name\">Specific rain water content</att>\n" +
                           "            </var>\n" +
                           "        </collection>\n" +
                           "        <collection name=\"an_sfc\">\n" +
                           "            <var name=\"tclw\">\n" +
                           "                <att name=\"units\">kg m**-2</att>\n" +
                           "                <att name=\"long_name\">Total column cloud liquid water</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"tciw\">\n" +
                           "                <att name=\"units\">kg m**-2</att>\n" +
                           "                <att name=\"long_name\">Total column cloud ice water</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"tcrw\">\n" +
                           "                <att name=\"units\">kg m**-2</att>\n" +
                           "                <att name=\"long_name\">Total column rain water</att>\n" +
                           "            </var>\n" +
                           "            <var name=\"tcsw\">\n" +
                           "                <att name=\"units\">kg m**-2</att>\n" +
                           "                <att name=\"long_name\">Total column snow water</att>\n" +
                           "            </var>\n" +
                           "        </collection>\n" +
                           "    </satellite-fields>\n" +
                           "</era5>";
        final Path configPath = output(XML);
        final Configuration configuration = new Configuration();

        //execution
        Era5PostProcessingPlugin.parseGeneralizedInformation(configuration, configPath);

        //verification
        final SatelliteFieldsConfiguration satConf = configuration.getSatelliteFields();
        assertNotNull(satConf);
        final Map<String, TemplateVariable> genVars = satConf.getGeneralizedVariables();
        assertNotNull(genVars);
        assertThat(genVars.size(), is(8));

        TemplateVariable var = genVars.get("an_pl_t");
        assertNotNull(var);
        assertThat(var.getName(), is("t"));
        assertThat(var.getUnits(), is("K"));
        assertThat(var.getLongName(), is("Temperature"));
        assertThat(var.getStandardName(), is("air_temperature"));
        assertThat(var.getFillValue(), is(24.3f));

        var = genVars.get("an_pl_q");
        assertNotNull(var);
        assertThat(var.getName(), is("q"));
        assertThat(var.getUnits(), is("kg kg**-1"));
        assertThat(var.getLongName(), is("Specific humidity"));
        assertThat(var.getStandardName(), is("specific_humidity"));
        assertThat(var.getFillValue(), is(24.3e-5f));
        assertThat(var.is3d(), is(true));

        var = genVars.get("an_pl_clwc");
        assertNotNull(var);
        assertThat(var.getName(), is("clwc"));
        assertThat(var.getUnits(), is("kg kg**-1"));
        assertThat(var.getLongName(), is("Specific cloud liquid water content"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(true));

        var = genVars.get("an_pl_crwc");
        assertNotNull(var);
        assertThat(var.getName(), is("crwc"));
        assertThat(var.getUnits(), is("kg kg**-1"));
        assertThat(var.getLongName(), is("Specific rain water content"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(true));

        var = genVars.get("an_sfc_tclw");
        assertNotNull(var);
        assertThat(var.getName(), is("tclw"));
        assertThat(var.getUnits(), is("kg m**-2"));
        assertThat(var.getLongName(), is("Total column cloud liquid water"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(false));

        var = genVars.get("an_sfc_tciw");
        assertNotNull(var);
        assertThat(var.getName(), is("tciw"));
        assertThat(var.getUnits(), is("kg m**-2"));
        assertThat(var.getLongName(), is("Total column cloud ice water"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(false));

        var = genVars.get("an_sfc_tcrw");
        assertNotNull(var);
        assertThat(var.getName(), is("tcrw"));
        assertThat(var.getUnits(), is("kg m**-2"));
        assertThat(var.getLongName(), is("Total column rain water"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(false));

        var = genVars.get("an_sfc_tcsw");
        assertNotNull(var);
        assertThat(var.getName(), is("tcsw"));
        assertThat(var.getUnits(), is("kg m**-2"));
        assertThat(var.getLongName(), is("Total column snow water"));
        assertThat(var.getStandardName(), is(nullValue()));
        assertThat(var.getFillValue(), is(9.96921E36F));
        assertThat(var.is3d(), is(false));
    }

    private static Path output(String XML) throws JDOMException, IOException {
        final Element rootElement = TestUtil.createDomElement(XML);
        final Path configPath = Jimfs.newFileSystem().getPath("config");
        final Path xmlPath = configPath.resolve(Era5PostProcessingPlugin.ERA5_POST_PROCESSING_GENERAL_INFO_XML);
        Files.createDirectories(configPath);
        try (OutputStream os = Files.newOutputStream(xmlPath)) {
            new XMLOutputter(Format.getPrettyFormat())
                    .output(rootElement, os);
        }
        return configPath;
    }
}

