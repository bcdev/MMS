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

package com.bc.fiduceo.post;

import com.bc.fiduceo.FiduceoConstants;
import com.bc.fiduceo.IOTestRunner;
import com.bc.fiduceo.NCTestUtils;
import com.bc.fiduceo.TestUtil;
import org.esa.snap.core.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(IOTestRunner.class)
public class PostProcessingToolIntegrationTest_Era5 {

    private File configDir;
    private File testDirectory;

    @Before
    public void setUp() throws IOException {
        testDirectory = TestUtil.createTestDirectory();
        configDir = new File(testDirectory, "config");
        if (!configDir.mkdir()) {
            fail("unable to create test directory: " + configDir.getAbsolutePath());
        }

        TestUtil.writeSystemConfig(configDir);
    }

    @After
    public void tearDown() {
        TestUtil.deleteTestDirectory();
    }

    @Test
    public void testAddEra5Variables_mmd15() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_mmd15();

        writeConfiguration_mmd15();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "mmd15_sst_drifter-sst_amsre-aq_caliop_vfm-cal_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("amsre\\.Geostationary_Reflection_Latitude", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 4105, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 4087, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 7, mmd);

            // satellite fields
            NCTestUtils.assertDimension("left", 5, mmd);
            NCTestUtils.assertDimension("right", 7, mmd);
            NCTestUtils.assertDimension("up", 23, mmd);

            variable = NCTestUtils.getVariable("nwp_q", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 0, 0, 2.067875129796448E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 10, 0, 4.002843979833415E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 2, 0, 20, 0, 3.6158501188765513E-6, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Logarithm of surface pressure");
            NCTestUtils.assert3DValueDouble(3, 1, 1, 11.530668258666992, variable);
            NCTestUtils.assert3DValueDouble(3, 2, 1, 11.530518531799316, variable);
            NCTestUtils.assert3DValueDouble(3, 3, 1, 11.530460357666016, variable);

            variable = NCTestUtils.getVariable("nwp_v10", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert3DValueDouble(4, 2, 2, 0.8991702795028687, variable);
            NCTestUtils.assert3DValueDouble(4, 3, 2, 1.0558509826660156, variable);
            NCTestUtils.assert3DValueDouble(4, 4, 2, 1.1961302757263184, variable);

            variable = NCTestUtils.getVariable("nwp_sst", mmd);
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.969209968386869E36");
            NCTestUtils.assert3DValueDouble(0, 3, 3, 275.7586975097656, variable);
            NCTestUtils.assert3DValueDouble(0, 4, 3, 275.5484924316406, variable);
            NCTestUtils.assert3DValueDouble(0, 5, 3, 275.3242492675781, variable);

            variable = NCTestUtils.getVariable("era5-time", mmd);
            NCTestUtils.assertAttribute(variable, "units", "seconds since 1970-01-01");
            NCTestUtils.assert1DValueLong(2, 1212400800, variable);
            NCTestUtils.assert1DValueLong(6, 1212145200, variable);

            // matchup fields
            NCTestUtils.assertDimension("the_time", 54, mmd);

            variable = NCTestUtils.getVariable("era5-mu-time", mmd);
            NCTestUtils.assertAttribute(variable, "units", "seconds since 1970-01-01");
            NCTestUtils.assert2DValueInt(1, 1, 959796000, variable);
            NCTestUtils.assert2DValueInt(2, 2, 959803200, variable);
            NCTestUtils.assert2DValueInt(3, 2, 959806800, variable);

            variable = NCTestUtils.getVariable("nwp_mu_u10", mmd);
            NCTestUtils.assertAttribute(variable, "units", "m s**-1");
            NCTestUtils.assert2DValueFloat(4, 3, 1.5980221033096313f, variable);
            NCTestUtils.assert2DValueFloat(5, 3, 1.4887735843658447f, variable);
            NCTestUtils.assert2DValueFloat(6, 3, 1.3267265558242798f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_sst", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Sea surface temperature");
            NCTestUtils.assert2DValueFloat(7, 4, 284.4002990722656f, variable);
            NCTestUtils.assert2DValueFloat(8, 4, 284.400390625f, variable);
            NCTestUtils.assert2DValueFloat(9, 4, 284.4002380371094f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_mslhf", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert2DValueFloat(10, 5, -83.63811492919922f, variable);
            NCTestUtils.assert2DValueFloat(11, 5, -85.87189483642578f, variable);
            NCTestUtils.assert2DValueFloat(12, 5, -87.71128845214844f, variable);

            variable = NCTestUtils.getVariable("nwp_mu_msshf", mmd);
            NCTestUtils.assertAttribute(variable, "_FillValue", "9.969209968386869E36");
            NCTestUtils.assert2DValueFloat(13, 6, -8.905862808227539f, variable);
            NCTestUtils.assert2DValueFloat(14, 6, -9.198946952819824f, variable);
            NCTestUtils.assert2DValueFloat(15, 6, -10.215385437011719f, variable);
        }
    }

    @Test
    public void testAddEra5Variables_coo1() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_coo1();

        writeConfiguration_coo1(false);

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "coo_1_slstr-s3a-nt_avhrr-frac-ma_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("avhrr-frac-ma_delta_azimuth", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 11.972550392150879, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 11.975187301635742, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 1, mmd);

            // satellite fields
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_x", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_y", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_z", 137, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "units", "~");
            NCTestUtils.assert3DVariable(variable.getFullName(), 0, 0, 0, 11.525882720947266, mmd);

            variable = NCTestUtils.getVariable("nwp_o3", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 0, 0, 1.9407424645123683E-7, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 10, 0, 3.718567541000084E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 20, 0, 9.968232006940525E-6, mmd);

            variable = NCTestUtils.getVariable("nwp_u10", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert3DValueDouble(0, 0, 0, -0.9742769598960876, variable);

            variable = NCTestUtils.getVariable("nwp_skt", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Skin temperature");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 301.2406311035156, variable);

            variable = NCTestUtils.getVariable("slstr.s3a.blowVert", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "10 metre V wind component");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 3.4361371994018555, variable);
        }
    }

