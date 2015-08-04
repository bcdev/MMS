package com.bc.fiduceo.db;


import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.apache.commons.dbcp.BasicDataSource;
import org.esa.snap.BeamCoreActivator;

import java.sql.SQLException;
import java.util.Set;

public class Storage {

    private static Storage storage;

    private Driver driver;


    public static Storage create(BasicDataSource dataSource) throws SQLException {
        if (storage == null) {
            storage = new Storage(dataSource);
        }
        return storage;
    }

    public void close() throws SQLException {
        if (storage == null) {
            return;
        }

        if (driver != null) {
            driver.close();
            driver = null;
        }

        storage = null;
    }

    Storage(BasicDataSource dataSource) throws SQLException {
        driver = createDriver(dataSource);
        if (driver == null) {
            throw new IllegalArgumentException("No database driver registered for URL `" + dataSource.getUrl() + "`");
        }

        driver.open(dataSource);
    }

    private Driver createDriver(BasicDataSource dataSource) {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<Driver> driverRegistry = serviceRegistryManager.getServiceRegistry(Driver.class);

        BeamCoreActivator.loadServices(driverRegistry);
        final Set<Driver> services = driverRegistry.getServices();
        final String dbUrl = dataSource.getUrl().toLowerCase();
        for (final Driver driver : services) {
            final String urlPattern = driver.getUrlPattern().toLowerCase();
            if (dbUrl.startsWith(urlPattern)) {
                return driver;
            }
        }

        return null;
    }
}
