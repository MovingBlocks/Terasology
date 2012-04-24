/*
 * Copyright 2012
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

package org.terasology.asset.sources;

import com.google.common.collect.*;
import org.terasology.asset.AssetSource;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Immortius
 */
public class ClasspathSource implements AssetSource {

    private Logger logger = Logger.getLogger(getClass().getName());
    private String sourceId;
    private Map<AssetUri, URL> assets = Maps.newHashMap();
    private Multimap<AssetType, AssetUri> assetsByType = HashMultimap.create();

    public ClasspathSource(String id, CodeSource cs, String basePath) {
        sourceId = id;

        if (cs == null) {
            throw new IllegalStateException("Can't access assets: CodeSource is null");
        }

        URL url = cs.getLocation();

        try {
            File codePath = new File(url.toURI());
            logger.info("Loading assets from " + codePath);
            this.loadAssetsFrom(codePath, basePath);
        } catch (Throwable e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }

        logger.info("Loaded " + assets.size() + " assets");
    }

    @Override
    public String getSourceId() {
        return sourceId;
    }

    @Override
    public URL get(AssetUri uri) {
         return assets.get(uri);
    }

    @Override
    public Iterable<AssetUri> list() {
        return assets.keySet();
    }

    @Override
    public Iterable<AssetUri> list(AssetType type) {
        return assetsByType.get(type);
    }

    private void loadAssetsFrom(File file, String basePath) throws IOException {
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
                AssetUri uri = getUri(key);
                if (uri != null) {
                    assets.put(uri, url);
                    assetsByType.put(uri.getAssetType(), uri);
                }
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
                AssetUri uri = getUri(key);

                if (uri != null) {
                    try {
                        assets.put(uri, child.toURI().toURL());
                        assetsByType.put(uri.getAssetType(), uri);
                    } catch (MalformedURLException e) {
                        logger.warning("Failed to load asset " + key + " - " + e.getMessage());
                    }
                }
            }
        }
    }
    private AssetUri getUri(String relativePath) {
        String[] parts = relativePath.split("/", 2);
        if (parts.length > 1) {
            AssetType assetType = AssetType.getTypeForId(parts[0]);
            if (assetType != null) {
                if (parts[1].contains(".")) {
                    parts[1] = parts[1].substring(0, parts[1].lastIndexOf('.'));
                }
                return new AssetUri(assetType, sourceId, parts[1]);
            }
        }
        return null;
    }



}
