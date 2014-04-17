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

package org.terasology.asset.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Immortius
 */
public class ArchiveSource extends AbstractSource {

    private final Logger logger = LoggerFactory.getLogger(ArchiveSource.class);

    public ArchiveSource(String sourceId, File archive, String assetsPath, String overridesPath, String deltasPath) {
        super(sourceId);

        try {
            scanArchiveForAssets(archive, assetsPath);
            scanArchiveForOverrides(archive, overridesPath);
            scanArchiveForDeltas(archive, deltasPath);
        } catch (IOException e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }
    }

    private void scanArchiveForOverrides(File file, String overridesPath) throws IOException {
        ZipFile archive;

        if (file.getName().endsWith(".jar")) {
            archive = new JarFile(file, false);
        } else {
            archive = new ZipFile(file);
        }

        try {
            Enumeration<? extends ZipEntry> lister = archive.entries();

            while (lister.hasMoreElements()) {
                ZipEntry entry = lister.nextElement();
                String entryPath = entry.getName();
                logger.debug("Found {}", entryPath);

                if (entryPath.startsWith(overridesPath)) {
                    String key = entryPath.substring(overridesPath.length() + 1);
                    int moduleIndex = key.indexOf('/');
                    if (moduleIndex == -1) {
                        continue;
                    }
                    String moduleName = key.substring(0, moduleIndex);
                    key = key.substring(moduleIndex + 1);

                    AssetUri uri = getUri(moduleName, key);
                    if (uri == null || !uri.isValid()) {
                        continue;
                    }

                    logger.debug("Discovered override {} at {}", uri, entryPath);

                    // Using a jar protocol for zip files, because cannot register new protocols for the applet
                    URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/" + entryPath);
                    addOverride(uri, url);
                }
            }
        } finally {
            archive.close();
        }
    }

    private void scanArchiveForDeltas(File file, String deltaPath) throws IOException {
        ZipFile archive;

        if (file.getName().endsWith(".jar")) {
            archive = new JarFile(file, false);
        } else {
            archive = new ZipFile(file);
        }

        try {
            Enumeration<? extends ZipEntry> lister = archive.entries();

            while (lister.hasMoreElements()) {
                ZipEntry entry = lister.nextElement();
                String entryPath = entry.getName();
                logger.debug("Found {}", entryPath);

                if (entryPath.startsWith(deltaPath)) {
                    String key = entryPath.substring(deltaPath.length() + 1);
                    int moduleIndex = key.indexOf('/');
                    if (moduleIndex == -1) {
                        continue;
                    }
                    String moduleName = key.substring(0, moduleIndex);
                    key = key.substring(moduleIndex + 1);

                    AssetUri uri = getUri(moduleName, key);
                    if (uri == null || !uri.isValid()) {
                        continue;
                    }

                    logger.debug("Discovered delta {} at {}", uri, entryPath);

                    // Using a jar protocol for zip files, because cannot register new protocols for the applet
                    URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/" + entryPath);
                    setDelta(uri, url);
                }
            }
        } finally {
            archive.close();
        }
    }

    protected void scanArchiveForAssets(File file, String assetsPath) throws IOException {
        ZipFile archive;

        if (file.getName().endsWith(".jar")) {
            archive = new JarFile(file, false);
        } else {
            archive = new ZipFile(file);
        }

        try {
            Enumeration<? extends ZipEntry> lister = archive.entries();

            while (lister.hasMoreElements()) {
                ZipEntry entry = lister.nextElement();
                String entryPath = entry.getName();
                logger.debug("Found {}", entryPath);

                if (entryPath.startsWith(assetsPath)) {
                    String key = entryPath.substring(assetsPath.length() + 1);
                    AssetUri uri = getUri(key);
                    if (uri == null || !uri.isValid()) {
                        continue;
                    }

                    logger.debug("Discovered resource {}", uri);

                    // Using a jar protocol for zip files, because cannot register new protocols for the applet
                    URL url = new URL("jar:file:" + file.getAbsolutePath() + "!/" + entryPath);
                    addItem(uri, url);
                }
            }
        } finally {
            archive.close();
        }
    }

}
