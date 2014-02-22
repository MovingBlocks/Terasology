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

import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;

/**
 * @author Immortius
 */
public interface NUIManager extends ComponentSystem, FocusManager {

    HUDScreenLayer getHUD();

    boolean isHUDVisible();

    void setHUDVisible(boolean visible);

    boolean isOpen(String screenUri);

    boolean isOpen(AssetUri screenUri);

    UIScreenLayer getScreen(AssetUri screenUri);

    UIScreenLayer getScreen(String screenUri);

    void closeScreen(String screenUri);

    void closeScreen(AssetUri screenUri);

    void closeScreen(UIScreenLayer screen);

    void toggleScreen(String screenUri);

    void toggleScreen(AssetUri screenUri);

    UIScreenLayer pushScreen(AssetUri screenUri);

    UIScreenLayer pushScreen(String screenUri);

    <T extends CoreScreenLayer> T pushScreen(AssetUri screenUri, Class<T> expectedType);

    <T extends CoreScreenLayer> T pushScreen(String screenUri, Class<T> expectedType);

    void pushScreen(CoreScreenLayer screen);

    void pushScreen(CoreScreenLayer screen, AssetUri uri);

    void popScreen();

    UIScreenLayer setScreen(AssetUri screenUri);

    UIScreenLayer setScreen(String screenUri);

    <T extends CoreScreenLayer> T setScreen(AssetUri screenUri, Class<T> expectedType);

    <T extends CoreScreenLayer> T setScreen(String screenUri, Class<T> expectedType);

    void setScreen(CoreScreenLayer screen);

    <T extends ControlWidget> T addOverlay(String screenUri, Class<T> expectedType);

    <T extends ControlWidget> T addOverlay(AssetUri screenUri, Class<T> expectedType);

    void addOverlay(ControlWidget overlay);

    void removeOverlay(ControlWidget overlay);

    void clear();

    void render();

    void update(float delta);

    ClassLibrary<UIWidget> getWidgetMetadataLibrary();

    void setFocus(UIWidget element);

    UIWidget getFocus();

    boolean isReleasingMouse();
}
