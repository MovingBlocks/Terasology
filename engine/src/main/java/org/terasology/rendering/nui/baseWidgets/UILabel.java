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

import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public class UILabel extends AbstractWidget {

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

    public UILabel(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    public String getText() {
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
        canvas.drawText(text.get());
    }
}
