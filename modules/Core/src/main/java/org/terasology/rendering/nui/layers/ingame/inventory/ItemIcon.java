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

import org.terasology.utilities.Assets;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.rendering.nui.widgets.TooltipLineRenderer;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ItemIcon extends CoreWidget {

    @LayoutConfig
    private Binding<TextureRegion> icon = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Mesh> mesh = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Texture> meshTexture = new DefaultBinding<>();
    @LayoutConfig
    private Binding<Integer> quantity = new DefaultBinding<>(1);

    private InteractionListener listener = new BaseInteractionListener();

    private UIList<TooltipLine> tooltip;

    public ItemIcon() {
        tooltip = new UIList<>();
        tooltip.setSelectable(false);
        final UISkin defaultSkin = Assets.getSkin("core:itemTooltip").get();
        tooltip.setSkin(defaultSkin);
        tooltip.setItemRenderer(new TooltipLineRenderer(defaultSkin));
        tooltip.bindList(new DefaultBinding<>(new ArrayList<>()));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getIcon() != null) {
            canvas.drawTexture(getIcon());
        } else if (getMesh() != null && getMeshTexture() != null) {
            Quat4f rot = new Quat4f(TeraMath.PI / 6, -TeraMath.PI / 12, 0);
            canvas.drawMesh(getMesh(), getMeshTexture(), canvas.getRegion(), rot, new Vector3f(), 1.0f);
        }
        if (getQuantity() > 1) {
            canvas.drawText(Integer.toString(getQuantity()));
        }
        List<TooltipLine> tooltipLines = tooltip.getList();
        if (tooltipLines != null && !tooltipLines.isEmpty()) {
            canvas.addInteractionRegion(listener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (icon != null) {
            TextureRegion texture = icon.get();
            if  (texture != null) {
                return texture.size();
            }
        }
        return new Vector2i();
    }

    @Override
    public float getTooltipDelay() {
        return 0;
    }

    public void bindIcon(Binding<TextureRegion> binding) {
        icon = binding;
    }

    public TextureRegion getIcon() {
        return icon.get();
    }

    public void setIcon(TextureRegion val) {
        icon.set(val);
    }

    public void bindQuantity(Binding<Integer> binding) {
        quantity = binding;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int val) {
        quantity.set(val);
    }

    public void bindMesh(Binding<Mesh> binding) {
        mesh = binding;
    }

    public Mesh getMesh() {
        return mesh.get();
    }

    public void setMesh(Mesh val) {
        mesh.set(val);
    }

    public void bindMeshTexture(Binding<Texture> binding) {
        meshTexture = binding;
    }

    public Texture getMeshTexture() {
        return meshTexture.get();
    }

    public void setMeshTexture(Texture val) {
        meshTexture.set(val);
    }

    public void bindTooltipLines(Binding<List<TooltipLine>> lines) {
        tooltip.bindList(lines);
    }

    public void setTooltipLines(List<TooltipLine> lines) {
        tooltip.setList(lines);
    }

    @Override
    public UIWidget getTooltip() {
        if (tooltip.getList().size() > 0) {
            return tooltip;
        } else {
            return null;
        }
    }
}
