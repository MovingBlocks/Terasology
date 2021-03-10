// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.i18n;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;

/**
 * This system refreshes the translation system during the initialization, reloading the i18n files from the module environment.
 * The translation system by it's own is not refreshed, as it is an EngineSubsystem.
 */
@RegisterSystem
public class TranslationRefreshSystem extends BaseComponentSystem {

    @In
    private TranslationSystem translationSystem;

    @Override
    public void initialise() {
        //TODO See https://github.com/MovingBlocks/Terasology/issues/2433 for further translation support.
        translationSystem.refresh();
    }
}
