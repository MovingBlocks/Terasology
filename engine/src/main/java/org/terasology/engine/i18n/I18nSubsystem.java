// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.i18n.assets.Translation;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;

/**
 * Registers internationalization systems.
 */
public class I18nSubsystem implements EngineSubsystem {

    @Override
    public String getName() {
        return "Internationalization";
    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {
        assetTypeManager.createAssetType(Translation.class, Translation::create, "i18n");
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(TranslationSystem.class, new TranslationSystemImpl(rootContext));
    }

    @Override
    public void postInitialise(Context context) {
        context.get(TranslationSystem.class).refresh();
    }
}
