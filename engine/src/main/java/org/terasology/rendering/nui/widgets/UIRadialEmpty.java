/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;

public class UIRadialEmpty extends UIRadialSection {
    @Override
    public void onDraw(Canvas canvas) {
    }

    @Override
    public void setSectionTexture(TextureRegion newTexture) {
    }

    @Override
    public void setSelectedTexture(TextureRegion newTexture) {
    }

    @Override
    public void setDrawRegion(Rect2i newRegion) {
    }

    @Override
    public void setInfoRegion(Rect2i newRegion) {
    }

    @Override
    public void setSelected(boolean selected) {
    }

    @Override
    public boolean getIsSubmenu() {
        return false;
    }
}
