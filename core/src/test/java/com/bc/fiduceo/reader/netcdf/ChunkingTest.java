package com.bc.fiduceo.reader.netcdf;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

public class ChunkingTest {

    @Test
    public void testToChunkIndex_1d() {
        final Array chunkSizes = Array.factory(DataType.INT, new int[]{1});
        chunkSizes.setInt(0, 128);

        final Chunking chunking = new Chunking(chunkSizes);
    }

}
