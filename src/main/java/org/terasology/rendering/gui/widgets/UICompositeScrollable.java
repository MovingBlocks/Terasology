/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.widgets;

import org.terasology.rendering.gui.framework.UIDisplayContainerScrollable;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.layout.Layout;

/**
 * Scrollable Composition of multiple display elements which can be arranged in a specific manner by setting a layout type. Similar to the SWT composite class.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see org.eclipse.swt.widgets.Composite
 * TODO Adding a lot of display elements in a loop is inefficient.
 */
public class UICompositeScrollable extends UIDisplayContainerScrollable {
    
    private Layout compositeLayout;
    
    private void renderLayout() {
        if (compositeLayout != null) {
            compositeLayout.render();
        }
    }
    
    @Override
    public void render() {
        super.render();
        renderLayout();
    }
    
    @Override
    public void addDisplayElement(UIDisplayElement element) {
        super.addDisplayElement(element);
        
        applyLayout();
    }

    @Override
    public void addDisplayElementToPosition(int position, UIDisplayElement element) {
        super.addDisplayElementToPosition(position, element);
        
        applyLayout();
    }
    
    @Override
    public void removeDisplayElement(UIDisplayElement element) {
        super.removeDisplayElement(element);
        
        applyLayout();
    }
    
    @Override
    public void removeAllDisplayElements() {
        super.removeAllDisplayElements();
        
        applyLayout();
    }
    
    public Layout getLayout() {
        return compositeLayout;
    }
    
    public void setLayout(Layout layout) {
        compositeLayout = layout;
    }
    
    public void applyLayout() {
        if (compositeLayout != null) {
            compositeLayout.layout(this, false);
        }
    }
}