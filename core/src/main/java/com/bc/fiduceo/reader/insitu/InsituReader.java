package com.bc.fiduceo.reader.insitu;

import com.bc.fiduceo.reader.netcdf.NetCDFReader;
import ucar.ma2.Array;

import java.io.IOException;

public abstract class InsituReader extends NetCDFReader {

    abstract public Array getSourceArray(String variableName) throws IOException;

}
