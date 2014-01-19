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
package org.terasology.rendering.nui.itemRendering;

import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.TextLineBuilder;

import java.util.List;

/**
 *
 */
public abstract class StringTextRenderer<T> extends AbstractItemRenderer<T> {

    @Override
    public void draw(T value, Canvas canvas) {
        canvas.drawText(getString(value));

    }

    @Override
    public Vector2i getPreferredSize(T value, Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, getString(value), canvas.size().x);
        return font.getSize(lines);
    }

    public abstract String getString(T value);
}
