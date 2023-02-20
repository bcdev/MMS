package com.bc.fiduceo.reader.netcdf;

import org.esa.snap.core.util.StringUtils;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;

public class NcChunkCache {

    private final HashMap<String, Chunks> cache;
    private final NetcdfFile netcdfFile;

    /**
     * Creates the chunk cache.
     *
     * @param netcdfFile opened NetCDF file in read mode.
     */
    public NcChunkCache(NetcdfFile netcdfFile) {
        this.netcdfFile = netcdfFile;
        cache = new HashMap<>();
    }

    public Array read(int[] offset, int[] shape, String groupName, String variableName) throws IOException {
        // check cache if variable-struct in cache
        final Chunks chunks = cache.get(variableName);
        if (chunks != null) {
            // yes
        } else {
            // no
            Group group = null;
            if (StringUtils.isNotNullAndNotEmpty(groupName)) {
                group = netcdfFile.findGroup(groupName);
            }
            final Variable variable = netcdfFile.findVariable(group, variableName);
            if (variable == null) {
                throw new IOException("Unknown variable: " + variableName);
            }
            final Array read = variable.read();
            final Attribute chunkSize = variable.findAttribute("_ChunkSizes");
            if (chunkSize == null) {
                // data is not chunked, read completely.
            } else {
                final Array values = chunkSize.getValues();
            }
            // - calculate tile-size and location
            // - read from disk
            // - add to cache-struct
            // - return
        }

        return null;
    }

    public Array read(int[] offset, int[] shape, String variableName) throws IOException {
        return read(offset, shape, null, variableName);
    }
}