package com.bc.fiduceo.reader.slstr;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.AcquisitionInfo;
import com.bc.fiduceo.reader.Reader;
import com.bc.fiduceo.reader.ReaderContext;
import com.bc.fiduceo.reader.ReaderUtils;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MicrosSince2000;
import com.bc.fiduceo.util.NetCDFUtils;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.util.ArrayUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ucar.ma2.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipFile;

import static com.bc.fiduceo.reader.slstr.VariableType.Type.NADIR_1km;
import static ucar.ma2.DataType.*;

public class SlstrReaderNew implements Reader {

    private static final String REGEX_ALL = "S3([AB])_SL_1_RBT_.*(.SEN3|zip)";
    private static final String REGEX_NR = "S3([AB])_SL_1_RBT_.*_NR_.*(.SEN3|zip)";
    private static final String REGEX_NT = "S3([AB])_SL_1_RBT_.*_NT_.*(.SEN3|zip)";

    private static final String[] SLSTR_GRID_INDEXES = new String[]{
            "an", "ao", "bn", "bo", "cn", "co", "in", "io", "fn", "fo", "tn", "to", "tx"
    };

    private static final Interval INTERVAL = new Interval(100, 100);
    private static final int NUM_SPLITS = 1;

    private final VariableFactory variableFactory;
    private final String regEx;
    private final ReaderContext readerContext;

    private FileContainer fileContainer;
    private long[] subs_times;
    private TransformFactory transformFactory;
    private ZipFile zipFile;
    private ProductDir productDir;
    private Manifest manifest;

    SlstrReaderNew(ReaderContext readerContext, ProductType productType) {
        this.readerContext = readerContext;
        productDir = null;

        if (productType == ProductType.ALL) {
            this.regEx = REGEX_ALL;
        } else if (productType == ProductType.NR) {
            this.regEx = REGEX_NR;
        } else if (productType == ProductType.NT) {
            this.regEx = REGEX_NT;
        } else {
            throw new IllegalArgumentException("Unsupported product type");
        }

        variableFactory = new VariableFactory();
        zipFile = null;
    }

    // package access for testing only tb 2019-05-13
    static long[] subSampleTimes(long[] timeStamps) {
        final long[] subs_times = new long[(int) Math.ceil(timeStamps.length / 2.0)];

        int writeIndex = 0;
        for (int i = 0; i < timeStamps.length; i++) {
            if (i % 2 == 0) {
                subs_times[writeIndex] = timeStamps[i];
                ++writeIndex;
            }
        }
        return subs_times;
    }

    // @todo 1 tb/tb write test 2020-10-23
    protected static double getGeophysicalNoDataValue(Variable variable) {
        final DataType dataType = variable.getDataType();
        final Number defaultFillValue = NetCDFUtils.getDefaultFillValue(dataType, dataType.isUnsigned());
        return defaultFillValue.doubleValue();
    }

    // @todo 1 tb/tb write test 2020-10-23
    protected static double getGeophysicalNoDataValue(TiePointGrid tiePointGrid) {
        final DataType dataType = NetCDFUtils.getNetcdfDataType(tiePointGrid.getDataType());
        final Number defaultFillValue = NetCDFUtils.getDefaultFillValue(dataType, dataType.isUnsigned());
        return defaultFillValue.doubleValue();
    }

    // @todo 1 tb/tb write test 2020-10-23
    protected static int[] getShape(Interval interval) {
        final int[] shape = new int[2];
        shape[0] = interval.getY();
        shape[1] = interval.getX();

        return shape;
    }

    // @todo 1 tb/tb write test 2020-10-23
    // package access for testing only tb 2019-05-17
    protected static Array createReadingArray(DataType targetDataType, int[] shape) {
        switch (targetDataType) {
            case FLOAT:
                return Array.factory(DataType.FLOAT, shape);
            case INT:
            case SHORT:
            case BYTE:
                return Array.factory(DataType.INT, shape);
            default:
                throw new RuntimeException("unsupported data type: " + targetDataType);
        }
    }

    @Override
    public void open(File file) throws IOException {
        productDir = ProductDir.create(file, readerContext);
        readManifest();

        final int obliqueGridOffset = getObliqueGridOffset();
        final Dimension productSize = readProductSize();
        transformFactory = new TransformFactory(productSize.getNx(),
                productSize.getNy(),
                obliqueGridOffset);

        fileContainer = new FileContainer(productDir);
    }

