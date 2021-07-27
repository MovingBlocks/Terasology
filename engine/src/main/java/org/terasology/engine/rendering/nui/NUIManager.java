// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.rendering.nui.layers.hud.HUDScreenLayer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.FocusManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.canvas.CanvasControl;
import org.terasology.nui.reflection.WidgetLibrary;

import java.util.Deque;

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

    WidgetLibrary getWidgetMetadataLibrary();

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
