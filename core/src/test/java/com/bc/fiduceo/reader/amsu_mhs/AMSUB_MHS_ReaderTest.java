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

package com.bc.fiduceo.reader.amsu_mhs;


import com.bc.fiduceo.TestUtil;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMSUB_MHS_ReaderTest {

    @Test
    public void testGetDate() {
        Date date = AMSUB_MHS_L1C_Reader.getDate(2002, 125, 0);
        TestUtil.assertCorrectUTCDate(2002, 5, 5, 0, 0, 0, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2002, 126, 0);
        TestUtil.assertCorrectUTCDate(2002, 5, 6, 0, 0, 0, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 1000);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 0, 0, 1, 0, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 22567);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 0, 0, 22, 567, date);

        date = AMSUB_MHS_L1C_Reader.getDate(2008, 217, 82022567);
        TestUtil.assertCorrectUTCDate(2008, 8, 4, 22, 47, 2, 567, date);
    }

    @Test
    public void testGetRegEx() {
        final AMSUB_MHS_L1C_Reader reader = new AMSUB_MHS_L1C_Reader();

        final String regEx = reader.getRegEx();
        assertEquals("'?[A-Z].+[AMBX|MHSX].+[NK|M1].D\\d{5}.S\\d{4}.E\\d{4}.B\\d{7}.+[GC|WI].h5", regEx);

        final Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher("L0496703.NSS.AMBX.NK.D07234.S0630.E0824.B4821011.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("L0502033.NSS.AMBX.NK.D07234.S1004.E1149.B4821213.WI.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NN.D07234.S1151.E1337.B1162021.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("NSS.MHSX.NN.D07234.S1332.E1518.B1162122.GC.h5");
        assertTrue(matcher.matches());

        matcher = pattern.matcher("19890501225800-ESACCI-L1C-AVHRR10_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("20070401033400-ESACCI-L1C-AVHRR17_G-fv01.0.nc");
        assertFalse(matcher.matches());

        matcher = pattern.matcher("W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,MetOpA+IASI_C_EUMP_20130528172543_34281_eps_o_l1.nc");
        assertFalse(matcher.matches());
    }
}
