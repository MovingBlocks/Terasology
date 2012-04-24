package org.terasology.logic.manager;

import com.google.common.collect.Maps;
import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.common.NullIterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarFile;
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

    protected AssetManager() {
    }

    public void addAssetSource(AssetSource source) {
        assetSources.put(source.getSourceId(), source);
    }

    public void removeAssetSource(AssetSource source) {
        assetSources.remove(source.getSourceId());
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
