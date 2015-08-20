package com.bc.fiduceo.db;


import org.apache.commons.dbcp.BasicDataSource;

public class StorageTest_SatelliteObservation_H2 extends StorageTest_SatelliteObservation {

    public StorageTest_SatelliteObservation_H2() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:fiduceo");
    }
}