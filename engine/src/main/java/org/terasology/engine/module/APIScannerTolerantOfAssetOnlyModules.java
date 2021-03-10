// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.terasology.module.Module;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.sandbox.APIScanner;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;

import static org.terasology.engine.core.module.StandardModuleExtension.IS_ASSET;

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
    APIScannerTolerantOfAssetOnlyModules(StandardPermissionProviderFactory permissionProviderFactory) {
        super(permissionProviderFactory);
    }

    @Override
    public void scan(ModuleRegistry registry) {
        for (Module module : registry) {
            if (module.isOnClasspath() && !IS_ASSET.isProvidedBy(module)) {
                scan(module);
            }
        }
    }
}
