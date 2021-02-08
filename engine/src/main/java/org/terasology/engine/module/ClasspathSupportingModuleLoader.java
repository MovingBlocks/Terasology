// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.module;

import org.terasology.module.ClasspathModule;
import org.terasology.module.Module;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.cert.Certificate;

public class ClasspathSupportingModuleLoader extends ModuleLoader {
    boolean makeClasspathModules;
    private final ModuleMetadataJsonAdapter metadataReader;

    public ClasspathSupportingModuleLoader(ModuleMetadataJsonAdapter metadataReader) {
        super(metadataReader);
        this.metadataReader = metadataReader;
    }

    public ClasspathSupportingModuleLoader(ModuleMetadataJsonAdapter metadataReader, boolean makeClasspathModules) {
        super(metadataReader);
        this.metadataReader = metadataReader;
        this.makeClasspathModules = makeClasspathModules;
    }

    @Override
    public Module load(Path modulePath) throws IOException {
        Module module;
        if (makeClasspathModules) {
            URL url = urlFromPath(modulePath);
            ModuleMetadata metadata = readMetadata(url);
            // TODO: basic metadata checks
            CodeSource codeSource = new CodeSource(url, (Certificate[]) null);
            try {
                module = ClasspathModule.create(metadata, false, codeSource);
            } catch (URISyntaxException e) {
                throw new RuntimeException("URI syntax problems for: " + modulePath.toString(), e);
            }
        } else {
            module = super.load(modulePath);
        }
        return module;
    }

    static URL urlFromPath(Path path) throws MalformedURLException {
        File baseFile = path.toFile();
        URL url;
        if (baseFile.isDirectory()) {
            url = path.toUri().toURL();
        } else {
            url = new URL("jar", null, path.toString());
        }
        return url;
    }
    
    ModuleMetadata readMetadata(URL moduleUrl) throws IOException {
        URL metadataUrl = new URL(moduleUrl, getModuleInfoPath().toString());
        try (InputStreamReader readme = new InputStreamReader(metadataUrl.openStream())) {
            return metadataReader.read(readme);
        }
    }
}
