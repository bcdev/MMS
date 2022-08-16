package com.bc.fiduceo.reader.slstr_subset;

import com.bc.fiduceo.core.Dimension;
import com.bc.fiduceo.core.Interval;
import com.bc.fiduceo.core.NodeType;
import com.bc.fiduceo.geometry.Geometry;
import com.bc.fiduceo.geometry.GeometryFactory;
import com.bc.fiduceo.geometry.LineString;
import com.bc.fiduceo.geometry.Polygon;
import com.bc.fiduceo.location.PixelLocator;
import com.bc.fiduceo.reader.*;
import com.bc.fiduceo.reader.slstr.utility.TransformFactory;
import com.bc.fiduceo.reader.snap.SNAP_PixelLocator;
import com.bc.fiduceo.reader.time.TimeLocator;
import com.bc.fiduceo.reader.time.TimeLocator_MicrosSince2000;
import com.bc.fiduceo.store.FileSystemStore;
import com.bc.fiduceo.store.Store;
import com.bc.fiduceo.store.ZipStore;
import com.bc.fiduceo.util.NetCDFUtils;
import com.bc.fiduceo.util.TimeUtils;
import org.esa.s3tbx.dataio.s3.Manifest;
import org.esa.s3tbx.dataio.s3.XfduManifest;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ucar.ma2.DataType;
import ucar.ma2.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static com.bc.fiduceo.reader.slstr.utility.ManifestUtil.getObliqueGridOffset;
import static com.bc.fiduceo.util.NetCDFUtils.scaleIfNecessary;
import static ucar.ma2.DataType.INT;
import static ucar.nc2.NetcdfFiles.openInMemory;

public class SlstrRegriddedSubsetReader implements Reader {

    private final ReaderContext _readerContext;
    private final boolean _nadirView;
    private final String LONGITUDE_VAR_NAME = "longitude_in";
    private final String LATITUDE_VAR_NAME = "latitude_in";
    private TreeMap<String, NetcdfFile> _ncFiles;
    private ArrayList<Variable> _variables;
    private HashMap<String, Variable> _variablesLUT;
    private String _manifest;
    private PixelLocator pixelLocator;
    private TimeLocator_MicrosSince2000 _timeLocator;
    private long[] _timeStamps2000;
    private TransformFactory transformFactory;

    public SlstrRegriddedSubsetReader(ReaderContext readerContext, boolean nadirView) {
        _readerContext = readerContext;
        _nadirView = nadirView;
    }