    @Test
    public void testAddEra5Variables_coo1_overwrite() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_coo1();

        // copy input to test-directory as we want to overwrite the MMD
        copyFilesTo(inputDir, testDirectory);

        writeConfiguration_coo1(true);

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", testDirectory.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "coo_1_slstr-s3a-nt_avhrr-frac-ma_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("avhrr-frac-ma_delta_azimuth", mmd, false);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 11.975187301635742, variable);
            NCTestUtils.assert3DValueDouble(2, 0, 0, 11.977825164794922, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 1, mmd);

            // satellite fields
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_x", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_y", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_z", 137, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "units", "~");
            NCTestUtils.assert3DVariable(variable.getFullName(), 0, 0, 0, 11.525882720947266, mmd);

            variable = NCTestUtils.getVariable("nwp_o3", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 30, 0, 1.5600191545672715E-5, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 40, 0, 7.563196959381457E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 50, 0, 1.5587879715894815E-6, mmd);
        }
    }

    @Test
    public void testAddEra5Variables_coo1_using_sensorRef() throws IOException, InvalidRangeException {
        final File inputDir = getInputDirectory_coo1();

        writeConfiguration_coo1_with_sensorRef();

        final String[] args = new String[]{"-c", configDir.getAbsolutePath(), "-start", "2008-149", "-end", "2008-155",
                "-i", inputDir.getAbsolutePath(), "-j", "post-processing-config.xml"};

        PostProcessingToolMain.main(args);

        final File targetFile = new File(testDirectory, "coo_1_slstr-s3a-nt_avhrr-frac-ma_2008-149_2008-155.nc");
        assertTrue(targetFile.isFile());

        try (NetcdfFile mmd = NetcdfFiles.open(targetFile.getAbsolutePath())) {
            NCTestUtils.assertGlobalAttribute(mmd, "era5-collection", "ERA_5");

            Variable variable = NCTestUtils.getVariable("avhrr-frac-ma_delta_azimuth", mmd, false);
            NCTestUtils.assert3DValueDouble(0, 0, 0, 11.972550392150879, variable);
            NCTestUtils.assert3DValueDouble(1, 0, 0, 11.975187301635742, variable);

            NCTestUtils.assertDimension(FiduceoConstants.MATCHUP_COUNT, 1, mmd);

            // satellite fields
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_x", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_y", 1, mmd);
            NCTestUtils.assertDimension("slstr.s3a.nt_nwp_z", 137, mmd);

            variable = NCTestUtils.getVariable("nwp_lnsp", mmd);
            NCTestUtils.assertAttribute(variable, "units", "~");
            NCTestUtils.assert3DVariable(variable.getFullName(), 0, 0, 0, 11.525882720947266, mmd);

            variable = NCTestUtils.getVariable("nwp_o3", mmd);
            NCTestUtils.assertAttribute(variable, "units", "kg kg**-1");
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 0, 0, 1.9407424645123683E-7, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 10, 0, 3.718567541000084E-6, mmd);
            NCTestUtils.assert4DVariable(variable.getFullName(), 0, 0, 20, 0, 9.968232006940525E-6, mmd);

            variable = NCTestUtils.getVariable("nwp_u10", mmd);
            assertNull(variable.findAttribute("standard_name"));
            NCTestUtils.assert3DValueDouble(0, 0, 0, -0.9742769598960876, variable);

            variable = NCTestUtils.getVariable("nwp_skt", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "Skin temperature");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 301.2406311035156, variable);

            variable = NCTestUtils.getVariable("slstr-s3a-nt.blowVert", mmd);
            NCTestUtils.assertAttribute(variable, "long_name", "10 metre V wind component");
            NCTestUtils.assert3DValueDouble(0, 0, 0, 3.4361371994018555, variable);

            variable = NCTestUtils.getVariable("slstr-s3a-nt_nwp_time", mmd);
            NCTestUtils.assert1DValueLong(0, 1212400800, variable);
        }
    }

    private void writeConfiguration_mmd15() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era-5/v1");
        final String postProcessingConfig = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <era5>\n" +
                "            <nwp-aux-dir>\n" +
                era5Dir.getAbsolutePath() +
                "            </nwp-aux-dir>\n" +
                "            <satellite-fields>" +
                "                <x_dim name='left' length='5' />" +
                "                <y_dim name='right' length='7' />" +
                "                <z_dim name='up' length='23' />" +
                "                <era5_time_variable>era5-time</era5_time_variable>" +
                "                <time_variable>amsre.acquisition_time</time_variable>" +
                "                <longitude_variable>amsre.longitude</longitude_variable>" +
                "                <latitude_variable>amsre.latitude</latitude_variable>" +
                "            </satellite-fields>" +
                "            <matchup-fields>" +
                "                <time_steps_past>41</time_steps_past>" +
                "                <time_steps_future>12</time_steps_future>" +
                "                <time_dim_name>the_time</time_dim_name>" +
                "                <era5_time_variable>era5-mu-time</era5_time_variable>" +
                "                <time_variable>drifter-sst.insitu.time</time_variable>" +
                "                <longitude_variable>drifter-sst.insitu.lon</longitude_variable>" +
                "                <latitude_variable>drifter-sst.insitu.lat</latitude_variable>" +
                "            </matchup-fields>" +
                "        </era5>\n" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, postProcessingConfig);
    }

    private void writeConfiguration_coo1(boolean overwrite) throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era-5/v1");

        final StringBuilder configBuffer = new StringBuilder();
        configBuffer.append("<post-processing-config>\n");
        if (overwrite) {
            configBuffer.append("    <overwrite/>\n");
        } else {
            configBuffer.append("    <create-new-files>\n");
            configBuffer.append("        <output-directory>\n");
            configBuffer.append(testDirectory.getAbsolutePath());
            configBuffer.append("        </output-directory>\n");
            configBuffer.append("    </create-new-files>\n");
        }

        configBuffer.append("    <post-processings>\n");
        configBuffer.append("        <era5>\n");
        configBuffer.append("            <nwp-aux-dir>\n");
        configBuffer.append(era5Dir.getAbsolutePath());
        configBuffer.append("            </nwp-aux-dir>\n");
        configBuffer.append("            <satellite-fields>");
        configBuffer.append("                <x_dim name='slstr.s3a.nt_nwp_x' length='1' />");
        configBuffer.append("                <y_dim name='slstr.s3a.nt_nwp_y' length='1' />");
        configBuffer.append("                <z_dim name='slstr.s3a.nt_nwp_z' length='137'/>");
        configBuffer.append("                <era5_time_variable>slstr.s3ant_nwp_time</era5_time_variable>");
        configBuffer.append("                <time_variable>slstr-s3a-nt_acquisition_time</time_variable>");
        configBuffer.append("                <longitude_variable>slstr-s3a-nt_longitude_tx</longitude_variable>");
        configBuffer.append("                <latitude_variable>slstr-s3a-nt_latitude_tx</latitude_variable>");
        configBuffer.append("                <an_sfc_v10>slstr.s3a.blowVert</an_sfc_v10>");
        configBuffer.append("            </satellite-fields>");
        configBuffer.append("        </era5>\n");
        configBuffer.append("    </post-processings>\n");
        configBuffer.append("</post-processing-config>");

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, configBuffer.toString());
    }

    private void writeConfiguration_coo1_with_sensorRef() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        final File era5Dir = new File(testDataDirectory, "era-5/v1");

        String configBuffer = "<post-processing-config>\n" +
                "    <create-new-files>\n" +
                "        <output-directory>\n" +
                testDirectory.getAbsolutePath() +
                "        </output-directory>\n" +
                "    </create-new-files>\n" +
                "    <post-processings>\n" +
                "        <era5>\n" +
                "            <nwp-aux-dir>\n" +
                era5Dir.getAbsolutePath() +
                "            </nwp-aux-dir>\n" +
                "            <satellite-fields>" +
                "                <sensor-ref>slstr-s3a-nt</sensor-ref>" +
                "                <x_dim name='slstr.s3a.nt_nwp_x' length='1' />" +
                "                <y_dim name='slstr.s3a.nt_nwp_y' length='1' />" +
                "                <z_dim name='slstr.s3a.nt_nwp_z' length='137' />" +
                "                <era5_time_variable>{sensor-ref}_nwp_time</era5_time_variable>" +
                "                <time_variable>{sensor-ref}_acquisition_time</time_variable>" +
                "                <longitude_variable>{sensor-ref}_longitude_tx</longitude_variable>" +
                "                <latitude_variable>{sensor-ref}_latitude_tx</latitude_variable>" +
                "                <an_sfc_v10>{sensor-ref}.blowVert</an_sfc_v10>" +
                "            </satellite-fields>" +
                "        </era5>\n" +
                "    </post-processings>\n" +
                "</post-processing-config>";

        final File postProcessingConfigFile = new File(configDir, "post-processing-config.xml");
        if (!postProcessingConfigFile.createNewFile()) {
            fail("unable to create test file");
        }
        TestUtil.writeStringTo(postProcessingConfigFile, configBuffer);
    }

    private File getInputDirectory_mmd15() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing/mmd15sst");
    }

    private File getInputDirectory_coo1() throws IOException {
        final File testDataDirectory = TestUtil.getTestDataDirectory();
        return new File(testDataDirectory, "post-processing/mmd_coo1");
    }

    // @todo 3 tb/** this is code of general interest, we may move it to a common location tb 2022-06-16
    private static void copyFilesTo(File inputDir, File outputDir) throws IOException {
        final File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IOException("invalid directory path: " + inputDir.getAbsolutePath());
        }
        final byte[] buffer = new byte[32768];

        for (final File inputFile : inputFiles) {
            final String filename = FileUtils.getFilenameFromPath(inputFile.toString());
            final File targetFile = new File(outputDir, filename);

            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                boolean newFile = targetFile.createNewFile();
                if (!newFile) {
                    throw new IOException("Unable to create target file: " + targetFile.getAbsolutePath());
                }

                inputStream = new FileInputStream(inputFile);
                outputStream = new FileOutputStream(targetFile);

                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }
}
