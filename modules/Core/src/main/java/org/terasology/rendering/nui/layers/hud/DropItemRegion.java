/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.hud;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.MouseInput;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;

/**
 * A region/layer around the inventory grid to allow players to get rid of extra items
 * and have free inventory slots by dropping them onto this layer.
 */
public class DropItemRegion extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            MouseInput mouseButton = event.getMouseButton();
            if (mouseButton == MouseInput.MOUSE_LEFT || mouseButton == MouseInput.MOUSE_RIGHT) {
                EntityRef playerEntity = localPlayer.getCharacterEntity();
                EntityRef movingItem = playerEntity.getComponent(CharacterComponent.class).movingItem;
                EntityRef item  = InventoryUtils.getItemAt(movingItem, 0);
                if (!item.exists()) {
                    return true;
                }
                int count = 1;
                if (mouseButton == MouseInput.MOUSE_LEFT) {
                    count = InventoryUtils.getStackCount(item);     //Drop complete stack with left click
                }

                Vector3f position = localPlayer.getViewPosition();
                Vector3f direction = localPlayer.getViewDirection();
                Vector3f newPosition = new Vector3f(position.x + direction.x * 1.5f,
                        position.y + direction.y * 1.5f,
                        position.z + direction.z * 1.5f
                );
                //send DropItemRequest
                Vector3f impulseVector = new Vector3f(direction);
                playerEntity.send(new DropItemRequest(item, playerEntity,
                        impulseVector,
                        newPosition,
                        count));
                return true;
            }
            return false;
        }
    };

    @Override
    public void initialise() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.addInteractionRegion(interactionListener);
    }
}
