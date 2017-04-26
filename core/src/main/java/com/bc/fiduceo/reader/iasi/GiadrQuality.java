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

class GiadrQuality {

    private static final int PN = 4; //number of sounder pixels

    int[] defPsfSondNbLin;
    int[] defPsfSondNbCol;

    void readRecord(ImageInputStream inputStream) throws IOException {
        final GenericRecordHeader grh = GenericRecordHeader.readGenericRecordHeader(inputStream);

        if (grh.recordClass != RecordClass.GIADR
                || grh.instrumentGroup != InstrumentGroup.IASI
                || grh.recordSubclass != 0) {
            throw new IllegalArgumentException("Bad GRH.");
        }

        defPsfSondNbLin = new int[PN];
        for (int i = 0; i < defPsfSondNbLin.length; i++) {
            defPsfSondNbLin[i] = inputStream.readInt();
        }
        defPsfSondNbCol = new int[PN];
        for (int i = 0; i < defPsfSondNbCol.length; i++) {
            defPsfSondNbCol[i] = inputStream.readInt();
        }
        byte readByte = inputStream.readByte();
        int readInt = inputStream.readInt();

    }
}
