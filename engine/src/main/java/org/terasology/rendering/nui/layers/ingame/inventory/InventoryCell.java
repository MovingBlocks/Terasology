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
package org.terasology.rendering.nui.layers.ingame.inventory;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.block.items.BlockItemComponent;

/**
 * @author Immortius
 */
public class InventoryCell extends CoreWidget {

    private Binding<EntityRef> targetInventory = new DefaultBinding<>(EntityRef.NULL);
    private Binding<Integer> targetSlot = new DefaultBinding<Integer>(0);

    private ItemIcon icon = new ItemIcon();

    private SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
    private LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (MouseInput.MOUSE_LEFT == button) {
                swapItem();
            } else if (MouseInput.MOUSE_RIGHT == button) {
                int stackSize = inventoryManager.getStackSize(getTargetItem());
                if (stackSize > 0) {
                    giveAmount((stackSize + 1) / 2);
                }
            }
            return true;
        }

        @Override
        public boolean onMouseWheel(int wheelTurns, Vector2i pos) {
            int amount = (Keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || Keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL)) ? 2 : 1;

            //move item to the transfer slot
            if (wheelTurns > 0) {
                takeAmount(amount);
            } else {
                //get item from transfer slot
                giveAmount(amount);
            }
            return true;
        }
    };

    public InventoryCell() {
        icon.bindTooltip(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                DisplayNameComponent displayNameComponent = getTargetItem().getComponent(DisplayNameComponent.class);
                if (displayNameComponent != null) {
                    return displayNameComponent.name;
                }
                return "";
            }
        });
        icon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                if (getTargetItem().exists()) {
                    ItemComponent itemComp = getTargetItem().getComponent(ItemComponent.class);
                    if (itemComp != null) {
                        return itemComp.icon;
                    }
                    BlockItemComponent blockItemComp = getTargetItem().getComponent(BlockItemComponent.class);
                    if (blockItemComp == null) {
                        return Assets.getTextureRegion("engine:items.questionMark");
                    }
                }
                return null;
            }
        });
        icon.bindMesh(new ReadOnlyBinding<Mesh>() {
            @Override
            public Mesh get() {
                BlockItemComponent blockItemComp = getTargetItem().getComponent(BlockItemComponent.class);
                if (blockItemComp != null) {
                    return blockItemComp.blockFamily.getArchetypeBlock().getMesh();
                }
                return null;
            }
        });
        icon.setMeshTexture(Assets.getTexture("engine:terrain"));
        icon.bindQuantity(new ReadOnlyBinding<Integer>() {
            @Override
            public Integer get() {
                ItemComponent itemComp = getTargetItem().getComponent(ItemComponent.class);
                if (itemComp != null) {
                    return UnsignedBytes.toInt(itemComp.stackCount);
                }
                return 1;
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(interactionListener, canvas.getRegion());
        canvas.drawWidget(icon);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.calculateRestrictedSize(icon, sizeHint);
    }

    public EntityRef getTargetItem() {
        return inventoryManager.getItemInSlot(getTargetInventory(), getTargetSlot());
    }

    public void bindTargetInventory(Binding<EntityRef> binding) {
        targetInventory = binding;
    }

    public EntityRef getTargetInventory() {
        return targetInventory.get();
    }

    public void setTargetInventory(EntityRef val) {
        targetInventory.set(val);
    }

    public void bindTargetSlot(Binding<Integer> binding) {
        targetSlot = binding;
    }

    public int getTargetSlot() {
        return targetSlot.get();
    }

    public void setTargetSlot(int val) {
        targetSlot.set(val);
    }

    @Override
    public float getTooltipDelay() {
        return 0;
    }

    private void swapItem() {
        if (getTransferItem().exists()) {
            inventoryManager.moveItem(getTransferEntity(), 0, getTargetInventory(), getTargetSlot());
        } else {
            inventoryManager.moveItem(getTargetInventory(), getTargetSlot(), getTransferEntity(), 0);
        }
    }

    private void giveAmount(int amount) {
        inventoryManager.moveItemAmount(getTargetInventory(), getTargetSlot(), getTransferEntity(), 0, amount);
    }

    private void takeAmount(int amount) {
        inventoryManager.moveItemAmount(getTransferEntity(), 0, getTargetInventory(), getTargetSlot(), amount);
    }

    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return inventoryManager.getItemInSlot(getTransferEntity(), 0);
    }
}
