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
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.FocusManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.canvas.CanvasControl;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.nui.asset.UIElement;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;

import java.util.Deque;

/**
 */
public interface NUIManager extends ComponentSystem, FocusManager {

    HUDScreenLayer getHUD();

    boolean isHUDVisible();

    void setHUDVisible(boolean visible);

    boolean isOpen(String screenUri);

    boolean isOpen(ResourceUrn screenUri);

    /* @deprecated */
    @Deprecated
    boolean isOpen(UIElement element);

    UIScreenLayer getScreen(ResourceUrn screenUri);

    UIScreenLayer getScreen(String screenUri);

    void closeScreen(String screenUri);

    void closeScreen(ResourceUrn screenUri);

    void closeScreen(UIScreenLayer screen);

    /* @deprecated */
    @Deprecated
    void closeScreen(UIElement element);

    void closeAllScreens();

    void toggleScreen(String screenUri);

    void toggleScreen(ResourceUrn screenUri);

    /* @deprecated */
    @Deprecated
    void toggleScreen(UIElement element);

    UIScreenLayer createScreen(String screenUri);

    UIScreenLayer createScreen(ResourceUrn screenUri);

    <T extends CoreScreenLayer> T createScreen(String screenUri, Class<T> expectedType);

    <T extends CoreScreenLayer> T createScreen(ResourceUrn screenUri, Class<T> expectedType);

    UIScreenLayer pushScreen(ResourceUrn screenUri);

    UIScreenLayer pushScreen(String screenUri);

    /* @deprecated */
    @Deprecated
    UIScreenLayer pushScreen(UIElement element);

    <T extends CoreScreenLayer> T pushScreen(ResourceUrn screenUri, Class<T> expectedType);

    <T extends CoreScreenLayer> T pushScreen(String screenUri, Class<T> expectedType);

    /* @deprecated */
    @Deprecated
    <T extends CoreScreenLayer> T pushScreen(UIElement element, Class<T> expectedType);

    void pushScreen(UIScreenLayer layer);

    void popScreen();

    <T extends ControlWidget> T addOverlay(String screenUri, Class<T> expectedType);

    <T extends ControlWidget> T addOverlay(ResourceUrn screenUri, Class<T> expectedType);

    /* @deprecated */
    @Deprecated
    <T extends ControlWidget> T addOverlay(UIElement element, Class<T> expectedType);

    /* @deprecated */
    @Deprecated
    void removeOverlay(UIElement overlay);

    void removeOverlay(String uri);

    void removeOverlay(ResourceUrn uri);

    Deque<UIScreenLayer> getScreens();

    void setScreens(Deque<UIScreenLayer> screens);

    ResourceUrn getUri(UIScreenLayer screen);

    //void setUpdateFrozen(boolean updateFrozen);

    void clear();

    void render();

    void update(float delta);

    ClassLibrary<UIWidget> getWidgetMetadataLibrary();

    @Override
    void setFocus(UIWidget element);

    @Override
    UIWidget getFocus();

    boolean isReleasingMouse();

    boolean isForceReleasingMouse();

    void setForceReleasingMouse(boolean value);

    void invalidate();

    CanvasControl getCanvas();

}
