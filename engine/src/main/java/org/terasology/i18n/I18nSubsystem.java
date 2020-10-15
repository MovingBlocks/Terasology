// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.i18n;

import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.i18n.assets.Translation;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 * Registers internationalization systems.
 */
public class I18nSubsystem implements EngineSubsystem {

    @In
    ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Internationalization";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.registerCoreAssetType(Translation.class,
                Translation::new, "i18n");
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        classFactory.createToContext(TranslationSystemImpl.class, TranslationSystem.class);
    }

    @Override
    public void postInitialise(Context context) {
        context.get(TranslationSystem.class).refresh();
    }
}
