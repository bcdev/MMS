package com.bc.fiduceo.post.plugin.era5;

import com.bc.fiduceo.FiduceoConstants;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SatelliteFieldsTest {

    @Test
    public void testGetVariables() {
        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.setVarName("an_sfc_skt", "Skate");

        final Map<String, TemplateVariable> variables = satelliteFields.getVariables(config);
        assertEquals(13, variables.size());

        TemplateVariable template = variables.get("an_sfc_u10");
        assertNull(template.getStandardName());
        assertEquals("m s**-1", template.getUnits());
        assertEquals("10 metre U wind component", template.getLongName());
        assertEquals("nwp_u10", template.getName());
        assertFalse(template.is3d());

        template = variables.get("an_sfc_skt");
        assertNull(template.getStandardName());
        assertEquals("K", template.getUnits());
        assertEquals("Skin temperature", template.getLongName());
        assertEquals("Skate", template.getName());
        assertFalse(template.is3d());
    }

    @Test
    public void testGetVariables_withGeneralizedVariables() {
        //preparation
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        final TemplateVariable tempVar1 = new TemplateVariable("skt", "u1", "ln1", "sn1", true);
        final TemplateVariable tempVar2 = new TemplateVariable("kbb", "u2", "ln2", null, false);
        tempVar1.setFill_value(123.4f);
        tempVar2.setFill_value(123.4e-1f);
        final HashMap<String, TemplateVariable> generalizedVars = new HashMap<>();
        generalizedVars.put("an_sfc_skt", tempVar1);
        generalizedVars.put("an_sfc_kbb", tempVar2);
        config.setGeneralizedVariables(generalizedVars);

        config.setSensorRef("DerSensor");
        config.setVarName("an_sfc_skt", "{sensor-ref}.und-skt");
        config.setVarName("an_sfc_kbb", "{sensor-ref}.morgenrot");

        final SatelliteFields satelliteFields = new SatelliteFields();

        //execution
        final Map<String, TemplateVariable> variables = satelliteFields.getVariables(config);

        //verification
        assertEquals(2, variables.size());

        TemplateVariable template = variables.get("an_sfc_skt");
        assertEquals("sn1", template.getStandardName());
        assertEquals("u1", template.getUnits());
        assertEquals("ln1", template.getLongName());
        assertEquals("DerSensor.und-skt", template.getName());
        assertThat(template.getFillValue(), is(123.4f));
        assertTrue(template.is3d());

        template = variables.get("an_sfc_kbb");
        assertNull(template.getStandardName());
        assertEquals("u2", template.getUnits());
        assertEquals("ln2", template.getLongName());
        assertEquals("DerSensor.morgenrot", template.getName());
        assertThat(template.getFillValue(), is(12.34f));
        assertFalse(template.is3d());
    }

    @Test
    public void testSetGetDimensions_2D() {
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 10));

        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = createConfig();
        config.setMatchupDimensionName(FiduceoConstants.MATCHUP_COUNT);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.get_x_dim_name(), config.get_x_dim())).thenReturn(new Dimension(config.get_x_dim_name(), config.get_x_dim()));
        when(writer.addDimension(config.get_y_dim_name(), config.get_y_dim())).thenReturn(new Dimension(config.get_y_dim_name(), config.get_y_dim()));

        satelliteFields.setDimensions(config, writer, ncFile);

        final List<Dimension> dimensions = satelliteFields.getDimensions(new TemplateVariable("what", "ever", "we", "write", false));
        assertEquals(3, dimensions.size());

        Dimension dimension = dimensions.get(0);
        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimension.getShortName());
        assertEquals(10, dimension.getLength());

        dimension = dimensions.get(1);
        assertEquals("y_dim", dimension.getShortName());
        assertEquals(14, dimension.getLength());

        dimension = dimensions.get(2);
        assertEquals("x_dim", dimension.getShortName());
        assertEquals(12, dimension.getLength());

        verify(writer, times(3)).addDimension(anyString(), anyInt());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testSetGetDimensions_3D() {
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 11));

        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = createConfig();
        config.setMatchupDimensionName(FiduceoConstants.MATCHUP_COUNT);
        config.set_z_dim(17);

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.get_x_dim_name(), config.get_x_dim())).thenReturn(new Dimension(config.get_x_dim_name(), config.get_x_dim()));
        when(writer.addDimension(config.get_y_dim_name(), config.get_y_dim())).thenReturn(new Dimension(config.get_y_dim_name(), config.get_y_dim()));
        when(writer.addDimension(config.get_z_dim_name(), config.get_z_dim())).thenReturn(new Dimension(config.get_z_dim_name(), config.get_z_dim()));

        satelliteFields.setDimensions(config, writer, ncFile);

        final List<Dimension> dimensions = satelliteFields.getDimensions(new TemplateVariable("what", "ever", "we", "write", true));
        assertEquals(4, dimensions.size());

        Dimension dimension = dimensions.get(0);
        assertEquals(FiduceoConstants.MATCHUP_COUNT, dimension.getShortName());
        assertEquals(11, dimension.getLength());

        dimension = dimensions.get(1);
        assertEquals("z_dim", dimension.getShortName());
        assertEquals(17, dimension.getLength());

        dimension = dimensions.get(2);
        assertEquals("y_dim", dimension.getShortName());
        assertEquals(14, dimension.getLength());

        dimension = dimensions.get(3);
        assertEquals("x_dim", dimension.getShortName());
        assertEquals(12, dimension.getLength());

        verify(writer, times(3)).addDimension(anyString(), anyInt());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testSetGetDimensions_2D_nameToEscape() {
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension(FiduceoConstants.MATCHUP_COUNT)).thenReturn(new Dimension(FiduceoConstants.MATCHUP_COUNT, 10));

        final SatelliteFields satelliteFields = new SatelliteFields();
        final SatelliteFieldsConfiguration config = createConfig();
        config.set_x_dim_name("x.dim.ension");
        config.set_y_dim_name("y.dim.ension");

        final NetcdfFileWriter writer = mock(NetcdfFileWriter.class);
        when(writer.addDimension(config.get_x_dim_name(), config.get_x_dim())).thenReturn(new Dimension(config.get_x_dim_name(), config.get_x_dim()));
        when(writer.addDimension(config.get_y_dim_name(), config.get_y_dim())).thenReturn(new Dimension(config.get_y_dim_name(), config.get_y_dim()));

        satelliteFields.setDimensions(config, writer, ncFile);

        final List<Dimension> dimensions = satelliteFields.getDimensions(new TemplateVariable("what", "ever", "we", "write", false));
        assertEquals(3, dimensions.size());

        Dimension dimension = dimensions.get(1);
        assertEquals("y.dim.ension", dimension.getShortName());
        assertEquals(14, dimension.getLength());

        dimension = dimensions.get(2);
        assertEquals("x.dim.ension", dimension.getShortName());
        assertEquals(12, dimension.getLength());

        verify(writer, times(3)).addDimension(anyString(), anyInt());
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void testMergeData_2D_left() {
        final Array right = createArray(3, 1, 1);
        final Array left = createArray(3, 2, 4);

        Array array = mock(Array.class);
        when(array.getRank()).thenReturn(3);
        when(array.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 1, new Rectangle(-1, 100, 3, 3), array);
        final int[] shape = merged.getShape();
        assertEquals(2, shape.length);
        assertEquals(3, shape[0]);
        assertEquals(3, shape[1]);

        final Index index = merged.getIndex();
        index.set(0, 0);
        assertEquals(4, merged.getInt(index));
        index.set(0, 1);
        assertEquals(5, merged.getInt(index));
        index.set(0, 2);
        assertEquals(1, merged.getInt(index));

        verify(array, times(1)).getRank();
        verify(array, times(1)).getDataType();
        verifyNoMoreInteractions(array);
    }

    @Test
    public void testMergeData_2D_right() {
        final Array left = createArray(3, 1, 6);
        final Array right = createArray(3, 2, 0);

        Array array = mock(Array.class);
        when(array.getRank()).thenReturn(3);
        when(array.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 1, new Rectangle(1438, 100, 3, 3), array);
        final int[] shape = merged.getShape();
        assertEquals(2, shape.length);
        assertEquals(3, shape[0]);
        assertEquals(3, shape[1]);

        final Index index = merged.getIndex();
        index.set(0, 0);
        assertEquals(0, merged.getInt(index));
        index.set(0, 1);
        assertEquals(1, merged.getInt(index));
        index.set(0, 2);
        assertEquals(6, merged.getInt(index));

        verify(array, times(1)).getRank();
        verify(array, times(1)).getDataType();
        verifyNoMoreInteractions(array);
    }

    @Test
    public void testMergeData_3D_left() {
        final Array right = createArray_3D(5, 3, 1, 1);
        final Array left = createArray_3D(5, 3, 2, 100);

        Array array = mock(Array.class);
        when(array.getRank()).thenReturn(4);
        when(array.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 5, new Rectangle(-1, 100, 3, 3), array);
        final int[] shape = merged.getShape();
        assertEquals(3, shape.length);
        assertEquals(5, shape[0]);
        assertEquals(3, shape[1]);
        assertEquals(3, shape[2]);

        final Index index = merged.getIndex();
        index.set(0, 0, 0);
        assertEquals(100, merged.getInt(index));
        index.set(0, 0, 1);
        assertEquals(101, merged.getInt(index));
        index.set(0, 0, 2);
        assertEquals(1, merged.getInt(index));

        index.set(1, 0, 0);
        assertEquals(106, merged.getInt(index));
        index.set(1, 0, 1);
        assertEquals(107, merged.getInt(index));
        index.set(1, 0, 2);
        assertEquals(4, merged.getInt(index));

        verify(array, times(1)).getRank();
        verify(array, times(1)).getDataType();
        verifyNoMoreInteractions(array);
    }

    @Test
    public void testMergeData_3D_right() {
        final Array left = createArray_3D(6, 3, 1, 0);
        final Array right = createArray_3D(6, 3, 2, 100);

        Array array = mock(Array.class);
        when(array.getRank()).thenReturn(4);
        when(array.getDataType()).thenReturn(DataType.INT);

        final Array merged = SatelliteFields.mergeData(left, right, 6, new Rectangle(1438, 100, 3, 3), array);
        final int[] shape = merged.getShape();
        assertEquals(3, shape.length);
        assertEquals(6, shape[0]);
        assertEquals(3, shape[1]);
        assertEquals(3, shape[1]);

        final Index index = merged.getIndex();
        index.set(1, 0, 0);
        assertEquals(106, merged.getInt(index));
        index.set(1, 0, 1);
        assertEquals(107, merged.getInt(index));
        index.set(1, 0, 2);
        assertEquals(3, merged.getInt(index));

        index.set(2, 0, 0);
        assertEquals(112, merged.getInt(index));
        index.set(2, 0, 1);
        assertEquals(113, merged.getInt(index));
        index.set(2, 0, 2);
        assertEquals(6, merged.getInt(index));

        verify(array, times(1)).getRank();
        verify(array, times(1)).getDataType();
        verifyNoMoreInteractions(array);
    }

    private Array createArray(int height, int width, int start) {
        final Array array = Array.factory(DataType.INT, new int[]{height, width});
        Index index = array.getIndex();
        int value = start;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                index.set(y, x);
                array.setInt(index, value);
                ++value;
            }
        }
        return array;
    }

    private Array createArray_3D(int depth, int height, int width, int start) {
        final Array array = Array.factory(DataType.INT, new int[]{depth, height, width});
        Index index = array.getIndex();
        int value = start;
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    index.set(z, y, x);
                    array.setInt(index, value);
                    ++value;
                }
            }
        }
        return array;
    }

    private SatelliteFieldsConfiguration createConfig() {
        final SatelliteFieldsConfiguration config = new SatelliteFieldsConfiguration();
        config.set_x_dim(12);
        config.set_x_dim_name("x_dim");
        config.set_y_dim(14);
        config.set_y_dim_name("y_dim");
        config.set_z_dim_name("z_dim");
        config.set_nwp_time_variable_name("nwp_time");
        config.set_time_variable_name("sat_time");
        config.set_longitude_variable_name("sat_lon");
        config.set_latitude_variable_name("sat_lat");
        return config;
    }
}
