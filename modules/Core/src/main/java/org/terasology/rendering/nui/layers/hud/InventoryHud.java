/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.ingame.inventory.InventoryCell;

public class InventoryHud extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    @In
    private Time time;

    private UICrosshair crosshair;

    @Override
    public void initialise() {
        for (InventoryCell cell : findAll(InventoryCell.class)) {
            cell.bindSelected(new SlotSelectedBinding(cell.getTargetSlot(), localPlayer));
            cell.bindTargetInventory(new ReadOnlyBinding<EntityRef>() {
                @Override
                public EntityRef get() {
                    return localPlayer.getCharacterEntity();
                }
            });
        }

        crosshair = find("crosshair", UICrosshair.class);
    }

    public void setChargeAmount(float amount) {
        crosshair.setChargeAmount(amount);
    }


    private static final class SlotSelectedBinding extends ReadOnlyBinding<Boolean> {

        private int slot;
        private LocalPlayer localPlayer;

        private SlotSelectedBinding(int slot, LocalPlayer localPlayer) {
            this.slot = slot;
            this.localPlayer = localPlayer;
        }

        @Override
        public Boolean get() {
            SelectedInventorySlotComponent component = localPlayer.getCharacterEntity().getComponent(SelectedInventorySlotComponent.class);
            return component != null && component.slot == slot;
        }
    }
}
