// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.ClasspathModule;
import org.terasology.module.Module;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.cert.Certificate;


public class ClasspathSupportingModuleLoader extends ModuleLoader {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathSupportingModuleLoader.class);

    boolean makeClasspathModules;
    boolean lenient;
    private final ModuleMetadataJsonAdapter metadataReader;

    public ClasspathSupportingModuleLoader(boolean makeClasspathModules, boolean lenient) {
        this(new ModuleMetadataJsonAdapter(), makeClasspathModules, lenient);
    }

    public ClasspathSupportingModuleLoader(
            ModuleMetadataJsonAdapter metadataReader,
            boolean makeClasspathModules,
            boolean lenient) {
        super(metadataReader);
        this.metadataReader = metadataReader;
        this.makeClasspathModules = makeClasspathModules;
        this.lenient = lenient;
    }

    @Override
    public Module load(Path modulePath) throws IOException {
        if (makeClasspathModules) {
            return loadFromClasspath(modulePath);
        } else {
            return super.load(modulePath);
        }
    }

    private Module loadFromClasspath(Path modulePath) throws IOException {
        Module module;
        URL url = modulePath.toUri().toURL();
        ModuleMetadata metadata = readMetadata(url);
        if (metadata == null) {
            return null;  // no metadata file at all, ModuleLoader skips without comment
        } else if (null == metadata.getId() || metadata.getDisplayName().toString().isEmpty()) {
            logger.warn("Found an archive with {} but missing id or display name. Skipping {}",
                    getModuleInfoPath(), url);
            return null;
        }

        CodeSource codeSource = new CodeSource(url, (Certificate[]) null);
        try {
            logger.debug("Creating module for {}", codeSource);
            module = ClasspathModule.create(metadata, false, codeSource);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax problems for: " + modulePath.toString(), e);
        }
        return module;
    }

    ModuleMetadata readMetadata(URL moduleUrl) throws IOException {
        URL metadataUrl;
        ModuleMetadata metadata = null;
        try {
            if (Paths.get(moduleUrl.toURI()).toFile().isDirectory()) {
                metadataUrl = new URL(moduleUrl, getModuleInfoPath().toString());
            } else {
                metadataUrl = new URL("jar", null, moduleUrl + "!/" + getModuleInfoPath());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException("Error making metadata URL for " + moduleUrl.toString(), e);
        }

        try (InputStreamReader readme = new InputStreamReader(metadataUrl.openStream())) {
            metadata = metadataReader.read(readme);
        } catch (java.io.FileNotFoundException e) {
            if (!lenient) {
                throw e;
            }
        }
        if (metadata == null && !lenient) {
            throw new RuntimeException("Empty metadata from " + moduleUrl.toString());
        }
        return metadata;
    }
}
