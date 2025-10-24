package com.bc.fiduceo.reader.insitu.generic;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CsvFormatConfigTest {

    @Test
    public void testLoadCsvFormatConfig_SM_success() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_SM);

        assertEquals("NDBC_SM", config.getName());
        assertEquals("space", config.getDelimiter());
        assertEquals('#', config.getCommentChar());
        assertEquals("\\\\w{5}h\\\\d{4}\\.txt", config.getRegex());
        assertEquals("longitude", config.getLongitudeName());
        assertEquals("latitude", config.getLatitudeName());
        assertEquals("time", config.getTimeName());
        assertEquals(5, config.getTimeVars().size());
        assertTrue(config.isLocationFromStationDatabase());

        List<GenericVariable> vars = config.getVariables();
        assertEquals(18, vars.size());

        GenericVariable var5 = vars.get(5);
        assertEquals("WDIR", var5.getName());
        assertEquals("short", var5.getType());
        assertEquals(ProductData.TYPE_INT16, var5.getProductData());
        assertEquals('v', var5.getOrigin());
        assertEquals(999, var5.getFillValue(), .00001);
        assertEquals("degT", var5.getUnits());
        assertEquals("Ten-minute average wind direction measurements in degrees clockwise from true North.", var5.getLongName());
        assertEquals("wind_from_direction", var5.getCfStandard());

        StationDatabase database = config.getStationDatabase();
        assertNotNull(database);

        String stationIdIdentifier = database.getPrimaryId();
        assertNotNull(stationIdIdentifier);
        assertEquals("id", stationIdIdentifier);

        List<GenericVariable> stationVars = database.getVariables();
        assertEquals(8, stationVars.size());

        assertEquals("id", stationVars.get(0).getName());
        assertEquals("latitude", stationVars.get(1).getName());
        assertEquals("longitude", stationVars.get(2).getName());
        assertEquals("type", stationVars.get(3).getName());
        assertEquals("anemometer_height", stationVars.get(4).getName());
        assertEquals("air_temp_height", stationVars.get(5).getName());
        assertEquals("barometer_height", stationVars.get(6).getName());
        assertEquals("sst_depth", stationVars.get(7).getName());

        GenericVariable stationAirTempHeight = stationVars.get(5);
        assertEquals("float", stationAirTempHeight.getType());
        assertEquals(ProductData.TYPE_FLOAT32, stationAirTempHeight.getProductData());
        assertEquals('s', stationAirTempHeight.getOrigin());
        assertNull(stationAirTempHeight.getFillValue());
        assertEquals("m", stationAirTempHeight.getUnits());
        assertEquals("Height of instrument above site elevation", stationAirTempHeight.getLongName());
        assertNull(stationAirTempHeight.getCfStandard());

        List<List<Object>> stations = database.getStations();
        assertNotNull(stations);
        assertFalse(stations.isEmpty());
        assertEquals(444, stations.size());

        List<Object> station = stations.get(443);
        assertEquals(8, station.size());

        assertEquals("YGNN6", station.get(0));
        assertEquals(43.262, station.get(1));
        assertEquals(-79.064, station.get(2));
        assertEquals(5, station.get(3));
        assertEquals(10.00, station.get(4));
        assertEquals(6.00, station.get(5));
        assertEquals(75.30, station.get(6));
        assertNull(station.get(7));
    }

    @Test
    public void testLoadCsvFormatConfig_CW_success() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_CW);

        assertEquals("NDBC_CW", config.getName());
        assertEquals("space", config.getDelimiter());
        assertEquals('#', config.getCommentChar());
        assertEquals("\\\\w{5}c\\\\d{4}\\.txt", config.getRegex());
        assertEquals("longitude", config.getLongitudeName());
        assertEquals("latitude", config.getLatitudeName());
        assertEquals("time", config.getTimeName());
        assertEquals(5, config.getTimeVars().size());
        assertTrue(config.isLocationFromStationDatabase());

        List<GenericVariable> vars = config.getVariables();
        assertEquals(10, vars.size());

        GenericVariable var9 = vars.get(9);
        assertEquals("GTIME", var9.getName());
        assertEquals("short", var9.getType());
        assertEquals(ProductData.TYPE_INT16, var9.getProductData());
        assertEquals('v', var9.getOrigin());
        assertEquals(9999, var9.getFillValue(), .00001);
        assertEquals("hhmm", var9.getUnits());
        assertEquals("The minute of the hour that the GSP occurred, reported at the last hourly 10-minute segment.", var9.getLongName());
        assertNull(var9.getCfStandard());

        StationDatabase database = config.getStationDatabase();
        assertNotNull(database);

        String stationIdIdentifier = database.getPrimaryId();
        assertNotNull(stationIdIdentifier);
        assertEquals("id", stationIdIdentifier);

        List<GenericVariable> stationVars = database.getVariables();
        assertEquals(5, stationVars.size());

        assertEquals("id", stationVars.get(0).getName());
        assertEquals("latitude", stationVars.get(1).getName());
        assertEquals("longitude", stationVars.get(2).getName());
        assertEquals("type", stationVars.get(3).getName());
        assertEquals("anemometer_height", stationVars.get(4).getName());

        GenericVariable stationAnemometerHeight = stationVars.get(4);
        assertEquals("float", stationAnemometerHeight.getType());
        assertEquals(ProductData.TYPE_FLOAT32, stationAnemometerHeight.getProductData());
        assertEquals('s', stationAnemometerHeight.getOrigin());
        assertNull(stationAnemometerHeight.getFillValue());
        assertEquals("m", stationAnemometerHeight.getUnits());
        assertEquals("Height of instrument above site elevation", stationAnemometerHeight.getLongName());
        assertNull(stationAnemometerHeight.getCfStandard());

        List<List<Object>> stations = database.getStations();
        assertNotNull(stations);
        assertFalse(stations.isEmpty());
        assertEquals(115, stations.size());

        List<Object> station = stations.get(114);
        assertEquals(5, station.size());

        assertEquals("STDM4", station.get(0));
        assertEquals(47.184, station.get(1));
        assertEquals(-87.225, station.get(2));
        assertEquals(5, station.get(3));
        assertEquals(35.2, station.get(4));
    }

    @Test
    public void testLoadCsvFormatConfig_GBOV_success() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_GBOV);

        assertEquals("GBOV", config.getName());
        assertEquals(";", config.getDelimiter());
        assertEquals('#', config.getCommentChar());
        assertEquals("^GBOV__(.*?)__(.*?)__([0-9]{8}T[0-9]{6}Z)__([0-9]{8}T[0-9]{6}Z)\\.csv$", config.getRegex());
        assertEquals("Lon_IS", config.getLongitudeName());
        assertEquals("Lat_IS", config.getLatitudeName());
        assertEquals("TIME_IS", config.getTimeName());
        assertNull(config.getTimeVars());
        assertTrue(config.isLocationFromStationDatabase());

        List<GenericVariable> vars = config.getVariables();
        assertEquals(61, vars.size());

        GenericVariable var9 = vars.get(7);
        assertEquals("RM6_down_flag", var9.getName());
        assertEquals("short", var9.getType());
        assertEquals(ProductData.TYPE_INT16, var9.getProductData());
        assertEquals('v', var9.getOrigin());
        assertEquals((short) -999, var9.getFillValue(), .00001);
        assertEquals("", var9.getUnits());
        assertEquals("down_flag", var9.getAncillaryVariables());

        StationDatabase database = config.getStationDatabase();
        assertNotNull(database);

        String siteIdIdentifier = database.getPrimaryId();
        assertNotNull(siteIdIdentifier);
        assertEquals("site", siteIdIdentifier);
        String stationIdIdentifier = database.getSecondaryId();
        assertNotNull(stationIdIdentifier);
        assertEquals("station", stationIdIdentifier);

        List<GenericVariable> stationVars = database.getVariables();
        assertEquals(6, stationVars.size());

        assertEquals("site", stationVars.get(0).getName());
        assertEquals("station", stationVars.get(1).getName());
        assertEquals("elevation", stationVars.get(2).getName());
        assertEquals("IGBP_class", stationVars.get(3).getName());
        assertEquals("Lat_IS", stationVars.get(4).getName());
        assertEquals("Lon_IS", stationVars.get(5).getName());

        GenericVariable stationLatitude = stationVars.get(4);
        assertEquals("float", stationLatitude.getType());
        assertEquals(ProductData.TYPE_FLOAT32, stationLatitude.getProductData());
        assertEquals('s', stationLatitude.getOrigin());
        assertNull(stationLatitude.getFillValue());
        assertEquals("degree_north", stationLatitude.getUnits());
        assertEquals("Latitude is positive northward; its units of degree_north (or equivalent) indicate this explicitly. In a latitude-longitude system defined with respect to a rotated North Pole, the standard name of grid_latitude should be used instead of latitude. Grid latitude is positive in the grid-northward direction, but its units should be plain degree.", stationLatitude.getLongName());
        assertEquals("latitude", stationLatitude.getCfStandard());

        List<List<Object>> stations = database.getStations();
        assertNotNull(stations);
        assertFalse(stations.isEmpty());

        assertEquals(781, stations.size());

        List<Object> station = stations.get(780);
        assertEquals(6, station.size());

        assertEquals("Payerne", station.get(0));
        assertEquals("Payerne", station.get(1));
        assertEquals(637, station.get(2));
        assertEquals("Evergreen Needleleaf", station.get(3));
        assertEquals(46.815, station.get(4));
        assertEquals(6.944, station.get(5));

    }

    @Test
    public void testLoadCsvFormatConfig_resourceNotFound() {
        try {
            CsvFormatConfig.loadConfig("XX");
            fail();
        } catch (Exception e) {
            assertEquals("Resource not found: XX_config.json", e.getMessage());
        }
    }

    @Test
    public void testGetAllVariables_GBOV() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_GBOV);

        List<GenericVariable> vars = config.getAllVariables();
        assertNotNull(vars);
        assertFalse(vars.isEmpty());
        assertEquals(67, vars.size());
    }

    @Test
    public void testGetAllVariables_NDBC_CW() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_CW);

        List<GenericVariable> vars = config.getAllVariables();
        assertNotNull(vars);
        assertFalse(vars.isEmpty());
        assertEquals(11, vars.size());
    }

    @Test
    public void testGetAllVariables_NDBC_SM() {
        CsvFormatConfig config = CsvFormatConfig.loadConfig(GenericCsvHelper.RESOURCE_KEY_NDBC_SM);

        List<GenericVariable> vars = config.getAllVariables();
        assertNotNull(vars);
        assertFalse(vars.isEmpty());
        assertEquals(22, vars.size());
    }
}