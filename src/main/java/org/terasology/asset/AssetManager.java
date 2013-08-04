/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.internal.PojoPrefab;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.utilities.collection.NullIterator;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.shapes.BlockShapeData;
import org.terasology.world.block.shapes.BlockShapeImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// TODO: Split out static methods to an Assets static class
// TODO: Split out an interface, possibly two with one for loading and adding assets, the other with disposal and other more management methods
// TODO: No more AssetManager singleton
public class AssetManager {

    private static final Logger logger = LoggerFactory.getLogger(AssetManager.class);
    private static AssetManager instance = null;

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
            instance.setAssetFactory(AssetType.PREFAB, new AssetFactory<PrefabData, Prefab>() {

                @Override
                public Prefab buildAsset(AssetUri uri, PrefabData data) {
                    return new PojoPrefab(uri, data);
                }
            });
            instance.setAssetFactory(AssetType.SHAPE, new AssetFactory<BlockShapeData, BlockShape>() {

                @Override
                public BlockShape buildAsset(AssetUri uri, BlockShapeData data) {
                    return new BlockShapeImpl(uri, data);
                }
            });
        }

        return instance;
    }

    private Map<String, AssetSource> assetSources = Maps.newHashMap();
    private EnumMap<AssetType, Map<String, AssetLoader>> assetLoaders = Maps.newEnumMap(AssetType.class);
    private Map<AssetUri, Asset> assetCache = Maps.newHashMap();
    private Map<AssetUri, AssetSource> overrides = Maps.newHashMap();
    private Map<AssetType, AssetFactory> factories = Maps.newHashMap();

    protected AssetManager() {
    }

    public void setAssetFactory(AssetType type, AssetFactory factory) {
        factories.put(type, factory);
    }

    // Static syntax sugar
    public static InputStream assetStream(AssetUri uri) throws IOException {
        return getInstance().getAssetStream(uri);
    }

    public static Asset tryLoad(AssetUri uri) {
        return getInstance().tryLoadAsset(uri);
    }

    public static <T> T tryLoadAssetData(AssetUri uri, Class<T> type) {
        AssetData data = getInstance().loadAssetData(uri, false);
        if (type.isInstance(data)) {
            return type.cast(data);
        }
        return null;
    }

    public static <T> T loadAssetData(AssetUri uri, Class<T> type) {
        AssetData data = getInstance().loadAssetData(uri, true);
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

    // TODO: Remove (we have generateAsset now)?
    public void addAssetTemporary(AssetUri uri, Asset asset) {
        assetCache.put(uri, asset);
        // TODO - most of our assets cause crashes when disposed at the moment
        /* if (old != null) {
            // old.dispose();
        } */
    }

    public <T extends Asset> T tryLoadAsset(AssetUri uri, Class<T> type) {
        Asset result = loadAsset(uri, false);
        if (type.isInstance(result)) {
            return type.cast(result);
        }
        return null;
    }

    public Asset tryLoadAsset(AssetUri uri) {
        return loadAsset(uri, false);
    }

    public Asset loadAsset(AssetUri uri) {
        return loadAsset(uri, true);
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

            InputStream stream = null;
            try {
                stream = url.openStream();
                urls.remove(url);
                urls.add(0, url);
                return loader.load(uri, stream, urls);
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

        AssetData data = loadAssetData(uri, logErrors);

        if (data != null) {
            asset = factory.buildAsset(uri, data);
            if (asset != null) {
                logger.debug("Loaded {}", uri);
                assetCache.put(uri, asset);
            }
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
//            if (!asset.getURI().getModuleName().equals(ModManager.ENGINE_PACKAGE)) {
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
    }

    public void removeAssetSource(AssetSource source) {
        assetSources.remove(source.getSourceId().toLowerCase(Locale.ENGLISH));
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
        AssetUri next = null;

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
        AssetUri next = null;

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