    @Override
    public void open(File file) throws IOException {
        final Store store;
        if (isZipFile(file)) {
            store = new ZipStore(file.toPath());
        } else {
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            store = new FileSystemStore(file.toPath());
        }
        try {
            final TreeSet<String> keyManifest = store.getKeysEndingWith("xfdumanifest.xml");
            _manifest = new String(store.getBytes(keyManifest.first()));
            openNcFiles(store);
            initVariables();

            final InputSource is = new InputSource(new StringReader(_manifest));
            final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            final Manifest manifest = XfduManifest.createManifest(document);

            final MetadataElement metadataRoot = new MetadataElement("root");
            metadataRoot.addElement(manifest.getMetadata());
            final int obliqueGridOffset = getObliqueGridOffset(metadataRoot);
            final Dimension productSize = getProductSize();

            transformFactory = new TransformFactory(productSize.getNx() * 2,
                    productSize.getNy() * 2,
                    obliqueGridOffset);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage());
        } finally {
            store.close();
        }
    }

    @Override
    public void close() throws IOException {
        _ncFiles.clear();
        transformFactory = null;
    }

    @Override
    public AcquisitionInfo read() throws IOException {
        final AcquisitionInfo info;
        try {
            getVariables();
            info = new AcquisitionInfo();
            info.setNodeType(findNodeType());
            info.setSensingStart(findSensingTime("start_time"));
            info.setSensingStop(findSensingTime("stop_time"));
            extractGeometries(info);
        } catch (ParseException e) {
            throw new IOException(e.getMessage());
        }
        return info;
    }

    private void extractGeometries(AcquisitionInfo acquisitionInfo) throws IOException {
        final GeometryFactory geometryFactory = _readerContext.getGeometryFactory();
        final BoundingPolygonCreator boundingPolygonCreator = new BoundingPolygonCreator(new Interval(250, 250), geometryFactory);

        final Variable lonVariable = _variablesLUT.get(LONGITUDE_VAR_NAME);
        final Array longitude = NetCDFUtils.readAndScaleIfNecessary(lonVariable);
        final Variable latVariable = _variablesLUT.get(LATITUDE_VAR_NAME);
        final Array latitude = NetCDFUtils.readAndScaleIfNecessary(latVariable);

        final Geometry boundingGeometry = boundingPolygonCreator.createBoundingGeometry(longitude, latitude);
        if (!boundingGeometry.isValid()) {
            throw new RuntimeException("Detected invalid bounding geometry");
        }
        acquisitionInfo.setBoundingGeometry(boundingGeometry);

        final Geometries geometries = new Geometries();
        geometries.setBoundingGeometry(boundingGeometry);
        final LineString timeAxisGeometry = boundingPolygonCreator.createTimeAxisGeometry(longitude, latitude);
        geometries.setTimeAxesGeometry(timeAxisGeometry);
        ReaderUtils.setTimeAxes(acquisitionInfo, geometries.getTimeAxesGeometry(), geometryFactory);
    }

    private Date findSensingTime(String attributeName) throws ParseException {
        final NetcdfFile netcdfFile = _ncFiles.firstEntry().getValue();
        final String startTimeStr = NetCDFUtils.getGlobalAttributeString(attributeName, netcdfFile);
        final String substring = startTimeStr.substring(0, startTimeStr.lastIndexOf("."));
        final ProductData.UTC utc = ProductData.UTC.parse(substring, "yyyy-MM-dd'T'HH:mm:ss");
        return utc.getAsDate();
    }

    private NodeType findNodeType() {
        final NodeType nodeType;
        if (_manifest.contains("groundTrackDirection")) {
            final Pattern pattern = Pattern.compile("groundTrackDirection *= *['\"]ascending['\"]");
            final Matcher matcher = pattern.matcher(_manifest);
            if (matcher.find()) {
                nodeType = NodeType.ASCENDING;
            } else {
                nodeType = NodeType.DESCENDING;
            }
        } else {
            nodeType = NodeType.UNDEFINED;
        }
        return nodeType;
    }

    @Override
    public String getRegEx() {
        return "S3[AB]_SL_1_RBT____(\\d{8}T\\d{6}_){3}\\d{4}(_\\d{3}){2}_\\d{4}_LN2_O_NT_\\d{3}(.SEN3|.zip)";
    }

    @Override
    public PixelLocator getPixelLocator() throws IOException {
        if (pixelLocator == null) {
            final Variable lonVariable = _variablesLUT.get(LONGITUDE_VAR_NAME);
            final Array longitude = NetCDFUtils.readAndScaleIfNecessary(lonVariable);
            final Variable latVariable = _variablesLUT.get(LATITUDE_VAR_NAME);
            final Array latitude = NetCDFUtils.readAndScaleIfNecessary(latVariable);
            final int[] shape = latitude.getShape();

            final GeoRaster geoRaster = new GeoRaster((double[]) longitude.get1DJavaArray(DataType.DOUBLE),
                    (double[]) latitude.get1DJavaArray(DataType.DOUBLE),
                    LONGITUDE_VAR_NAME, LATITUDE_VAR_NAME,
                    shape[1], shape[0], shape[1], shape[0],
                    1.0, 0.5, 0.5, 1.0, 1.0);

            final ForwardCoding forward = ComponentFactory.getForward(PixelForward.KEY);
            final InverseCoding inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);
            final ComponentGeoCoding componentGeoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.ANTIMERIDIAN);
            componentGeoCoding.initialize();

            pixelLocator = new SNAP_PixelLocator(componentGeoCoding);
        }
        return pixelLocator;
    }

    @Override
    public PixelLocator getSubScenePixelLocator(Polygon sceneGeometry) throws IOException {
        return getPixelLocator();
    }

    @Override
    public TimeLocator getTimeLocator() throws IOException {
        if (_timeLocator == null) {
            ensureTimeStamps();
            _timeLocator = new TimeLocator_MicrosSince2000(_timeStamps2000);
        }
        return _timeLocator;
    }

    private void ensureTimeStamps() throws IOException {
        if (_timeStamps2000 == null) {
            final NetcdfFile ncFile = _ncFiles.get("time_in.nc");
            final Variable timeStampVariable = ncFile.findVariable("time_stamp_i");
            if (timeStampVariable == null) {
                throw new IOException("variable time_stamp_i not found.");
            }

            final Array array = timeStampVariable.read();
            _timeStamps2000 = (long[]) array.get1DJavaArray(DataType.LONG);

            if (_timeStamps2000 == null) {
                throw new IOException("Unable to read time stamp data");
            }
        }
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
    public Array readRaw(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Variable variable = _variablesLUT.get(variableName);
        final Number fillValue = NetCDFUtils.getFillValue(variable);
        // @todo 1 tb/** this needs to be cached! Else we read the full array for every matchup. tb 2022-07-21
        final Array fullArray = variable.read();
        return RawDataReader.read(centerX, centerY, interval, fillValue, fullArray, getProductSize());
    }

    @Override
    public Array readScaled(int centerX, int centerY, Interval interval, String variableName) throws IOException, InvalidRangeException {
        final Array rawData = readRaw(centerX, centerY, interval, variableName);
        final Variable variable = _variablesLUT.get(variableName);

        return scaleIfNecessary(variable, rawData);
    }

    @Override
    public ArrayInt.D2 readAcquisitionTime(int x, int y, Interval interval) throws IOException, InvalidRangeException {
        ensureTimeStamps();
        final int targetWidth = interval.getX();
        final int targetHeight = interval.getY();
        final Dimension productSize = getProductSize();
        final int prodWidth = productSize.getNx();

        final int fillValue = NetCDFUtils.getDefaultFillValue(int.class).intValue();

        final int startX = x - targetWidth / 2;
        final int startY = y - targetHeight / 2;

        final int[] targetShape = {targetHeight, targetWidth};
        final int[] store = new int[targetHeight * targetWidth];
        Arrays.fill(store, fillValue);
        final ArrayInt.D2 target = (ArrayInt.D2) Array.factory(INT, targetShape, store);
        final Index index = target.getIndex();
        for (int yIdx = 0; yIdx < targetHeight; yIdx++) {
            final int srcY = startY + yIdx;
            if (srcY < 0 || srcY >= _timeStamps2000.length) {
                continue;
            }
            final long lineTimeMillis = TimeUtils.millisSince2000ToUnixEpoch(_timeStamps2000[srcY]);
            int lineTimeSeconds = (int) Math.round(lineTimeMillis * 0.001);
            index.set0(yIdx);
            for (int xIdx = 0; xIdx < targetWidth; xIdx++) {
                final int srcX = startX + xIdx;
                if (srcX < 0 || srcX >= prodWidth) {
                    continue;
                }
                index.set1(xIdx);
                target.setInt(index, lineTimeSeconds);
            }
        }

        return target;
    }

    private void initVariables() throws IOException {
        if (_variables != null) {
            return;
        }
        final Comparator<int[]> comp = getIntArrayComparator();
        final TreeMap<int[], ArrayList<Variable>> shapeGroupedVariablesMap = new TreeMap<>(comp.reversed());
        for (NetcdfFile netcdfFile : _ncFiles.values()) {
            final ucar.nc2.Dimension rowsDim = netcdfFile.findDimension("rows");
            final ucar.nc2.Dimension colsDim = netcdfFile.findDimension("columns");
            if (rowsDim == null || colsDim == null) {
                continue;
            }
            final int numRows = rowsDim.getLength();
            final int numCols = colsDim.getLength();
            final int[] fileShape = new int[]{numRows, numCols};
            if (!shapeGroupedVariablesMap.containsKey(fileShape)) {
                shapeGroupedVariablesMap.put(fileShape, new ArrayList<>());
            }
            final List<Variable> vars = netcdfFile.getVariables();
            for (Variable var : vars) {
                final int[] shape = var.getShape();
                if (shape.length != 2 || shape[0] != numRows || shape[1] < 130) {
                    continue;
                }
                if (shape[1] < numCols && var.getShortName().contains("orphan")) {
                    continue;
                }
                shapeGroupedVariablesMap.get(fileShape).add(var);
            }
        }
        final Map.Entry<int[], ArrayList<Variable>> fullSizeVarsEntry = shapeGroupedVariablesMap.pollFirstEntry();
        final int[] fullSizeShape = fullSizeVarsEntry.getKey();
        _variables = fullSizeVarsEntry.getValue();
        final Variable fullSizeVar = _variables.get(0);
        final NetcdfFile fullSizeNcFile = fullSizeVar.getNetcdfFile();
        assert fullSizeNcFile != null;
        final double fullTrackOffset = NetCDFUtils.getGlobalAttributeDouble("track_offset", fullSizeNcFile);
        final String fullAcrossResStr = NetCDFUtils.getGlobalAttributeString("resolution", fullSizeNcFile);
        final Number fullAcrossRes = Double.parseDouble(StringUtils.split(fullAcrossResStr, " ".toCharArray(), true)[1]);

        for (Map.Entry<int[], ArrayList<Variable>> entry : shapeGroupedVariablesMap.entrySet()) {
            final ArrayList<Variable> vars = entry.getValue();
            for (Variable var : vars) {
                final NetcdfFile varNcFile = var.getNetcdfFile();
                assert varNcFile != null;
                final double varTrackOffset = NetCDFUtils.getGlobalAttributeDouble("track_offset", varNcFile);
                final String varAcrossResStr = NetCDFUtils.getGlobalAttributeString("resolution", varNcFile);
                final Number varAcrossRes = Double.parseDouble(StringUtils.split(varAcrossResStr, " ".toCharArray(), true)[1]);
                final double subsamplingX = varAcrossRes.doubleValue() / fullAcrossRes.doubleValue();
                final double offsetX = fullTrackOffset - varTrackOffset * subsamplingX;
                _variables.add(
                        new SlstrSubsetTiePointVariable(var, fullSizeShape[1], fullSizeShape[0], offsetX, subsamplingX)
                );
            }
        }

        _variablesLUT = new HashMap<>();
        for (Variable variable : _variables) {
            final String shortName = variable.getShortName();
            _variablesLUT.put(shortName, variable);
        }
    }

    @Override
    public List<Variable> getVariables() throws IOException {
        return _variables;
    }

    private Comparator<int[]> getIntArrayComparator() {
        return (o1, o2) -> {
            int numIter = Math.min(o1.length, o2.length);
            for (int i = 0; i < numIter; i++) {
                int result = Integer.compare(o1[i], o2[i]);
                if (result != 0) {
                    return result;
                }
            }
            return Integer.compare(o1.length, o2.length);
        };
    }

    @Override
    public Dimension getProductSize() throws IOException {
        final int[] shape = _variables.get(0).getShape();
        return new Dimension("shape", shape[1], shape[0]);
    }

    @Override
    public String getLongitudeVariableName() {
        return LONGITUDE_VAR_NAME;
    }

    @Override
    public String getLatitudeVariableName() {
        return LATITUDE_VAR_NAME;
    }

    private boolean isZipFile(File file) {
        try (ZipFile ignored = new ZipFile(file)) {
            // Try with resource block to automatically close the ZipFile if it does not throw an exception
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void openNcFiles(Store store) throws IOException {
        final String suffix;
        if (_nadirView) {
            suffix = "n.nc";
        } else {
            suffix = "o.nc";
        }
        final TreeSet<String> keys = store.getKeysEndingWith(suffix);
        final TreeSet<String> geoTx = store.getKeysEndingWith("geodetic_tx.nc");
        final TreeSet<String> timeIn = store.getKeysEndingWith("time_in.nc");
        keys.addAll(geoTx);
        keys.addAll(timeIn);
        _ncFiles = new TreeMap<>();
        for (String key : keys) {
            final byte[] bytes = store.getBytes(key);
            final String name = extractName(key);
            final NetcdfFile netcdfFile = openInMemory(key, bytes);
            _ncFiles.put(name, netcdfFile);
        }
    }

    // package instead of private for testing purposes
    static String extractName(String key) {
        if (key.contains("\\")) {
            return key.substring(key.lastIndexOf("\\") + 1);
        } else {
            return key.substring(key.lastIndexOf("/") + 1);
        }
    }

    // just for testing tb 2022-07-18
    boolean isNadirView() {
        return _nadirView;
    }
}
