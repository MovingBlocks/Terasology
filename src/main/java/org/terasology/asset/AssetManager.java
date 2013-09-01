/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.asset;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.persistence.ModuleContext;
import org.terasology.utilities.collection.NullIterator;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// TODO: Split out an interface, possibly two with one for loading and adding assets, the other with disposal and other more management methods
public class AssetManager {

    private static final Logger logger = LoggerFactory.getLogger(AssetManager.class);

    private ModuleManager moduleManager;
    private Map<String, AssetSource> assetSources = Maps.newHashMap();
    private Map<AssetType, Map<String, AssetLoader>> assetLoaders = Maps.newEnumMap(AssetType.class);
    private Map<AssetUri, Asset> assetCache = Maps.newHashMap();
    private Map<AssetUri, AssetSource> overrides = Maps.newHashMap();
    private Map<AssetType, AssetFactory> factories = Maps.newHashMap();
    private Map<AssetType, Table<String, String, AssetUri>> uriLookup = Maps.newHashMap();

    public AssetManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {

            @Override
            public Prefab buildAsset(AssetUri uri, PrefabData data) {
                return new PojoPrefab(uri, data);
            }
        });
        setAssetFactory(AssetType.SHAPE, new AssetFactory<BlockShapeData, BlockShape>() {

            @Override
            public BlockShape buildAsset(AssetUri uri, BlockShapeData data) {
                return new BlockShapeImpl(uri, data);
            }
        });
        for (AssetType type : AssetType.values()) {
            uriLookup.put(type, HashBasedTable.<String, String, AssetUri>create());
        }
    }

    public void setAssetFactory(AssetType type, AssetFactory factory) {
        factories.put(type, factory);
    }

    public List<AssetUri> resolveAll(AssetType type, String name) {
        return Lists.newArrayList(uriLookup.get(type).row(SimpleUri.normalise(name)).values());
    }

    public AssetUri resolve(AssetType type, String name) {
        AssetUri uri = new AssetUri(type, name);
        if (uri.isValid()) {
            return uri;
        }
        List<AssetUri> possibilities = resolveAll(type, name);
        switch (possibilities.size()) {
            case 0:
                return null;
            case 1:
                return possibilities.get(0);
            default:
                Module context = ModuleContext.getContext();
                if (context != null) {
                    Set<String> dependencies = moduleManager.getDependencyNamesOf(context);
                    Iterator<AssetUri> iterator = possibilities.iterator();
                    while (iterator.hasNext()) {
                        AssetUri possibleUri = iterator.next();
                        if (!dependencies.contains(possibleUri.getModuleName())) {
                            iterator.remove();
                        }
                    }
                    if (possibilities.size() == 1) {
                        return possibilities.get(0);
                    }
                }
                return null;
        }
    }

    public <T extends AssetData> T resolveAndLoadData(AssetType type, String name, Class<T> dataClass) {
        AssetData data = resolveAndLoadData(type, name);
        if (dataClass.isInstance(data)) {
            return dataClass.cast(data);
        }
        return null;
    }

    public <T extends AssetData> T resolveAndTryLoadData(AssetType type, String name, Class<T> dataClass) {
        AssetData data = resolveAndTryLoadData(type, name);
        if (dataClass.isInstance(data)) {
            return dataClass.cast(data);
        }
        return null;
    }

    public AssetData resolveAndLoadData(AssetType type, String name) {
        AssetUri uri = resolve(type, name);
        if (uri != null) {
            return loadAssetData(uri, true);
        }
        return null;
    }

    public AssetData resolveAndTryLoadData(AssetType type, String name) {
        AssetUri uri = resolve(type, name);
        if (uri != null) {
            return loadAssetData(uri, false);
        }
        return null;
    }

    public <T extends Asset> T resolveAndLoad(AssetType type, String name, Class<T> assetClass) {
        Asset result = resolveAndLoad(type, name);
        if (assetClass.isInstance(result)) {
            return assetClass.cast(result);
        }
        return null;
    }

    public <T extends Asset> T resolveAndTryLoad(AssetType type, String name, Class<T> assetClass) {
        Asset result = resolveAndTryLoad(type, name);
        if (assetClass.isInstance(result)) {
            return assetClass.cast(result);
        }
        return null;
    }

    public Asset resolveAndLoad(AssetType type, String name) {
        AssetUri uri = resolve(type, name);
        if (uri != null) {
            return loadAsset(uri, false);
        }
        return null;
    }

    public Asset resolveAndTryLoad(AssetType type, String name) {
        AssetUri uri = resolve(type, name);
        if (uri != null) {
            return loadAsset(uri, true);
        }
        return null;
    }

    public <T> T tryLoadAssetData(AssetUri uri, Class<T> type) {
        AssetData data = loadAssetData(uri, false);
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        return null;
    }

    public <T> T loadAssetData(AssetUri uri, Class<T> type) {
        AssetData data = loadAssetData(uri, true);
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        return null;
    }

    public void register(AssetType type, String extension, AssetLoader loader) {
        Map<String, AssetLoader> assetTypeMap = assetLoaders.get(type);
        if (assetTypeMap == null) {
            assetTypeMap = Maps.newHashMap();
            assetLoaders.put(type, assetTypeMap);
        }
        assetTypeMap.put(extension.toLowerCase(Locale.ENGLISH), loader);
    }

    public <T extends Asset> T tryLoadAsset(AssetUri uri, Class<T> type) {
        Asset result = loadAsset(uri, false);
        if (type.isInstance(result)) {
            return type.cast(result);
        }
        return null;
    }

    public Asset loadAsset(AssetUri uri) {
        return loadAsset(uri, true);
    }

    public <T extends Asset> T loadAsset(AssetUri uri, Class<T> assetClass) {
        Asset result = loadAsset(uri, true);
        if (assetClass.isInstance(result)) {
            return assetClass.cast(result);
        }
        return null;
    }

    private AssetData loadAssetData(AssetUri uri, boolean logErrors) {
        if (!uri.isValid()) {
            return null;
        }

        List<URL> urls = getAssetURLs(uri);
        if (urls.size() == 0) {
            if (logErrors) {
                logger.warn("Unable to resolve asset: {}", uri);
            }
            return null;
        }

        for (URL url : urls) {
            int extensionIndex = url.toString().lastIndexOf('.');
            if (extensionIndex == -1) {
                continue;
            }

            String extension = url.toString().substring(extensionIndex + 1).toLowerCase(Locale.ENGLISH);
            Map<String, AssetLoader> extensionMap = assetLoaders.get(uri.getAssetType());
            if (extensionMap == null) {
                continue;
            }

            AssetLoader loader = extensionMap.get(extension);
            if (loader == null) {
                continue;
            }

            Module module = moduleManager.getModule(uri.getModuleName());
            InputStream stream = null;
            try {
                stream = url.openStream();
                urls.remove(url);
                urls.add(0, url);
                return loader.load(module, stream, urls);
            } catch (IOException ioe) {
                logger.error("Error reading asset {}", uri, ioe);
                return null;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException innerException) {
                        logger.error("Error closing stream for {}", uri, innerException);
                    }
                }
            }
        }
        logger.warn("Unable to resolve asset: {}", uri);
        return null;
    }

    private Asset loadAsset(AssetUri uri, boolean logErrors) {
        if (!uri.isValid()) {
            return null;
        }

        Asset asset = assetCache.get(uri);
        if (asset != null) {
            return asset;
        }

        AssetFactory factory = factories.get(uri.getAssetType());
        if (factory == null) {
            logger.error("No asset factory set for assets of type {}", uri.getAssetType());
            return null;
        }

        try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(moduleManager.getModule(uri.getModuleName()))) {
            AssetData data = loadAssetData(uri, logErrors);

            if (data != null) {
                asset = factory.buildAsset(uri, data);
                if (asset != null) {
                    logger.debug("Loaded {}", uri);
                    assetCache.put(uri, asset);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading asset: {}", e);
        }
        return asset;
    }

    public void clear() {
        Iterator<Asset> iterator = assetCache.values().iterator();
        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            if (asset instanceof Prefab) {
                asset.dispose();
                iterator.remove();
            }
        }
        // TODO: Fix disposal
//        Iterator<Asset> iterator = assetCache.values().iterator();
//        while (iterator.hasNext()) {
//            Asset asset = iterator.next();
//
//            // Don't dispose engine assets, all sorts of systems have references to them
//            if (!asset.getURI().getModuleName().equals(ModuleManager.ENGINE_MODULE)) {
//                asset.dispose();
//                iterator.remove();
//            }
//        }
    }

    public <U extends AssetData> Asset<U> generateAsset(AssetUri uri, U data) {
        AssetFactory assetFactory = factories.get(uri.getAssetType());
        if (assetFactory == null) {
            logger.warn("Unsupported asset type: {}", uri.getAssetType());
            return null;
        }
        Asset asset = assetCache.get(uri);
        if (asset != null) {
            logger.info("Reloading {} with newly generated data", uri);
            asset.reload(data);
        } else {
            asset = assetFactory.buildAsset(uri, data);
            if (asset != null && !asset.isDisposed()) {
                assetCache.put(uri, asset);
            }
        }
        return asset;
    }

    public <U extends AssetData> Asset<U> generateTemporaryAsset(AssetType type, U data) {
        return generateAsset(new AssetUri(type, "temp", UUID.randomUUID().toString()), data);
    }

    public void addAssetSource(AssetSource source) {
        assetSources.put(source.getSourceId().toLowerCase(Locale.ENGLISH), source);
        for (AssetUri asset : source.list()) {
            uriLookup.get(asset.getAssetType()).put(asset.getNormalisedAssetName(), asset.getNormalisedModuleName(), asset);
        }
    }

    public void removeAssetSource(AssetSource source) {
        assetSources.remove(source.getSourceId().toLowerCase(Locale.ENGLISH));
        for (AssetUri override : source.listOverrides()) {
            if (overrides.get(override).equals(source)) {
                overrides.remove(override);
                Asset asset = assetCache.get(override);
                if (asset != null) {
                    if (override.getModuleName().equals(TerasologyConstants.ENGINE_MODULE)) {
                        AssetData data = loadAssetData(override, true);
                        asset.reload(data);
                    } else {
                        asset.dispose();
                        assetCache.remove(override);
                    }
                }
            }
        }
        for (Table<String, String, AssetUri> table : uriLookup.values()) {
            Map<String, AssetUri> columnMap = table.column(SimpleUri.normalise(source.getSourceId()));
            for (AssetUri value : columnMap.values()) {
                Asset asset = assetCache.remove(value);
                if (asset != null) {
                    asset.dispose();
                }
            }
            columnMap.clear();
        }
    }

    public void applyOverrides() {
        overrides.clear();
        for (AssetSource assetSource : assetSources.values()) {
            for (AssetUri overrideURI : assetSource.listOverrides()) {
                overrides.put(overrideURI, assetSource);
            }
        }
    }

    public Iterable<AssetUri> listAssets() {
        return new Iterable<AssetUri>() {

            @Override
            public Iterator<AssetUri> iterator() {
                return new AllAssetIterator();
            }
        };
    }

    public Iterable<AssetUri> listAssets(final AssetType type) {
        return new Iterable<AssetUri>() {

            @Override
            public Iterator<AssetUri> iterator() {
                return new TypedAssetIterator(type);
            }
        };
    }

    public <T> Iterable<T> listLoadedAssets(final AssetType type, Class<T> assetClass) {
        List<T> results = Lists.newArrayList();
        for (Map.Entry<AssetUri, Asset> entry : assetCache.entrySet()) {
            if (entry.getKey().getAssetType() == type && assetClass.isInstance(entry.getValue())) {
                results.add(assetClass.cast(entry.getValue()));
            }
        }
        return results;
    }

    public Iterable<String> listModuleNames() {
        return assetSources.keySet();
    }

    public List<URL> getAssetURLs(AssetUri uri) {
        AssetSource overrideSource = overrides.get(uri);
        if (overrideSource != null) {
            return overrideSource.getOverride(uri);
        } else {
            AssetSource source = assetSources.get(uri.getNormalisedModuleName());
            if (source != null) {
                return source.get(uri);
            }
        }
        return Lists.newArrayList();
    }

    public InputStream getAssetStream(AssetUri uri) throws IOException {
        List<URL> assetURLs = getAssetURLs(uri);

        if (assetURLs.isEmpty()) {
            return null;
        }

        return assetURLs.get(0).openStream();
    }

    private class AllAssetIterator implements Iterator<AssetUri> {
        Iterator<AssetSource> sourceIterator;
        Iterator<AssetUri> currentUriIterator;
        AssetUri next;

        public AllAssetIterator() {
            sourceIterator = assetSources.values().iterator();
            if (sourceIterator.hasNext()) {
                currentUriIterator = sourceIterator.next().list().iterator();
            } else {
                currentUriIterator = NullIterator.newInstance();
            }
            iterate();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public AssetUri next() {
            AssetUri result = next;
            iterate();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void iterate() {
            while (!currentUriIterator.hasNext() && sourceIterator.hasNext()) {
                currentUriIterator = sourceIterator.next().list().iterator();
            }
            if (currentUriIterator.hasNext()) {
                next = currentUriIterator.next();
            } else {
                next = null;
            }
        }
    }

    private class TypedAssetIterator implements Iterator<AssetUri> {
        AssetType type;
        Iterator<AssetSource> sourceIterator;
        Iterator<AssetUri> currentUriIterator;
        AssetUri next;

        public TypedAssetIterator(AssetType type) {
            this.type = type;
            sourceIterator = assetSources.values().iterator();
            if (sourceIterator.hasNext()) {
                currentUriIterator = sourceIterator.next().list(type).iterator();
            } else {
                currentUriIterator = NullIterator.newInstance();
            }
            iterate();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public AssetUri next() {
            AssetUri result = next;
            iterate();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void iterate() {
            while (!currentUriIterator.hasNext() && sourceIterator.hasNext()) {
                currentUriIterator = sourceIterator.next().list(type).iterator();
            }
            if (currentUriIterator.hasNext()) {
                next = currentUriIterator.next();
            } else {
                next = null;
            }
        }
    }

}
