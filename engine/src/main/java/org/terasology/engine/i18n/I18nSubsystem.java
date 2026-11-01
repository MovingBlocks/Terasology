// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.terasology.context.Lifetime;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.i18n.assets.Translation;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

/**
 * Registers internationalization systems.
 */
public class I18nSubsystem implements EngineSubsystem {
    @Inject
    protected SystemConfig systemConfig;
    @Inject
    protected AssetManager assetManager;

    @Inject
    public I18nSubsystem() {
    }

    @Override
    public String getName() {
        return "Internationalization";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.createAssetType(Translation.class, Translation::create, "i18n");
    }

    @Override
    public void preInitialise(ServiceRegistry serviceRegistry) {
        serviceRegistry.with(TranslationSystem.class).lifetime(Lifetime.Singleton).use(TranslationSystemImpl.class);
    }

    @Override
    public void postInitialise(Context context) {
        context.get(TranslationSystem.class).refresh();
    }
}
