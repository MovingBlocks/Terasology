/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.tcn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.module.Module;
import org.terasology.module.sandbox.API;
import org.terasology.registry.In;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.md5.MD5SkeletonLoader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author synopia
 */
@API
public class TCNSkeletalMeshLoader extends MD5SkeletonLoader {
    @In
    private AssetManager assetManager;
    private static final Logger logger = LoggerFactory.getLogger(TCNSkeletalMeshLoader.class);
    private TCN tcn;


    @Override
    public SkeletalMeshData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        logger.info("Loading skeletal mesh for " + urls);
        ZipInputStream zipStream = new ZipInputStream(stream);

        try {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zipStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }

                byte[] bytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                if ("model.xml".equals(entry.getName())) {
                    try {
                        tcn = TCN.parse(bais);
                        return createMesh(tcn);
                    } catch (TCN.TCNParseException e) {
                        logger.error("Unable to parse model.xml from " + urls);
                    }
                } else if (entry.getName().endsWith("png")) {
                    // todo would be nice to use the png inside the zip
//                    PNGTextureLoader loader = new PNGTextureLoader();
//                    TextureData textureData = loader.load(module, bais, Arrays.asList(new URL(urls.get(0).toExternalForm() + "$" + entry.getName())), deltas);
//                    assetManager.
                }
            }
        } finally {
            // we must always close the zip file.
            zipStream.close();
        }
        return null;
    }

    public SkeletalMeshData createMesh(TCN tcn) {
        MD5 md5 = tcn.getMD5();
        return buildMeshData(md5);
    }

    public static void main(String[] args) throws IOException, TCN.TCNParseException {
        File tcnFile = new File(args[0]);

        TCNSkeletalMeshLoader loader = new TCNSkeletalMeshLoader();
        loader.load(null, new BufferedInputStream(new FileInputStream(tcnFile)), Arrays.asList(tcnFile.toURL()), Arrays.asList(tcnFile.toURL()));

        TCN tcn = loader.tcn;
        int ext = args[0].lastIndexOf('.');
        FileOutputStream fos = new FileOutputStream(args[0].substring(0, ext) + ".md5mesh");
        System.setOut(new PrintStream(fos));
        System.out.println(tcn.getMD5());
    }

}
