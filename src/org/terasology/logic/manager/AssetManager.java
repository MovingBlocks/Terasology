package org.terasology.logic.manager;

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
    private final static String ASSETS_BASE_PATH = "org/terasology/data";

    private Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

    private static AssetManager _instance = null;

    protected Map<String, URL> assets = new HashMap<String, URL>();

    public static AssetManager getInstance() {
        if (_instance == null) {
            _instance = new AssetManager();
        }

        return _instance;
    }

    protected AssetManager() {
        this.initialize();
    }

    private void initialize() {
        logger.info("Loading assets...");
        CodeSource cs = this.getClass().getProtectionDomain().getCodeSource();

        if (cs == null) {
            throw new IllegalStateException("Can't access assets: CodeSource is null");
        }

        URL url = cs.getLocation();

        try {
            File codePath = new File(url.toURI());
            logger.info("Loading assets from " + codePath);
            this.loadAssetsFrom(codePath, ASSETS_BASE_PATH);
        } catch (Throwable e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }

        logger.info("Loaded " + assets.size() + " assets");
    }
    
    public Set<String> listAssets() {
        return listAssets(null);
    }

    public Set<String> listAssets(String key) {
        // @todo make more optimal all assets listing (just return assets.keySet()?)
        Set<String> result = new LinkedHashSet<String>();

        for (String asset : this.assets.keySet()) {

            if (key == null || asset.startsWith(key)) {
                result.add(asset);
            }
        }
        
        return result;
    }
    
    public void addAsset(String name, URL url) {
        this.assets.put(name, url);
    }
    
    public URL getAsset(String name) {
        return this.assets.get(name);
    }
    
    public InputStream getAssetStream(String name) throws IOException {
        URL assetURL = this.getAsset(name);

        if (assetURL == null) {
            return null;
        }
        
        return assetURL.openStream();
    }

    // Static syntax sugar
    public static InputStream assetStream(String name) throws IOException {
        return getInstance().getAssetStream(name);
    }
    
    public static URL asset(String name) {
        return getInstance().getAsset(name);
    }
    
    public static Set<String> list() {
        return getInstance().listAssets();
    }
    
    public static Set<String> list(String key) {
        return getInstance().listAssets(key);
    }

    public void loadAssetsFrom(File file, String basePath) throws IOException {
        try {
            if (file.isFile()) { // assets stored in archive
                this.scanArchive(file, basePath);
            } else if (file.isDirectory()) { // unpacked
                File dataDirectory = new File(file, basePath);
                scanFiles(dataDirectory, dataDirectory.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // just rethrow as runtime exception
        }
    }

    private void scanArchive(File file, String basePath) throws IOException {
        ZipFile archive;
        String archiveType = "zip";
        
        if (file.getName().endsWith(".jar")) {
            archive = new JarFile(file, false);
            archiveType = "jar";
        } else {
            archive = new ZipFile(file);
        }
        
        
        Enumeration<? extends ZipEntry> lister = archive.entries();

        while (lister.hasMoreElements()) {
            ZipEntry entry = lister.nextElement();
            String entryPath = entry.getName();

            if (entryPath.startsWith(basePath)) {
                String key = (basePath != null) ? entryPath.substring(basePath.length() + 1) : entryPath;

                // @todo avoid this risky approach
                URL url = new URL(archiveType + ":file:" + file.getAbsolutePath() + "!/" + entryPath );

                this.addAsset(key, url);
            }
        }
    }

    private void scanFiles(File file, String basePath) {
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                this.scanFiles(child, basePath);
            } else if (child.isFile()) {
                String key = child.getAbsolutePath().replace(File.separatorChar, '/');

                if(basePath != null) { //strip down basepath
                    key = key.substring(basePath.length() + 1);
                }

                try {
                    this.addAsset(key, child.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warning("Failed to load asset " + key + " - " + e.getMessage());
                }
            }
        }
    }
}
