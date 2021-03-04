// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.module;

import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.Module;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.sandbox.APIScanner;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;

import static org.terasology.engine.module.StandardModuleExtension.IS_ASSET;

/**
 * APIScanner that doesn't crash on asset-only modules.
 * <p>
 * The APIScanner in gestalt-module v5 crashes when it encounters a module that does have a
 * <code>reflections.cache</code> but its <code>TypeAnnotationsScanner</code> element is empty
 * on account of there being no classes in there.
 * <p>
 * {@inheritDoc}
 */
class APIScannerTolerantOfAssetOnlyModules extends APIScanner {
    private static final Logger logger = LoggerFactory.getLogger(APIScannerTolerantOfAssetOnlyModules.class);

    APIScannerTolerantOfAssetOnlyModules(StandardPermissionProviderFactory permissionProviderFactory) {
        super(permissionProviderFactory);
    }

    @Override
    public void scan(ModuleRegistry registry) {
        for (Module module : registry) {
            if (module.isOnClasspath() && !IS_ASSET.isProvidedBy(module)) {
                try {
                    scan(module);
                } catch (ReflectionsException e) {
                    // {@link org.terasology.cities} is an example of a module that fails here.
                    // It is not asset-only, but it also doesn't have any of its classes annotated,
                    // and it gets an error about TypeAnnotationScanner configuration here.
                    logger.warn("Scanning module {} failed:", module, e);
                }
            }
        }
    }
}
