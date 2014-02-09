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
package org.terasology.rendering.nui.properties;

import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.databinding.Binding;

import com.google.common.base.Preconditions;

/**
 * Created by synopia on 03.01.14.
 */
public class Property<P, UI extends UIWidget> {
    private final Binding<P> binding;
    private final UI editor;
    private final UILabel label;
    private String description;

    public Property(String labelText, Binding<P> binding, UI editor, String description) {
        Preconditions.checkArgument(editor != null, "editor must not be null");
        
        this.binding = binding;
        this.editor = editor;
        this.description = description;
        this.label = new UILabel("", labelText);
    }

    /**
     * @return the UI label widget, never <code>null</code>
     */
    public UILabel getLabel() {
        return label;
    }

    public Binding<P> getBinding() {
        return binding;
    }
    
    public String getDescription() {
        return description;
    }

    /**
     * @return the UI editor widget, never <code>null</code>
     */
    public UI getEditor() {
        return editor;
    }
}
