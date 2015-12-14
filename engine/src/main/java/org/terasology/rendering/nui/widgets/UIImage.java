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
package org.terasology.rendering.nui.widgets;

import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 */
public class UIImage extends CoreWidget {
    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();

    public UIImage() {
    }

    public UIImage(String id) {
        super(id);
    }

    public UIImage(TextureRegion image) {
        this.image.set(image);
    }

    public UIImage(String id, TextureRegion image) {
        super(id);
        this.image.set(image);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image.get() != null) {
            canvas.drawTexture(image.get());
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        if (image.get() != null) {
            return image.get().size();
        }
        return Vector2i.zero();
    }

    public TextureRegion getImage() {
        return image.get();
    }

    public void setImage(TextureRegion image) {
        this.image.set(image);
    }

    public void bindTexture(Binding<TextureRegion> binding) {
        this.image = binding;
    }

}
