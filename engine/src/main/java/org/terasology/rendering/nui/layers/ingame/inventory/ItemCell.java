/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.inventory;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.List;

/**
 * Applies the logic to get information out of the EntityRef item
 */
public abstract class ItemCell extends CoreWidget {
    protected ItemIcon icon = new ItemIcon();
    private Binding<Boolean> selected = new DefaultBinding<>(false);

    public ItemCell() {
        icon.bindTooltipLines(
                new ReadOnlyBinding<List<TooltipLine>>() {
                    @Override
                    public List<TooltipLine> get() {
                        GetItemTooltip itemTooltip;

                        DisplayNameComponent displayNameComponent = getTargetItem().getComponent(DisplayNameComponent.class);
                        if (displayNameComponent != null) {
                            itemTooltip = new GetItemTooltip(displayNameComponent.name);
                        } else {
                            itemTooltip = new GetItemTooltip();
                        }
                        getTargetItem().send(itemTooltip);

                        return itemTooltip.getTooltipLines();
                    }
                });
        icon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
            @Override
            public TextureRegion get() {
                if (getTargetItem().exists()) {
                    ItemComponent itemComp = getTargetItem().getComponent(ItemComponent.class);
                    if (itemComp != null && itemComp.icon != null) {
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
        getTargetItem().send(new BeforeInventoryCellRendered(canvas));

        canvas.drawWidget(icon);

        getTargetItem().send(new InventoryCellRendered(canvas));
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.calculateRestrictedSize(icon, sizeHint);
    }

    public abstract EntityRef getTargetItem();

    public void bindSelected(Binding<Boolean> binding) {
        selected = binding;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean val) {
        selected.set(val);
    }

    @Override
    public float getTooltipDelay() {
        return 0;
    }

    @Override
    public String getMode() {
        if (isSelected()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }
}
