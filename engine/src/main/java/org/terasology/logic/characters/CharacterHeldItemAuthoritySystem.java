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
import org.terasology.rendering.logic.LightFadeComponent;
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

    private void updateLightFromItem(EntityRef entity, EntityRef oldItem, EntityRef newItem) {
        byte oldLuminance = getLuminance(oldItem);
        byte newLuminance = getLuminance(newItem);
        if (oldLuminance != newLuminance) {
            if (newLuminance == 0) {
                // Fade out
                if (entity.hasComponent(LightComponent.class)) {
                    LightFadeComponent fade = entity.getComponent(LightFadeComponent.class);
                    if (fade == null) {
                        fade = new LightFadeComponent();
                        fade.targetAmbientIntensity = 0.0f;
                        fade.targetDiffuseIntensity = 0.0f;
                        fade.removeLightAfterFadeComplete = true;
                        entity.addComponent(fade);
                    } else {
                        fade.targetAmbientIntensity = 0.0f;
                        fade.targetDiffuseIntensity = 0.0f;
                        fade.removeLightAfterFadeComplete = true;
                        entity.saveComponent(fade);
                    }
                }
            } else if (oldLuminance == 0) {
                // Fade in
                LightComponent light = entity.getComponent(LightComponent.class);
                if (light == null) {
                    light = new LightComponent();
                    light.lightColorAmbient.set(1.0f, 0.6f, 0.6f);
                    light.lightColorDiffuse.set(1.0f, 0.6f, 0.6f);
                    light.lightDiffuseIntensity = 0.0f;
                    light.lightAmbientIntensity = 0.0f;
                    entity.addComponent(light);
                }

                LightFadeComponent fade = entity.getComponent(LightFadeComponent.class);
                if (fade == null) {
                    fade = new LightFadeComponent();
                    fade.targetAmbientIntensity = 1.0f;
                    fade.targetDiffuseIntensity = 1.0f;
                    fade.removeLightAfterFadeComplete = false;
                    entity.addComponent(fade);
                } else {
                    fade.targetAmbientIntensity = 1.0f;
                    fade.targetDiffuseIntensity = 1.0f;
                    fade.removeLightAfterFadeComplete = false;
                    entity.saveComponent(fade);
                }
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
