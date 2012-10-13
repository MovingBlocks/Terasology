/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.logic.mod.ModManager;
import org.terasology.rendering.assets.Shader;
import org.terasology.rendering.assets.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssetManager {

    private static AssetManager instance = null;

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }

        return instance;
    }

    private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
    private Map<String, AssetSource> assetSources = Maps.newHashMap();
    private EnumMap<AssetType, Map<String, AssetLoader>> assetLoaders = Maps.newEnumMap(AssetType.class);
    private Map<AssetUri, Asset> assetCache = Maps.newHashMap();

    protected AssetManager() {
    }

    public void register(AssetType type, String extension, AssetLoader loader) {
        Map<String, AssetLoader> assetTypeMap = assetLoaders.get(type);
        if (assetTypeMap == null) {
            assetTypeMap = Maps.newHashMap();
            assetLoaders.put(type, assetTypeMap);
        }
        assetTypeMap.put(extension.toLowerCase(Locale.ENGLISH), loader);
    }

    public void addAssetTemporary(AssetUri uri, Asset asset) {
        Asset old = assetCache.put(uri, asset);
        if (old != null) {
            // TODO - most of our assets cause crashes when disposed at the moment
            // old.dispose();
        }
    }

    public Asset tryLoadAsset(AssetUri uri) {
        return loadAsset(uri, false);
    }


    public Asset loadAsset(AssetUri uri) {
        return loadAsset(uri, true);
    }

    private Asset loadAsset(AssetUri uri, boolean logErrors) {

        if (!uri.isValid()) return null;

        Asset asset = assetCache.get(uri);
        if (asset != null) return asset;

        List<URL> urls = getAssetURLs(uri);
        if (urls.size() == 0) {
            if (logErrors) {
                logger.log(Level.WARNING, "Unable to resolve asset: " + uri);
            }
            return null;
        }

        for (URL url : urls) {
            int extensionIndex = url.toString().lastIndexOf('.');
            if (extensionIndex == -1) continue;

            String extension = url.toString().substring(extensionIndex + 1).toLowerCase(Locale.ENGLISH);
            Map<String, AssetLoader> extensionMap = assetLoaders.get(uri.getAssetType());
            if (extensionMap == null) continue;

            AssetLoader loader = extensionMap.get(extension);
            if (loader == null) continue;

            InputStream stream = null;
            try {
                stream = url.openStream();
                urls.remove(url);
                urls.add(0, url);
                asset = loader.load(stream, uri, urls);
                if (asset != null) {
                    assetCache.put(uri, asset);
                }
                logger.log(Level.INFO, "Loaded " + uri);
                return asset;
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Error reading asset " + uri, ioe);
                return null;
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException innerException) {
                        logger.log(Level.SEVERE, "Error closing stream for " + uri, innerException);
                    }
                }
            }
        }
        logger.log(Level.WARNING, "Unable to resolve asset: " + uri);
        return null;
    }

    public void clear() {
        Iterator<Asset> iterator = assetCache.values().iterator();
        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            // Don't dispose engine assets, all sorts of systems have references to them
            if (!asset.getURI().getPackage().equals(ModManager.ENGINE_PACKAGE)) {
                asset.dispose();
                iterator.remove();
            }
        }
    }

    public void addAssetSource(AssetSource source) {
        assetSources.put(source.getSourceId().toLowerCase(Locale.ENGLISH), source);
    }

    public void removeAssetSource(AssetSource source) {
        assetSources.remove(source.getSourceId().toLowerCase(Locale.ENGLISH));
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

    public Iterable<String> listPackageNames() {
        return assetSources.keySet();
    }

    public List<URL> getAssetURLs(AssetUri uri) {
        AssetSource source = assetSources.get(uri.getPackage());
        if (source != null) {
            return source.get(uri);
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

    // Static syntax sugar
    public static InputStream assetStream(AssetUri uri) throws IOException {
        return getInstance().getAssetStream(uri);
    }

    public static Iterable<AssetUri> list() {
        return getInstance().listAssets();
    }

    public static Iterable<AssetUri> list(AssetType type) {
        return getInstance().listAssets(type);
    }

    public static Iterable<String> listPackages() {
        return getInstance().listPackageNames();
    }

    public static Asset load(AssetUri uri) {
        return getInstance().loadAsset(uri);
    }

    public static Asset tryLoad(AssetUri uri) {
        return getInstance().tryLoadAsset(uri);
    }

    public static <T extends Asset> T load(AssetUri uri, Class<T> assetClass) {
        Asset result = load(uri);
        if (result != null && assetClass.isAssignableFrom(result.getClass())) {
            return assetClass.cast(result);
        }
        return null;
    }

    // Some ease-of-use helper methods

    public static Texture loadTexture(String simpleUri) {
        return load(new AssetUri(AssetType.TEXTURE, simpleUri), Texture.class);
    }

    public static Shader loadShader(String simpleUri) {
        return load(new AssetUri(AssetType.SHADER, simpleUri), Shader.class);
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
