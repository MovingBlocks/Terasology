/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.hud;

import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIIconBar;

public class HealthHud extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        UIIconBar healthBar = find("healthBar", UIIconBar.class);
        healthBar.bindValue(new ReadOnlyBinding<Float>() {
            @Override
            public Float get() {
                HealthComponent healthComponent = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
                if (healthComponent != null) {
                    return (float) healthComponent.currentHealth;
                }
                return 0f;
            }
        });
        healthBar.bindMaxValue(new ReadOnlyBinding<Float>() {
            @Override
            public Float get() {
                HealthComponent healthComponent = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
                if (healthComponent != null) {
                    return (float) healthComponent.maxHealth;
                }
                return 0f;
            }
        });
    }
}
