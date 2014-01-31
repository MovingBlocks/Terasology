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
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class ItemIcon extends CoreWidget {

    private Binding<TextureRegion> icon = new DefaultBinding<>();
    private Binding<Mesh> mesh = new DefaultBinding<>();
    private Binding<Texture> meshTexture = new DefaultBinding<>();
    private Binding<Integer> quantity = new DefaultBinding<>(1);
    private InteractionListener listener = new BaseInteractionListener();

    @Override
    public void onDraw(Canvas canvas) {
        if (getIcon() != null) {
            canvas.drawTexture(getIcon());
        } else if (getMesh() != null && getMeshTexture() != null) {
            Quat4f rot = new Quat4f(0, 0, 0, 1);
            QuaternionUtil.setEuler(rot, TeraMath.PI / 6, -TeraMath.PI / 12, 0);
            canvas.drawMesh(getMesh(), getMeshTexture(), canvas.getRegion(), rot, new Vector3f(), 1.0f);
        }
        if (getQuantity() > 1) {
            canvas.drawText(Integer.toString(getQuantity()));
        }
        if (!getTooltip().isEmpty()) {
            canvas.addInteractionRegion(listener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
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
}
