/*
 * Copyright (C) 2017 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  more details.
 *
 *  A copy of the GNU General Public License should have been supplied along
 *  with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.reader.iasi;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.HashMap;

import static com.bc.fiduceo.reader.iasi.EpsMetopConstants.PN;
import static com.bc.fiduceo.reader.iasi.EpsMetopConstants.SS;

class MDR_1C_v5 extends MDR_1C {

    private static final long RECORD_SIZE = 2728908L;

    private static final long GQIS_FLAG_QUAL_DET_OFFSET = 255620;
    private static final long GQIS_SYS_TEC_IIS_QUAL_OFFSET = 255885;
    private static final long GQIS_QUAL_INDEX_OFFSET = 255860;
    private static final long GQIS_QUAL_INDEX_IIS_OFFSET = 255865;
    private static final long GQIS_QUAL_INDEX_LOC_OFFSET = 255870;
    private static final long GQIS_QUAL_INDEX_RAD_OFFSET = 255875;
    private static final long GQIS_QUAL_INDEX_SPECT_OFFSET = 255880;
    private static final long GQIS_SYS_TEC_SOND_QUAL_OFFSET = 255889;
    private static final long GGEO_SOND_LOC_OFFSET = 255893;
    private static final long GGEO_SOND_ANGLES_METOP_OFFSET = 256853;
    private static final long GGEO_SOND_ANGLES_SUN_OFFSET = 263813;
    private static final long EARTH_SATELLITE_DISTANCE_OFFSET = 276773;
    private static final long IDEF_SPECT_DWN_1B_OFFSET = 276777;
    private static final long IDEF_NS_FIRST_1B_OFFSET = 276782;
    private static final long IDEF_NS_LAST_1B_OFFSET = 276786;
    private static final long G1S_SPECT_OFFSET = 276790;
    private static final long GCS_RAD_ANAL_NB_OFFSET = 2365814;
    private static final long IDEF_CS_MODE_OFFSET = 2727614;
    private static final long GCS_IMG_CLASS_LIN_OFFSET = 2727618;
    private static final long GCS_IMG_CLASS_COL_OFFSET = 2727678;
    private static final long GCS_IMG_CLASS_FIRST_LIN_OFFSET = 2727738;
    private static final long GCS_IMG_CLASS_FIRST_COL_OFFSET = 2727888;
    private static final long GIAC_VAR_IMG_IIS_OFFSET = 2728248;
    private static final long GIAC_AVG_IMG_IIS_OFFSET = 2728398;
    private static final long GEUM_AVHRR_CLOUD_FRAC_OFFSET = 2728548;
    private static final long GEUM_AVHRR_LAND_FRAC_OFFSET = 2728668;
    private static final long GEUM_AVHRR_QUAL_OFFSET = 2728788;

    MDR_1C_v5() {
        super(new byte[(int)RECORD_SIZE]);
    }

    @Override
    long getMdrSize() {
        return RECORD_SIZE;
    }

    short[] get_GS1cSpect(int x, int line) throws IOException {
        final ImageInputStream stream = getStream();
        final long mdrPos = getMdrPos(x);
        final long efovIndex = getEFOVIndex(x, line);

        stream.seek(G1S_SPECT_OFFSET + (mdrPos * PN + efovIndex) * G1S_SPECT_SIZE);

        final short[] spectrum = new short[SS];
        for (int i = 0; i < SS; i++) {
            spectrum[i] = stream.readShort();
        }
        return spectrum;
    }

    static long getGeolocationOffset() {
        return GGEO_SOND_LOC_OFFSET;
    }

    long getFirst1BOffset() {
        return IDEF_NS_FIRST_1B_OFFSET;
    }

    byte readPerPixel_byte(int x, int line, long position) throws IOException {
        final ImageInputStream stream = getStream();
        final long mdrPos = getMdrPos(x);
        final long efovIndex = getEFOVIndex(x, line);

        stream.seek(position + mdrPos * PN + efovIndex);

        return stream.readByte();
    }

    static HashMap<String, ReadProxy> getReadProxies() {
        final HashMap<String, ReadProxy> proxies = new HashMap<>();
        proxies.put("DEGRADED_INST_MDR", new ReadProxy.bytePerScan(DEGRADED_INST_MDR_OFFSET));
        proxies.put("DEGRADED_PROC_MDR", new ReadProxy.bytePerScan(DEGRADED_PROC_MDR_OFFSET));
        proxies.put("GEPSIasiMode", new ReadProxy.intPerScan(GEPS_IASI_MODE_OFFSET));
        proxies.put("GEPSOPSProcessingMode", new ReadProxy.intPerScan(GEPS_OPS_PROC_MODE_OFFSET));
        // skipping GEPSIdConf tb 2017-06-07
        // skipping GEPSLocIasiAvhrr_IASI tb 2017-06-07
        // skipping GEPSLocIasiAvhrr_IIS tb 2017-06-07
        proxies.put("OBT", new ReadProxy.obtPerEVOF(OBT_OFFSET));
        proxies.put("OnboardUTC", new ReadProxy.utcPerEVOF(ONBOARD_UTC_OFFSET));
        proxies.put("GEPSDatIasi", new ReadProxy.utcPerEVOF(GEPS_DAT_IASI_OFFSET));
        // skipping GIsfLinOrigin tb 2017-06-07
        // skipping GIsfColOrigin tb 2017-06-07
        // skipping GIsfPds1 tb 2017-06-07
        // skipping GIsfPds2 tb 2017-06-07
        // skipping GIsfPds3 tb 2017-06-07
        // skipping GIsfPds4 tb 2017-06-07
        proxies.put("GEPS_CCD", new ReadProxy.bytePerEVOF(GEPS_CCD_OFFSET));
        proxies.put("GEPS_SP", new ReadProxy.intPerEVOF(GEPS_SP_OFFSET));
        // skipping GIrcImage tb 2017-06-07
        // @todo 3 tb/tb GQisFlagQual - one variable per channel 2017-05-04
        proxies.put("GQisFlagQualDetailed", new ReadProxy.shortPerPixel(GQIS_FLAG_QUAL_DET_OFFSET));
        proxies.put("GQisQualIndex", new ReadProxy.vInt4PerScan(GQIS_QUAL_INDEX_OFFSET));
        proxies.put("GQisQualIndexIIS", new ReadProxy.vInt4PerScan(GQIS_QUAL_INDEX_IIS_OFFSET));
        proxies.put("GQisQualIndexLoc", new ReadProxy.vInt4PerScan(GQIS_QUAL_INDEX_LOC_OFFSET));
        proxies.put("GQisQualIndexRad", new ReadProxy.vInt4PerScan(GQIS_QUAL_INDEX_RAD_OFFSET));
        proxies.put("GQisQualIndexSpect", new ReadProxy.vInt4PerScan(GQIS_QUAL_INDEX_SPECT_OFFSET));
        proxies.put("GQisSysTecIISQual", new ReadProxy.intPerScan(GQIS_SYS_TEC_IIS_QUAL_OFFSET));
        proxies.put("GQisSysTecSondQual", new ReadProxy.intPerScan(GQIS_SYS_TEC_SOND_QUAL_OFFSET));
        proxies.put("GGeoSondLoc_Lon", new ReadProxy.dualIntPerPixel(GGEO_SOND_LOC_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondLoc_Lat", new ReadProxy.dualIntPerPixel(GGEO_SOND_LOC_OFFSET, 4, 1e-6));
        proxies.put("GGeoSondAnglesMETOP_Zenith", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_METOP_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondAnglesMETOP_Azimuth", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_METOP_OFFSET, 4, 1e-6));
        proxies.put("GGeoSondAnglesSUN_Zenith", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_SUN_OFFSET, 0, 1e-6));
        proxies.put("GGeoSondAnglesSUN_Azimuth", new ReadProxy.dualIntPerPixel(GGEO_SOND_ANGLES_SUN_OFFSET, 4, 1e-6));
        // skipping GGeoIISLoc tb 2017-06-07
        proxies.put("EARTH_SATELLITE_DISTANCE", new ReadProxy.intPerScan(EARTH_SATELLITE_DISTANCE_OFFSET));
        // l1c specific --------------------------------------------
        proxies.put("IDefSpectDWn1b", new ReadProxy.vInt4PerScan(IDEF_SPECT_DWN_1B_OFFSET));
        proxies.put("IDefNsfirst1b", new ReadProxy.intPerScan(IDEF_NS_FIRST_1B_OFFSET));
        proxies.put("IDefNslast1b", new ReadProxy.intPerScan(IDEF_NS_LAST_1B_OFFSET));
        // skipping IDefCovarMatEigenVal1c tb 2017-06-07
        // skipping IDefCcsChannelId tb 2017-06-07
        proxies.put("GCcsRadAnalNbClass", new ReadProxy.intPerPixel(GCS_RAD_ANAL_NB_OFFSET));
        // skipping GCcsRadAnalWgt tb 2017-06-07
        // skipping GCcsRadAnalY tb 2017-06-07
        // skipping GCcsRadAnalZ tb 2017-06-07
        // skipping GCcsRadAnalMean tb 2017-06-07
        // skipping GCcsRadAnalStd tb 2017-06-07
        // skipping GCcsImageClassified tb 2017-06-07
        proxies.put("IDefCcsMode", new ReadProxy.intPerScan(IDEF_CS_MODE_OFFSET));
        proxies.put("GCcsImageClassifiedNbLin", new ReadProxy.shortPerEVOF(GCS_IMG_CLASS_LIN_OFFSET));
        proxies.put("GCcsImageClassifiedNbCol", new ReadProxy.shortPerEVOF(GCS_IMG_CLASS_COL_OFFSET));
        proxies.put("GCcsImageClassifiedFirstLin", new ReadProxy.vInt4PerEVOF(GCS_IMG_CLASS_FIRST_LIN_OFFSET));
        proxies.put("GCcsImageClassifiedFirstCol", new ReadProxy.vInt4PerEVOF(GCS_IMG_CLASS_FIRST_COL_OFFSET));
        // skipping GCcsRadAnalType tb 2017-06-07
        proxies.put("GIacVarImagIIS", new ReadProxy.vInt4PerEVOF(GIAC_VAR_IMG_IIS_OFFSET));
        proxies.put("GIacAvgImagIIS", new ReadProxy.vInt4PerEVOF(GIAC_AVG_IMG_IIS_OFFSET));
        proxies.put("GEUMAvhrr1BCldFrac", new ReadProxy.bytePerPixel(GEUM_AVHRR_CLOUD_FRAC_OFFSET));
        proxies.put("GEUMAvhrr1BLandFrac", new ReadProxy.bytePerPixel(GEUM_AVHRR_LAND_FRAC_OFFSET));
        proxies.put("GEUMAvhrr1BQual", new ReadProxy.bytePerPixel(GEUM_AVHRR_QUAL_OFFSET));
        return proxies;
    }
}