    private void readManifest() throws IOException {
        final File manifestFile = productDir.getFile("xfdumanifest.xml");

        try (InputStream inputStream = new FileInputStream(manifestFile)) {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            final Document document = builderFactory.newDocumentBuilder().parse(inputStream);
            manifest = new Manifest(document);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        manifest = null;

        if (fileContainer != null) {
            fileContainer.close();
            fileContainer = null;
        }

        if (productDir != null) {
            productDir.close();
            productDir = null;
        }

        if (zipFile != null) {
            zipFile.close();
            zipFile = null;
        }

        transformFactory = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
//        final AcquisitionInfo acquisitionInfo = read(INTERVAL, NUM_SPLITS);
//
//        setOrbitNodeInfo(acquisitionInfo);
//
//        return acquisitionInfo;

        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getRegEx() {
        return regEx;
    }

    @Override
    public PixelLocator getPixelLocator() {
//        return new SlstrPixelLocator(product.getSceneGeoCoding(), transformFactory.get(NADIR_500m));
        throw new IllegalStateException("not implemented");
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() {
        ensureTimingVector();

        return new TimeLocator_MicrosSince2000(subs_times);
    }

    @Override
    public List<Variable> getVariables() {
//        final List<Variable> result = new ArrayList<>();
//
//        final Band[] bands = product.getBands();
//        for (final Band band : bands) {
//            if (variableNames.isValidName(band.getName())) {
//                final VariableProxy variableProxy = new VariableProxy(band);
//                result.add(variableProxy);
//            }
//        }
//
//        final TiePointGrid[] tiePointGrids = product.getTiePointGrids();
//        for (final TiePointGrid tiePointGrid : tiePointGrids) {
//            if (variableNames.isValidName(tiePointGrid.getName())) {
//                final VariableProxy variableProxy = new VariableProxy(tiePointGrid);
//                result.add(variableProxy);
//            }
//        }
//
//        return result;
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Dimension getProductSize() {
        final Transform transform = transformFactory.get(new VariableType(NADIR_1km));
        return transform.getRasterSize();
    }

    @Override
    public int[] extractYearMonthDayFromFilename(String fileName) {
        final String datePart = fileName.substring(16, 24);
        final int[] ymd = new int[3];
        ymd[0] = Integer.parseInt(datePart.substring(0, 4));
        ymd[1] = Integer.parseInt(datePart.substring(4, 6));
        ymd[2] = Integer.parseInt(datePart.substring(6, 8));

        return ymd;
    }

    @Override
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException {
//        if (product.containsTiePointGrid(variableName)) {
//            // we do not want raw data access on tie-point grids tb 2016-08-11
//            return readScaled(centerX, centerY, interval, variableName);
//        }
//
//        final VariableType variableType = variableNames.getVariableType(variableName);
//        final Transform transform = transformFactory.get(variableType);
//        final Dimension rasterSize = transform.getRasterSize();
//
//        final RasterDataNode dataNode = getRasterDataNode(variableName);
//
//        final double noDataValue = getNoDataValue(dataNode);
//        final DataType targetDataType = NetCDFUtils.getNetcdfDataType(dataNode.getDataType());
//
//        final Interval mappedInterval = transform.mapInterval(interval);
//        final int width = mappedInterval.getX();
//        final int height = mappedInterval.getY();
//        final int[] shape = getShape(mappedInterval);
//        final Array readArray = Array.factory(targetDataType, shape);
//        final Array targetArray = Array.factory(targetDataType, shape);
//
//        final int mappedX = (int) (transform.mapCoordinate_X(centerX) + 0.5);
//        final int mappedY = (int) (transform.mapCoordinate_Y(centerY) + 0.5);
//
//        final int xOffset = mappedX - width / 2 + transform.getOffset();
//        final int yOffset = mappedY - height / 2 + transform.getOffset();
//
//        readRawProductData(dataNode, readArray, width, height, xOffset, yOffset);
//
//        final Index index = targetArray.getIndex();
//        int readIndex = 0;
//        for (int y = 0; y < width; y++) {
//            final int currentY = yOffset + y;
//            for (int x = 0; x < height; x++) {
//                final int currentX = xOffset + x;
//                index.set(y, x);
//                if (currentX >= 0 && currentX < rasterSize.getNx() && currentY >= 0 && currentY < rasterSize.getNy()) {
//                    targetArray.setObject(index, readArray.getObject(readIndex));
//                    ++readIndex;
//                } else {
//                    targetArray.setObject(index, noDataValue);
//                }
//            }
//        }
//
//        if (variableNames.isFlagVariable(variableName)) {
//            return transform.processFlags(targetArray, (int) noDataValue);
//        } else {
//            return transform.process(targetArray, noDataValue);
//        }
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException {
        final VariableType variableType = variableFactory.getVariableType(variableName);
        final Transform transform = transformFactory.get(variableType);

        if (variableType.isTiePoint()) {
            return readFromTiePoint(centerX, centerY, interval, variableName, transform);
        } else {
            return readScaledFromVariable(centerX, centerY, interval, variableName, transform);
        }
    }

    private Array readFromTiePoint(int centerX, int centerY, Interval interval, String variableName, Transform transform) throws IOException {
        final TiePointGrid tiePointGrid = getTiePointGrid(variableName);
        // @todo 1 tb/tb all this is repeated! Move to common method an add tests 2020-10-26
        final double noDataValue = getGeophysicalNoDataValue(tiePointGrid);

        final Interval mappedInterval = transform.mapInterval(interval);
        final int[] shape = getShape(mappedInterval);
        final DataType dataType = NetCDFUtils.getNetcdfDataType(tiePointGrid.getDataType());
        final Array targetArray = Array.factory(dataType, shape);

        final int width = mappedInterval.getX();
        final int height = mappedInterval.getY();

        final int mappedX = (int) (transform.mapCoordinate_X(centerX) + 0.5);
        final int mappedY = (int) (transform.mapCoordinate_Y(centerY) + 0.5);
        final int xOffset = mappedX - width / 2 + transform.getOffset();
        final int yOffset = mappedY - height / 2 + transform.getOffset();

        final Rectangle subsetRectangle = new Rectangle(xOffset, yOffset, width, height);
        final Dimension rasterSize = transform.getRasterSize();
        final Rectangle productRectangle = new Rectangle(0, 0, rasterSize.getNx(), rasterSize.getNy());
        final Rectangle intersection = productRectangle.intersection(subsetRectangle);
        if (intersection.isEmpty()) {
            MAMath.setDouble(targetArray, noDataValue);
            return targetArray;
        }

        final Array readingArray = Array.factory(dataType, new int[]{intersection.height, intersection.width});

        if (dataType == FLOAT) {
            tiePointGrid.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (float[]) readingArray.getStorage());
        } else if (dataType == INT || dataType == SHORT || dataType == BYTE) {
            tiePointGrid.readPixels(intersection.x, intersection.y, intersection.width, intersection.height, (int[]) readingArray.getStorage());
        }

        final Index index = targetArray.getIndex();
        int readIndex = 0;
        for (int y = 0; y < width; y++) {
            final int currentY = yOffset + y;
            for (int x = 0; x < height; x++) {
                final int currentX = xOffset + x;
                index.set(y, x);
                if (currentX >= 0 && currentX < rasterSize.getNx() && currentY >= 0 && currentY < rasterSize.getNy()) {
                    targetArray.setObject(index, readingArray.getObject(readIndex));
                    ++readIndex;
                } else {
                    targetArray.setObject(index, noDataValue);
                }
            }
        }

        return transform.process(targetArray, noDataValue);
    }

    private Array readScaledFromVariable(int centerX, int centerY, Interval interval, String variableName, Transform transform) throws IOException {
        final Variable variable = getVariable(variableName);
        final double noDataValue = getGeophysicalNoDataValue(variable);

        final Interval mappedInterval = transform.mapInterval(interval);
        final int[] shape = getShape(mappedInterval);

        final int width = mappedInterval.getX();
        final int height = mappedInterval.getY();

        final int mappedX = (int) (transform.mapCoordinate_X(centerX) + 0.5);
        final int mappedY = (int) (transform.mapCoordinate_Y(centerY) + 0.5);
        final int xOffset = mappedX - width / 2 + transform.getOffset();
        final int yOffset = mappedY - height / 2 + transform.getOffset();

        try {
            Array sourceArray = variable.read(new int[]{yOffset, xOffset}, shape);

            final double scaleFactor = NetCDFUtils.getScaleFactor(variable);
            final double offset = NetCDFUtils.getOffset(variable);
            if (ReaderUtils.mustScale(scaleFactor, offset)) {
                final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
                sourceArray = MAMath.convert2Unpacked(sourceArray, scaleOffset);
            }
            return transform.process(sourceArray, noDataValue);
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Variable getVariable(String variableName) throws IOException {
        final String manifestId = variableFactory.getManifestId(variableName);
        final String fileName = manifest.getFileName(manifestId);

        final NetcdfFile ncFile = fileContainer.get(fileName);

        final Variable variable = ncFile.findVariable(variableName);
        if (variable == null) {
            throw new IOException("Variable not found: " + variableName);
        }
        return variable;
    }

    private TiePointGrid getTiePointGrid(String variableName) throws IOException {
        // @todo 1 tb/tb add caching mechanism for TiePointGrids 2020-10-28
        final Variable variable = getVariable(variableName);

        final Array variableArray = variable.read();
        if (variableArray.getDataType() != DataType.DOUBLE) {
            throw new IllegalStateException("not implemented");
        }

        final String gridIndex = getGridIndex(variableName);
        final short[] sourceResolutions = getResolutions(gridIndex);

//        final int subSamplingX = sourceResolutions[0] / referenceResolutions[0];
//        final int subSamplingY = sourceResolutions[1] / referenceResolutions[1];

        final double[] storage = (double[]) variableArray.getStorage();
        final float[] floats = new float[storage.length];
        for (int i = 0; i < storage.length; i++){
            floats[i] = (float) storage[i];
        }
        final int[] shape = variable.getShape();
        return new TiePointGrid(variableName, shape[0], shape[1], 0.5f, 0.5f, 2.f, 2.f, floats);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) {
//        final int width = interval.getX();
//        final int height = interval.getY();
//        final int[] timeArray = new int[width * height];
//
//        ensureTimingVector();
//
//        final Transform transform = transformFactory.get(NADIR_1km);
//        final Dimension rasterSize = transform.getRasterSize();
//        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();
//        final int halfHeight = height / 2;
//        final int halfWidth = width / 2;
//        int writeOffset = 0;
//
//        for (int yRead = y - halfHeight; yRead <= y + halfHeight; yRead++) {
//            int lineTimeSeconds = fillValue;
//            if (yRead >= 0 && yRead < rasterSize.getNy()) {
//                final long lineTimeMillis = TimeUtils.millisSince2000ToUnixEpoch(subs_times[yRead]);
//                lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
//            }
//
//            for (int xRead = x - halfWidth; xRead <= x + halfWidth; xRead++) {
//                if (xRead >= 0 && xRead < rasterSize.getNx()) {
//                    timeArray[writeOffset] = lineTimeSeconds;
//                } else {
//                    timeArray[writeOffset] = fillValue;
//                }
//                ++writeOffset;
//            }
//        }
//
//        final int[] shape = getShape(interval);
//        return (ArrayInt.D2) Array.factory(INT, shape, timeArray);
        throw new IllegalStateException("not implemented");
    }

    @Override
    public String getLongitudeVariableName() {
        return "longitude_tx";
    }

    @Override
    public String getLatitudeVariableName() {
        return "latitude_tx";
    }

    protected void readProductData(RasterDataNode dataNode, Array targetArray, int width, int height, int xOffset, int yOffset) throws IOException {
//        final VariableType variableType = variableNames.getVariableType(dataNode.getName());
//        final Transform transform = transformFactory.get(variableType);
//        final Dimension rasterSize = transform.getRasterSize();
//
//        readSubsetData(dataNode, targetArray, width, height, xOffset, yOffset, rasterSize.getNx(), rasterSize.getNy());
        throw new IllegalStateException("not implemented");
    }

    protected RasterDataNode getRasterDataNode(String variableName) {
//        if (!variableNames.isValidName(variableName)) {
//            throw new RuntimeException("Requested variable not contained in product: " + variableName);
//        }
//
//        return super.getRasterDataNode(variableName);

        throw new IllegalStateException("not implemented");
    }

    private void setOrbitNodeInfo(AcquisitionInfo acquisitionInfo) {
//        acquisitionInfo.setNodeType(NodeType.UNDEFINED);
//        final MetadataElement metadataRoot = product.getMetadataRoot();
//        final MetadataElement manifest = metadataRoot.getElement("Manifest");
//        if (manifest != null) {
//            final MetadataElement metadataSection = manifest.getElement("metadataSection");
//            if (metadataSection != null) {
//                final MetadataElement orbitReference = metadataSection.getElement("orbitReference");
//                if (orbitReference != null) {
//                    final MetadataElement orbitNumber = orbitReference.getElement("orbitNumber");
//                    if (orbitNumber != null) {
//                        final String groundTrackDirection = orbitNumber.getAttribute("groundTrackDirection").getData().getElemString();
//                        if (groundTrackDirection.equalsIgnoreCase("descending")) {
//                            acquisitionInfo.setNodeType(NodeType.DESCENDING);
//                        } else if (groundTrackDirection.equalsIgnoreCase("ascending")) {
//                            acquisitionInfo.setNodeType(NodeType.ASCENDING);
//                        }
//                    }
//                }
//            }
//        }

        throw new IllegalStateException("not implemented");
    }

    private int getObliqueGridOffset() {
        final MetadataElement productInformationElement =  manifest.getSlstrProductInformation();

        int nadirTrackOffset = -1;
        int obliqueTrackOffset = -1;
        final MetadataElement[] elements = productInformationElement.getElements();
        for (final MetadataElement element : elements) {
            if (element.getName().equalsIgnoreCase("nadirImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    nadirTrackOffset = extractInteger("trackOffset", element);
                }
            }
            if (element.getName().equalsIgnoreCase("obliqueImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("1 km")) {
                    obliqueTrackOffset = extractInteger("trackOffset", element);
                }
            }
        }

        if (nadirTrackOffset < 0 | obliqueTrackOffset < 0) {
            throw new RuntimeException("Unable to extract raster offsets from metadata.");
        }

        return nadirTrackOffset - obliqueTrackOffset;
    }

    private Dimension readProductSize() {
        final Dimension productSize = new Dimension();

        final MetadataElement productInformationElement =  manifest.getSlstrProductInformation();
        final MetadataElement[] elements = productInformationElement.getElements();
        for (final MetadataElement element : elements) {
            if (element.getName().equalsIgnoreCase("nadirImageSize")) {
                final MetadataAttribute grid = element.getAttribute("grid");
                if (grid.getData().getElemString().equalsIgnoreCase("0.5 km stripe A")) {
                    productSize.setNx(extractInteger("columns", element));
                    productSize.setNy(extractInteger("rows", element));
                }
            }
        }

        return productSize;
    }

    private int extractInteger(String attributeName, MetadataElement element) {
        final MetadataAttribute trackOffset = element.getAttribute(attributeName);
        final String trackOffsetString = trackOffset.getData().getElemString();
        return Integer.parseInt(trackOffsetString);
    }

    private void ensureTimingVector() {
//        if (subs_times == null) {
//            final MetadataElement metadataRoot = product.getMetadataRoot();
//            final MetadataElement time_stamp_a = metadataRoot.getElement("time_stamp_a");
//            final MetadataAttribute values = time_stamp_a.getAttribute("value");
//            final ProductData valuesData = values.getData();
//            final long[] timeStamps = (long[]) valuesData.getElems();
//            subs_times = subSampleTimes(timeStamps);
//        }

        throw new IllegalStateException("not implemented");
    }

    static String getGridIndex(String bandName) {
        String[] nameParts = bandName.split("_");
        int lastPartIndex = nameParts.length - 1;
        int index = lastPartIndex;
        int firstIndexOfPartWithTwoLetters = -1;
        while (index >= 0) {
            if (ArrayUtils.isMemberOf(nameParts[index], SLSTR_GRID_INDEXES)) {
                return nameParts[index];
            } else if (firstIndexOfPartWithTwoLetters < 0 && nameParts[index].length() == 2) {
                firstIndexOfPartWithTwoLetters = index;
            }
            index--;
        }
        if (firstIndexOfPartWithTwoLetters >= 0) {
            return nameParts[firstIndexOfPartWithTwoLetters];
        }
        if (nameParts[lastPartIndex].length() > 1) {
            return nameParts[lastPartIndex].substring(nameParts[lastPartIndex].length() - 2);
        }
        return nameParts[lastPartIndex];
    }

    protected short[] getResolutions(String gridIndex) {
        short[] resolutions;
        if (gridIndex.startsWith("i") || gridIndex.startsWith("f")) {
            resolutions = new short[]{1000, 1000};
        } else if (gridIndex.startsWith("t")) {
            resolutions = new short[]{16000, 1000};
        } else {
            resolutions = new short[]{500, 500};
        }
        return resolutions;
    }
}
