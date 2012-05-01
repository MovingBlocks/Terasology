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

    public ArchiveSource(String sourceId, File archive) {
        super(sourceId);

        try {
            scanArchive(archive);
        } catch (IOException e) {
            throw new IllegalStateException("Error loading assets: " + e.getMessage(), e);
        }
    }

    protected void scanArchive(File file) throws IOException {
        ZipFile archive;
        String archiveType = "zip";
        String basePath = "";

        if (file.getName().endsWith(".jar")) {
            archive = new JarFile(file, false);
            archiveType = "jar";
            basePath = "org/terasology/data/";
        } else {
            archive = new ZipFile(file);
        }

        Enumeration<? extends ZipEntry> lister = archive.entries();

        while (lister.hasMoreElements()) {
            ZipEntry entry = lister.nextElement();
            String entryPath = entry.getName();

            if (entryPath.startsWith(basePath)) {
                String key = entryPath.substring(basePath.length());

                // @todo avoid this risky approach
                URL url = new URL(archiveType + ":file:" + file.getAbsolutePath() + "!/" + entryPath );
                AssetUri uri = getUri(key);
                if (uri != null) {
                    addItem(uri, url);
                }
            }
        }
    }
}
