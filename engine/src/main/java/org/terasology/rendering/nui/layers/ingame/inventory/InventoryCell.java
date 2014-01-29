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

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.world.block.items.BlockItemComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class InventoryCell extends CoreWidget {

    private Binding<EntityRef> targetItem = new DefaultBinding<>(EntityRef.NULL);

    @Override
    public void onDraw(Canvas canvas) {
        ItemComponent itemComponent = getTargetItem().getComponent(ItemComponent.class);
        if (itemComponent != null) {
            if (itemComponent.icon != null) {
                canvas.drawTexture(itemComponent.icon);
            } else {
                BlockItemComponent blockItem = getTargetItem().getComponent(BlockItemComponent.class);
                if (blockItem != null) {
                    Mesh mesh = blockItem.blockFamily.getArchetypeBlock().getMesh();
                    if (mesh != null) {
                        Quat4f rot = new Quat4f(0, 0, 0, 1);
                        QuaternionUtil.setEuler(rot, TeraMath.PI / 6, -TeraMath.PI / 12, 0);
                        canvas.drawMesh(mesh, Assets.getTexture("engine:terrain"), canvas.getRegion(), rot, new Vector3f(), 1.0f);
                    } else {
                        canvas.drawTexture(Assets.getSubtexture("engine:items.questionMark"));
                    }
                } else {
                    canvas.drawTexture(Assets.getSubtexture("engine:items.questionMark"));
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i();
    }

    public void bindTargetItem(Binding<EntityRef> binding) {
        targetItem = binding;
    }

    public EntityRef getTargetItem() {
        return targetItem.get();
    }

    public void setTargetItem(EntityRef val) {
        targetItem.set(val);
    }
}
