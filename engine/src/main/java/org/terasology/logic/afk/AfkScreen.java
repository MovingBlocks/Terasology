/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.afk;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.widgets.UIBox;
import org.terasology.nui.widgets.UILabel;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;

public class AfkScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    private UIBox uiBox;
    private UILabel uiText;

    private AfkClientSystem afkClientSystem;

    @Override
    public void initialise() {
        uiBox = find("box", UIBox.class);
        if (uiBox != null) {
            uiBox.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    EntityRef entity = localPlayer.getClientEntity();
                    AfkComponent component = entity.getComponent(AfkComponent.class);
                    if (component != null) {
                        return component.afk;
                    }
                    return false;
                }
            });
        }
        uiText = find("text", UILabel.class);
        if (uiText != null) {
            uiText.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    long afkTime = time.getGameTimeInMs() - afkClientSystem.getLastActive();
                    return String.format("( Press %s to disable the AFK mode )",
                            afkTime <= AfkClientSystem.AFK_FREEDOM ? "ESCAPE" : "anything");
                }
            });
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.getKey() == Keyboard.Key.ESCAPE) {
            if (getAfkClientSystem() != null) {
                afkClientSystem.onAfkScreenClosed();
            }
            return true;
        }
        return super.onKeyEvent(event);
    }

    public void setAfkClientSystem(AfkClientSystem afkClientSystem) {
        this.afkClientSystem = afkClientSystem;
    }

    public AfkClientSystem getAfkClientSystem() {
        return afkClientSystem;
    }
}
