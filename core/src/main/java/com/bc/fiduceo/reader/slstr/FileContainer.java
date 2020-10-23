package com.bc.fiduceo.reader.slstr;

import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class FileContainer {

    private final ProductDir productDir;
    private final HashMap<String, NetcdfFile> fileCache;

    FileContainer(ProductDir productDir) {
        this.productDir = productDir;
        this.fileCache = new HashMap<>();
    }


    public NetcdfFile get(String fileName) throws IOException {
        NetcdfFile netcdfFile = fileCache.get(fileName);
        if (netcdfFile == null) {
            final File file = productDir.getFile(fileName);
            netcdfFile = NetcdfFiles.open(file.getAbsolutePath());
            fileCache.put(fileName, netcdfFile);
        }

        return netcdfFile;
    }

    void close() throws IOException {
        final Set<Map.Entry<String, NetcdfFile>> entries = fileCache.entrySet();
        for (Map.Entry<String, NetcdfFile> next : entries) {
            final NetcdfFile netcdfFile = next.getValue();
            netcdfFile.close();
        }
    }
}
