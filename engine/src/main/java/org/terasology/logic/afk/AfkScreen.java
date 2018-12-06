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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIBox;

public class AfkScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private UIBox uiBox;

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
    }

    @Override
    public void onClosed() {
        if (getAfkClientSystem() != null) {
            afkClientSystem.onAfkScreenClosed();
        }
    }

    public void setAfkClientSystem(AfkClientSystem afkClientSystem) {
        this.afkClientSystem = afkClientSystem;
    }

    public AfkClientSystem getAfkClientSystem() {
        return afkClientSystem;
    }
}
