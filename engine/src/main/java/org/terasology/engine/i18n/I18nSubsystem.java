/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.i18n;

import org.terasology.assets.AssetFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.i18n.assets.Translation;
import org.terasology.i18n.assets.TranslationData;

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
