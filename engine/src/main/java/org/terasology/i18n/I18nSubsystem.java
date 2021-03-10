// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.i18n;

import org.terasology.assets.AssetFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.i18n.assets.TranslationData;
import org.terasology.engine.i18n.assets.Translation;

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
        assetTypeManager.registerCoreAssetType(Translation.class,
                (AssetFactory<Translation, TranslationData>) Translation::new, "i18n");
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
