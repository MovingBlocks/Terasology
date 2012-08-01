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
package org.terasology.rendering.gui.menus;

import org.terasology.rendering.gui.components.UIOpenBook;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

public class UIOpenBookScreen extends UIDisplayWindow {

    private final UIOpenBook _openbook;

    public UIOpenBookScreen() {
        maximize();
        _openbook = new UIOpenBook();
        _openbook.setVisible(true);
        addDisplayElement(_openbook);
        update();
        setModal(true);
    }

    @Override
    public void update() {
        super.update();
        _openbook.isVisible();
        _openbook.center();
    }
}