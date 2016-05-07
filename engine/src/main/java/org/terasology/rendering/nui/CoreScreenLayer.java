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

import org.terasology.assets.ResourceUrn;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.Keyboard;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.animation.MenuAnimationSystem;
import org.terasology.rendering.nui.animation.MenuAnimationSystemStub;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 */
public abstract class CoreScreenLayer extends AbstractWidget implements UIScreenLayer {

    private static final InteractionListener DEFAULT_SCREEN_LISTENER = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            return true;
        }
    };

    @LayoutConfig
    private UIWidget contents;

    private NUIManager manager;

    private MenuAnimationSystem animationSystem = new MenuAnimationSystemStub();

    public CoreScreenLayer() {
    }

    public CoreScreenLayer(String id) {
        super(id);
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    protected InteractionListener getScreenListener() {
        return DEFAULT_SCREEN_LISTENER;
    }

    public void setContents(UIWidget contents) {
        this.contents = contents;
    }

    @Override
    public void onOpened() {
        animationSystem.triggerFromPrev();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return true;
    }

    @Override
    public boolean isReleasingMouse() {
        return true;
    }

    public UIWidget getContents() {
        return contents;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect2i region = animationSystem.animateRegion(canvas.getRegion());
        if (isModal()) {
            canvas.addInteractionRegion(getScreenListener(), region);
        }
        if (getContents() != null) {
            canvas.drawWidget(getContents(), region);
        }
    }

    @Override
    public void update(float delta) {
        if (contents != null) {
            contents.update(delta);
            animationSystem.update(delta);
        }
    }

    @Override
    public NUIManager getManager() {
        return manager;
    }

    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    @Override
    public void onClosed() {
    }

    @Override
    public void onShow() {
        animationSystem.triggerFromNext();
    }

    @Override
    public void onHide() {
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
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.ESCAPE) {
            animationSystem.stop();
            if (isEscapeToCloseAllowed()) {
                triggerBackAnimation();
                return true;
            }
        }

        return false;
    }

    protected boolean isEscapeToCloseAllowed() {
        return true;
    }

    @Override
    public void onBindEvent(BindButtonEvent event) {
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        return areaHint;
    }

    @Override
    public boolean isModal() {
        return true;
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
        if (contents == null) {
            return Collections.emptyIterator();
        }
        return Arrays.asList(contents).iterator();
    }

    public MenuAnimationSystem getAnimationSystem() {
        return animationSystem;
    }

    protected void setAnimationSystem(MenuAnimationSystem animationSystem) {
        this.animationSystem = animationSystem;
    }

    protected void triggerForwardAnimation(ResourceUrn screenUri) {
        // create and initialize now, open when the animation has finished
        triggerForwardAnimation(getManager().createScreen(screenUri));
    }

    protected void triggerForwardAnimation(UIScreenLayer screen) {
        animationSystem.onEnd(() -> getManager().pushScreen(screen));
        animationSystem.triggerToNext();
    }

    protected void triggerBackAnimation() {
        animationSystem.onEnd(getManager()::popScreen);
        animationSystem.triggerToPrev();
    }
}
