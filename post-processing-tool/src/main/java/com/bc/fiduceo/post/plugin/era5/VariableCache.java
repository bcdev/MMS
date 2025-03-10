package com.bc.fiduceo.post.plugin.era5;

import org.apache.commons.lang3.StringUtils;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class VariableCache {

    private final Era5Archive archive;

    // We use a LRUCacheWithListener here so that the oldest entry will be removed automatically.
    // This requires a listener to be added to this LRUCache.
    private final LRUCacheWithListener<Integer, Map<String,CacheContainer>> cacheTimeStamp;

    VariableCache(Era5Archive archive, int cacheSize) {
        this.archive = archive;
        cacheTimeStamp = new LRUCacheWithListener<>(cacheSize);
        cacheTimeStamp.setListener((key, value) -> freeContainers(value));
    }

    Variable get(String variableKey, int era5TimeStamp) throws IOException {
        final CacheContainer cacheContainer = getCacheContainer(variableKey, era5TimeStamp);
        if (cacheContainer == null) {
            return createCacheContainer(variableKey, era5TimeStamp).variable;
        }
        return cacheContainer.variable;
    }

    private CacheContainer getCacheContainer(String variableKey, int era5TimeStamp) throws IOException {
        final Map<String, CacheContainer> cacheContainer = cacheTimeStamp.get(era5TimeStamp);
        if (cacheContainer != null) {
            return cacheContainer.get(variableKey);
        }
        return null;
    }

    private CacheContainer createCacheContainer(String variableKey, int era5TimeStamp) throws IOException {
        final String filePath = archive.get(variableKey, era5TimeStamp);
        final String variableName = getVariableName(variableKey);

        final NetcdfFile netcdfFile = NetcdfFile.open(filePath);
        final Variable variable = netcdfFile.findVariable(variableName);
        if (variable == null) {
            throw new IOException("variable not found: " + variableName + "  " + filePath);
        }

        CacheContainer cacheContainer = new CacheContainer(variable, netcdfFile);
        final Map<String, CacheContainer> cacheContainerMap;
        if (cacheTimeStamp.containsKey(era5TimeStamp)) {
            cacheContainerMap = cacheTimeStamp.get(era5TimeStamp);
        } else {
            cacheContainerMap = new HashMap<>();
            cacheTimeStamp.put(era5TimeStamp, cacheContainerMap);
        }
        cacheContainerMap.put(variableKey, cacheContainer);
        return cacheContainer;
    }

    void close() {
        for (Map<String, CacheContainer> cacheContainerMap : cacheTimeStamp.values()) {
            freeContainers(cacheContainerMap);
        }
        cacheTimeStamp.clear();
    }

    private String getVariableName(String variableKey) {
        final int cutPoint = StringUtils.ordinalIndexOf(variableKey, "_", 2);
        return variableKey.substring(cutPoint + 1);
    }

    private void freeContainers(Map<String, CacheContainer> cacheContainers) {
        for (CacheContainer container : cacheContainers.values()) {
            container.variable = null;
            try {
                container.netcdfFile.close();
                container.netcdfFile = null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        cacheContainers.clear();
    }

    private static class LRUCacheWithListener<K,V> extends LinkedHashMap<K,V> {
        private int capacity;
        private RemovalListener<K, V> listener;
        private boolean listenerWasSet = false;

        public LRUCacheWithListener(int capacity) {
            super(capacity + 1, 1.0f, true); // Pass 'true' for accessOrder.
            this.capacity = capacity;
        }

        public interface RemovalListener<K, V> {
            void onRemove(K key, V value);
        }

        public void setListener(RemovalListener<K, V> listener) {
            this.listener = listener;
            listenerWasSet = listener != null;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            boolean shouldRemove = this.size() > capacity;
            if (listenerWasSet && shouldRemove) {
                listener.onRemove(eldest.getKey(), eldest.getValue());
            }
            return shouldRemove;
        }
    }

    private static class CacheContainer {
        Variable variable;
        NetcdfFile netcdfFile;

        CacheContainer(Variable variable, NetcdfFile netcdfFile) {
            this.variable = variable;
            this.netcdfFile = netcdfFile;
        }
    }
}
