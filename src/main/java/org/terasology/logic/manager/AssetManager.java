package org.terasology.logic.manager;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.asset.*;
import org.terasology.entitySystem.common.NullIterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AssetManager {

    private static AssetManager _instance = null;

    public static AssetManager getInstance() {
        if (_instance == null) {
            _instance = new AssetManager();
        }

        return _instance;
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

    public Asset loadAsset(AssetUri uri) {
        if (!uri.isValid()) return null;

        Asset asset = assetCache.get(uri);
        if (asset != null) return asset;

        URL url = getAsset(uri);
        if (url == null) {
            logger.log(Level.WARNING, "Unable to resolve asset: " + uri);
            return null;
        }

        int extensionIndex = url.toString().lastIndexOf('.');
        if (extensionIndex == -1) return null;

        String extension = url.toString().substring(extensionIndex + 1).toLowerCase(Locale.ENGLISH);
        Map<String, AssetLoader> extensionMap = assetLoaders.get(uri.getAssetType());
        if (extensionMap == null) return null;

        AssetLoader loader = extensionMap.get(extension);
        if (loader == null) return null;

        InputStream stream = null;
        try {
            stream = url.openStream();
            asset = loader.load(stream, uri, url);
            if (asset != null) {
                assetCache.put(uri, asset);
            }
            return asset;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error reading asset " + uri, ioe);
            return null;
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException innerException) {
                    logger.log(Level.SEVERE, "Error closing stream for " + uri, innerException);
                }
            }
        }
    }

    public void clear() {
        // TODO: Unload assets
        assetCache.clear();
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

    public URL getAsset(AssetUri uri) {
        AssetSource source = assetSources.get(uri.getPackage());
        if (source != null) {
            return source.get(uri);
        }
        return null;
    }
    
    public InputStream getAssetStream(AssetUri uri) throws IOException {
        URL assetURL = this.getAsset(uri);

        if (assetURL == null) {
            return null;
        }
        
        return assetURL.openStream();
    }

    // Static syntax sugar
    public static InputStream assetStream(AssetUri uri) throws IOException {
        return getInstance().getAssetStream(uri);
    }
    
    public static URL asset(AssetUri uri) {
        return getInstance().getAsset(uri);
    }
    
    public static Iterable<AssetUri> list() {
        return getInstance().listAssets();
    }
    
    public static Iterable<AssetUri> list(AssetType type) {
        return getInstance().listAssets(type);
    }

    public static Asset load(AssetUri uri) {
        return getInstance().loadAsset(uri);
    }

    public static <T extends Asset> T load(AssetUri uri, Class<T> assetClass) {
        Asset result = load(uri);
        if (result != null && assetClass.isAssignableFrom(result.getClass())) {
            return assetClass.cast(result);
        }
        return null;
    }

    private class AllAssetIterator implements Iterator<AssetUri> {
        Iterator<AssetSource> sourceIterator;
        Iterator<AssetUri> currentUriIterator;
        AssetUri next = null;

        public AllAssetIterator() {
            sourceIterator = assetSources.values().iterator();
            if (sourceIterator.hasNext()) {
                currentUriIterator = sourceIterator.next().list().iterator();
            }
            else {
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
            }
            else {
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
