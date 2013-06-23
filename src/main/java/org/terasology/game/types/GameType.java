/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.game.types;

import org.terasology.components.HealthComponent;
import org.terasology.config.ModConfig;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * A game type.
 */
public interface GameType {
    String name();
    GameTypeUri uri();

    /**
     * Is called when world is initialized
     */
    void initialize();

    /**
     * @return mod configuration to auto select or null if default
     */
    ModConfig defaultModConfig();

    MapGeneratorUri defaultMapGenerator();

    void onCreateInventoryHook(UIWindow parent);
    void onPlayerDamageHook(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator);
}
