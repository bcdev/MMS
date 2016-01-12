/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo.db;


import com.bc.fiduceo.core.SatelliteObservation;
import com.bc.fiduceo.core.Sensor;
import com.bc.fiduceo.core.ServicesUtils;
import com.bc.fiduceo.geometry.GeometryFactory;
import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.util.List;

public class Storage {

    private Driver driver;

    Storage(BasicDataSource dataSource, GeometryFactory geometryFactory) throws SQLException {
        driver = createDriver(dataSource);
        if (driver == null) {
            throw new IllegalArgumentException("No database driver registered for URL `" + dataSource.getUrl() + "`");
        }

        driver.setGeometryFactory(geometryFactory);

        driver.open(dataSource);
    }

    public static Storage create(BasicDataSource dataSource, GeometryFactory geometryFactory) throws SQLException {
        return new Storage(dataSource, geometryFactory);
    }

    public void close() throws SQLException {
        if (driver != null) {
            driver.close();
            driver = null;
        }
    }

    public void initialize() throws SQLException {
        driver.initialize();
    }

    public void clear() throws SQLException {
        driver.clear();
    }

    public void insert(SatelliteObservation satelliteObservation) throws SQLException {
        driver.insert(satelliteObservation);
    }

    public List<SatelliteObservation> get() throws SQLException {
        return driver.get();
    }

    public List<SatelliteObservation> get(QueryParameter parameter) throws SQLException {
        return driver.get(parameter);
    }

    public int insert(Sensor sensor) throws SQLException {
        return driver.insert(sensor);
    }

    private Driver createDriver(BasicDataSource dataSource) {
        final String dbUrl = dataSource.getUrl().toLowerCase();
        ServicesUtils syServicesUtils = new ServicesUtils<>();
        return (Driver) syServicesUtils.getReader(Driver.class, dbUrl);
    }
}
