/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.manager;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.rendering.gui.widgets.UIWindow;

public class GUIManagerHeadless implements GUIManager {

    @Override
    public void render() {
    }

    @Override
    public void update() {
    }

    @Override
    public void update(boolean force) {
    }

    @Override
    public void closeWindow(UIWindow window) {
    }

    @Override
    public void closeWindow(String windowId) {
    }

    @Override
    public void closeAllWindows() {
    }

    @Override
    public UIWindow openWindow(UIWindow window) {
        return null;
    }

    @Override
    public UIWindow openWindow(String windowId) {
        return null;
    }

    @Override
    public void registerWindow(String windowId, Class<? extends UIWindow> windowClass) {
    }

    @Override
    public UIWindow loadWindow(String windowId) {
        return null;
    }

    @Override
    public UIWindow getWindowById(String windowId) {
        return null;
    }

    @Override
    public UIWindow getFocusedWindow() {
        return null;
    }

    @Override
    public boolean isReleasingMouse() {
        return false;
    }

    @Override
    public boolean isConsumingInput() {
        return false;
    }

    @Override
    public void showMessage(String title, String text) {
    }

    @Override
    public void initialise() {
    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {
    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {
    }

    @Override
    public void onMouseX(MouseXAxisEvent event, EntityRef entity) {
    }

    @Override
    public void onMouseY(MouseYAxisEvent event, EntityRef entity) {
    }

    @Override
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
    }

    @Override
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
    }

    @Override
    public void keyEvent(KeyEvent event, EntityRef entity) {
    }

    @Override
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
    }

    @Override
    public void toggleWindow(String id) {
    }

}
