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
package org.terasology.rendering.nui;

import org.terasology.asset.Assets;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.skin.UISkin;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Immortius
 */
public class UIScreenLayer extends AbstractWidget {


    private UIWidget contents;
    private UISkin skin = Assets.getSkin("engine:default");
    private InteractionListener screenListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            return true;
        }
    };
    private NUIManager manager;

    public UIScreenLayer() {
    }

    public UIScreenLayer(String id) {
        super(id);
    }

    public void setId(String id) {
        super.setId(id);
    }

    public void initialise() {

    }

    public void setContents(UIWidget contents) {
        this.contents = contents;
    }

    public boolean isLowerLayerVisible() {
        return false;
    }

    public boolean isReleasingMouse() {
        return true;
    }

    public UIWidget getContents() {
        return contents;
    }

    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(screenListener);
        if (contents != null) {
            canvas.drawWidget(contents, canvas.getRegion());
        }
    }

    public UISkin getSkin() {
        return skin;
    }

    public void setSkin(UISkin skin) {
        this.skin = skin;
    }

    public void update(float delta) {
        if (contents != null) {
            contents.update(delta);
        }
    }

    public NUIManager getManager() {
        return manager;
    }

    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    @Override
    public void onGainFocus() {
    }

    @Override
    public void onLoseFocus() {
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        return areaHint;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return true;
    }

    @Override
    public String getMode() {
        return DEFAULT_MODE;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return Arrays.asList(contents).iterator();
    }
}
