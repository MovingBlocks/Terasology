/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.baseWidgets;

import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DirectBinding;

/**
 * @author Immortius
 */
public class UIImage extends AbstractWidget {
    private Binding<TextureRegion> texture = new DirectBinding<>();

    public UIImage() {
    }

    public UIImage(TextureRegion texture) {
        this.texture.set(texture);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (texture.get() != null) {
            canvas.drawTexture(texture.get());
        }
    }

    public TextureRegion getTexture() {
        return texture.get();
    }

    public void setTexture(TextureRegion texture) {
        this.texture.set(texture);
    }

    public void bindTexture(Binding<TextureRegion> binding) {
        this.texture = binding;
    }

}
