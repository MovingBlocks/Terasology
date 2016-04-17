/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.characters;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.ChangeHeldItemRequest;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class CharacterHeldItemAuthoritySystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onChangeHeldItemRequest(ChangeHeldItemRequest event, EntityRef character,
                                        CharacterHeldItemComponent characterHeldItemComponent) {
        EntityRef oldItem = characterHeldItemComponent.selectedItem;
        characterHeldItemComponent.selectedItem = event.getItem();
        character.saveComponent(characterHeldItemComponent);
        updateLightFromItem(character, oldItem, event.getItem());
    }

    @ReceiveEvent
    public void onBlockItemDestroyedRemoveLight(BeforeDeactivateComponent event, EntityRef item, BlockItemComponent blockItemComponent) {
        if (blockItemComponent.blockFamily == null || blockItemComponent.blockFamily.getArchetypeBlock().getLuminance() == 0) {
            return;
        }

        CharacterHeldItemComponent characterHeldItemComponent = item.getOwner().getComponent(CharacterHeldItemComponent.class);
        if (characterHeldItemComponent != null) {
            if (item == characterHeldItemComponent.selectedItem) {
                item.getOwner().removeComponent(LightComponent.class);
            }
        }
    }

    /**
     * This will allow held items to still look lit for the viewer.  A better method of illuminating held items should be devised.
     *
     * @param entity
     * @param oldItem
     * @param newItem
     */
    private void updateLightFromItem(EntityRef entity, EntityRef oldItem, EntityRef newItem) {
        byte oldLuminance = getLuminance(oldItem);
        byte newLuminance = getLuminance(newItem);
        LightComponent newItemLight = newItem.getComponent(LightComponent.class);
        if (newLuminance == 0 || newItemLight == null) {
            entity.removeComponent(LightComponent.class);
        } else {
            LightComponent light = entity.getComponent(LightComponent.class);
            if (light == null) {
                // let the color of this light mirror the color of the held light
                light = new LightComponent();
                light.lightColorAmbient = newItemLight.lightColorAmbient;
                light.lightColorDiffuse = newItemLight.lightColorDiffuse;
                light.lightDiffuseIntensity = newItemLight.lightDiffuseIntensity;
                light.lightAmbientIntensity = newItemLight.lightAmbientIntensity;
                light.lightAttenuationRange = 1f;
                light.lightAttenuationFalloff = 0.1f;
                entity.addOrSaveComponent(light);
            }
        }
    }

    private byte getLuminance(EntityRef item) {
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            return blockItem.blockFamily.getArchetypeBlock().getLuminance();
        }
        return 0;
    }
}
