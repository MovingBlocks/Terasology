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
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.List;

/**
 */
public class UILabel extends CoreWidget {

    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>("");

    public UILabel() {
    }

    public UILabel(String text) {
        this.text.set(text);
    }

    public UILabel(Binding<String> text) {
        this.text = text;
    }

    public UILabel(String id, String text) {
        super(id);
        this.text.set(text);
    }

    public UILabel(String id, String family, String text) {
        super(id);
        this.text.set(text);
        setFamily(family);
    }

    public UILabel(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    public String getText() {
        if (text.get() == null) {
            return "";
        }
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public void bindText(Binding<String> binding) {
        this.text = binding;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(getText());
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, getText(), areaHint.x);
        return font.getSize(lines);
    }
}
