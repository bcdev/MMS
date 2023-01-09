package com.bc.fiduceo.reader.netcdf;

import com.bc.fiduceo.core.Interval;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

import java.io.IOException;

public class NcTileCache {

    public NcTileCache(NetcdfFile netcdfFile) {
    }

    public Array read(int[] offset, int[] shape, String variableName) throws IOException {
        // check cache if variable-struct in cache
        // yes
        // no
        // - calculate tile-size and location
        // - read from disk
        // - add to cache-struct
        // - return
        throw new IOException("not implemented");
    }
}