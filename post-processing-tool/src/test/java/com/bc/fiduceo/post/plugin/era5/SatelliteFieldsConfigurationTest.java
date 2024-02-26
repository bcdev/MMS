package com.bc.fiduceo.post.plugin.era5;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SatelliteFieldsConfigurationTest {

    private SatelliteFieldsConfiguration config;

    @Before
    public void setUp() {
        config = new SatelliteFieldsConfiguration();
    }

    @Test
    public void testConstructionAndDefaultValues() {
        assertEquals("nwp_q", config.getVarName("an_ml_q"));
        assertEquals("nwp_t", config.getVarName("an_ml_t"));
        assertEquals("nwp_o3", config.getVarName("an_ml_o3"));
        assertEquals("nwp_lnsp", config.getVarName("an_ml_lnsp"));
        assertEquals("nwp_t2m", config.getVarName("an_sfc_t2m") );
        assertEquals("nwp_siconc", config.getVarName("an_sfc_siconc")  );
        assertEquals("nwp_u10", config.getVarName("an_sfc_u10")  );
        assertEquals("nwp_v10", config.getVarName("an_sfc_v10")  );
        assertEquals("nwp_msl", config.getVarName("an_sfc_msl"));
        assertEquals("nwp_skt", config.getVarName("an_sfc_skt"));
        assertEquals("nwp_sst", config.getVarName("an_sfc_sst"));
        assertEquals("nwp_tcc", config.getVarName("an_sfc_tcc"));
        assertEquals("nwp_tcwv", config.getVarName("an_sfc_tcwv"));

        assertEquals(-1, config.get_x_dim());
        assertEquals(-1, config.get_y_dim());
        assertEquals(-1, config.get_z_dim());
        assertNull(config.get_x_dim_name());
        assertNull(config.get_y_dim_name());
        assertNull(config.get_z_dim_name());
        assertNull(config.get_nwp_time_variable_name());
        assertNull(config.get_time_variable_name());
        assertNull(config.get_longitude_variable_name());
        assertNull(config.get_latitude_variable_name());
        assertNull(config.getSensorRef());
    }

    @Test
    public void testSetGetVarName() {
        config.setVarName("an_ml_q","anku");
        assertEquals("anku", config.getVarName("an_ml_q"));
        config.setVarName("an_ml_t","tee");
        assertEquals("tee", config.getVarName("an_ml_t"));
        config.setVarName("an_ml_o3", "ozzi");
        assertEquals("ozzi", config.getVarName("an_ml_o3"));
        config.setVarName("an_ml_lnsp", "pratt");
        assertEquals("pratt", config.getVarName("an_ml_lnsp"));
        config.setVarName("an_sfc_t2m",  "tempi");
        assertEquals("tempi", config.getVarName("an_sfc_t2m") );
        config.setVarName("an_sfc_u10", "windu");
        assertEquals("windu", config.getVarName("an_sfc_u10")  );
        config.setVarName("an_sfc_v10", "Vicky");
        assertEquals("Vicky", config.getVarName("an_sfc_v10")  );
        config.setVarName("an_sfc_siconc", "sieglinde");
        assertEquals("sieglinde", config.getVarName("an_sfc_siconc")  );
        config.setVarName("an_sfc_msl", "meanSurf");
        assertEquals("meanSurf", config.getVarName("an_sfc_msl"));
        config.setVarName("an_sfc_skt", "scinny");
        assertEquals("scinny", config.getVarName("an_sfc_skt"));
        config.setVarName("an_sfc_sst", "seaTemp");
        assertEquals("seaTemp", config.getVarName("an_sfc_sst"));
        config.setVarName("an_sfc_tcc", "cloudCover");
        assertEquals("cloudCover", config.getVarName("an_sfc_tcc"));
        config.setVarName("an_sfc_tcwv", "steamy");
        assertEquals("steamy", config.getVarName("an_sfc_tcwv"));
    }

    @Test
    public void testSetGet_x_dim() {
        config.set_x_dim(12);
        assertEquals(12, config.get_x_dim());
    }

    @Test
    public void testSetGet_y_dim() {
        config.set_y_dim(13);
        assertEquals(13, config.get_y_dim());
    }

    @Test
    public void testSetGet_z_dim() {
        config.set_z_dim(14);
        assertEquals(14, config.get_z_dim());
    }

    @Test
    public void testSetGet_x_dim_name() {
        config.set_x_dim_name("watussi");
        assertEquals("watussi", config.get_x_dim_name());
    }

    @Test
    public void testSetGet_y_dim_name() {
        config.set_y_dim_name("yacanda");
        assertEquals("yacanda", config.get_y_dim_name());
    }

    @Test
    public void testSetGet_z_dim_name() {
        config.set_z_dim_name("zauberfee");
        assertEquals("zauberfee", config.get_z_dim_name());
    }

    @Test
    public void testSetGetSensorRef() {
        config.setSensorRef("radiometer");
        assertEquals("radiometer", config.getSensorRef());
    }

    @Test
    public void testGetVariablesWithReplacement() {
        config.setSensorRef("avhrr-n14");

        config.set_nwp_time_variable_name("{sensor-ref}_nwp_time");
        assertEquals("avhrr-n14_nwp_time", config.get_nwp_time_variable_name());

        config.set_longitude_variable_name("{sensor-ref}_nwp_lon");
        assertEquals("avhrr-n14_nwp_lon", config.get_longitude_variable_name());

        config.set_latitude_variable_name("{sensor-ref}_nwp_lat");
        assertEquals("avhrr-n14_nwp_lat", config.get_latitude_variable_name());

        config.set_time_variable_name("{sensor-ref}_acquisition-time");
        assertEquals("avhrr-n14_acquisition-time", config.get_time_variable_name());

        config.setVarName("an_ml_q", "a_{sensor-ref}_n_q");
        assertEquals("a_avhrr-n14_n_q", config.getVarName("an_ml_q"));

        config.setVarName("an_ml_t","an_{sensor-ref}_t");
        assertEquals("an_avhrr-n14_t", config.getVarName("an_ml_t"));

        config.setVarName("an_ml_o3", "{sensor-ref}_o3");
        assertEquals("avhrr-n14_o3", config.getVarName("an_ml_o3"));

        config.setVarName("an_ml_lnsp", "{sensor-ref}_lnsp");
        assertEquals("avhrr-n14_lnsp", config.getVarName("an_ml_lnsp"));

        config.setVarName("an_sfc_t2m",  "{sensor-ref}_t2m");
        assertEquals("avhrr-n14_t2m", config.getVarName("an_sfc_t2m") );

        config.setVarName("an_sfc_siconc", "{sensor-ref}_siconc");
        assertEquals("avhrr-n14_siconc", config.getVarName("an_sfc_siconc")  );

        config.setVarName("an_sfc_u10", "{sensor-ref}_u10");
        assertEquals("avhrr-n14_u10", config.getVarName("an_sfc_u10")  );

        config.setVarName("an_sfc_v10", "{sensor-ref}_v10");
        assertEquals("avhrr-n14_v10", config.getVarName("an_sfc_v10")  );

        config.setVarName("an_sfc_msl", "{sensor-ref}_msl");
        assertEquals("avhrr-n14_msl", config.getVarName("an_sfc_msl"));

        config.setVarName("an_sfc_skt", "{sensor-ref}_skt");
        assertEquals("avhrr-n14_skt", config.getVarName("an_sfc_skt"));

        config.setVarName("an_sfc_sst", "{sensor-ref}_sst");
        assertEquals("avhrr-n14_sst", config.getVarName("an_sfc_sst"));

        config.setVarName("an_sfc_tcc", "{sensor-ref}_tcc");
        assertEquals("avhrr-n14_tcc", config.getVarName("an_sfc_tcc"));

        config.setVarName("an_sfc_tcwv", "{sensor-ref}_tcwv");
        assertEquals("avhrr-n14_tcwv", config.getVarName("an_sfc_tcwv"));
    }

    @Test
    public void testVerify() {
        prepareConfig();

        config.verify();
    }

    private void prepareConfig() {
        config.set_x_dim(3);
        config.set_x_dim_name("A");
        config.set_y_dim(4);
        config.set_y_dim_name("B");
        config.set_z_dim(4);
        config.set_z_dim_name("C");
        config.set_nwp_time_variable_name("D");
        config.set_time_variable_name("E");
        config.set_longitude_variable_name("F");
        config.set_latitude_variable_name("G");
    }

    @Test
    public void testVerify_x_dim() {
        prepareConfig();
        config.set_x_dim(-1);

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_x_dim_name() {
        prepareConfig();
        config.set_x_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_y_dim() {
        prepareConfig();
        config.set_y_dim(-2);

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_y_dim_name() {
        prepareConfig();
        config.set_y_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_z_dim_name() {
        prepareConfig();
        config.set_z_dim_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_nwp_time_variable_name() {
        prepareConfig();
        config.set_nwp_time_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_time_variable_name() {
        prepareConfig();
        config.set_time_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_lon_variable_name() {
        prepareConfig();
        config.set_longitude_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testVerify_lat_variable_name() {
        prepareConfig();
        config.set_latitude_variable_name("");

        try {
            config.verify();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testSetGet_nwp_time_variable_name() {
        config.set_nwp_time_variable_name("tickTock");
        assertEquals("tickTock", config.get_nwp_time_variable_name());
    }

    @Test
    public void testSetGet_time_variable_name() {
        config.set_time_variable_name("twomins");
        assertEquals("twomins", config.get_time_variable_name());
    }
}
