/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com> <benjamin.glatzel@me.com>
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

package org.terasology.componentSystem.controllers;

import org.lwjgl.input.Keyboard;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.windows.UIScreenMetrics;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Immortius
 */
@RegisterComponentSystem
public class DebugControlSystem implements EventHandlerSystem {

    private UIScreenMetrics metrics;
    private WorldProvider world;
    private WorldRenderer worldRenderer;

    @Override
    public void initialise() {
        world = CoreRegistry.get(WorldProvider.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onKeyEvent(KeyEvent event, EntityRef entity) {
        boolean debugEnabled = Config.getInstance().isDebug();
        // Features for debug mode only
        if (debugEnabled && event.isDown()) {
            switch (event.getKey()) {
                case Keyboard.KEY_UP:
                    world.setTimeInDays(world.getTimeInDays() + 0.005f);
                    event.consume();
                    break;
                case Keyboard.KEY_DOWN:
                    world.setTimeInDays(world.getTimeInDays() - 0.005f);
                    event.consume();
                    break;
                case Keyboard.KEY_RIGHT:
                    world.setTimeInDays(world.getTimeInDays() + 0.02f);
                    event.consume();
                    break;
                case Keyboard.KEY_LEFT:
                    world.setTimeInDays(world.getTimeInDays() - 0.02f);
                    event.consume();
                    break;
            }
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        boolean debugEnabled = Config.getInstance().isDebug();
        // Features for debug mode only
        if (debugEnabled) {
            switch (event.getKey()) {
                case Keyboard.KEY_R:
                    worldRenderer.setWireframe(!worldRenderer.isWireframe());
                    event.consume();
                    break;
                case Keyboard.KEY_P:
                    worldRenderer.setCameraMode(WorldRenderer.CAMERA_MODE.PLAYER);
                    event.consume();
                    break;
                case Keyboard.KEY_O:
                    worldRenderer.setCameraMode(WorldRenderer.CAMERA_MODE.SPAWN);
                    event.consume();
                    break;
                case Keyboard.KEY_K:
                    entity.send(new DamageEvent(9999, null));
                    break;
                case Keyboard.KEY_H:
                	for (UIDisplayElement element : GUIManager.getInstance().getWindowById("hud").getDisplayElements()) {
                        element.setVisible(!element.isVisible());
                    }
                	
                    event.consume();
                    break;
            }
        }

        switch (event.getKey()) {
            case Keyboard.KEY_F3:
                Config.getInstance().setDebug(!Config.getInstance().isDebug());
                event.consume();
                break;
            case Keyboard.KEY_F:
                toggleViewingDistance();
                event.consume();
                break;
            case Keyboard.KEY_F4:
                metrics = (UIScreenMetrics) GUIManager.getInstance().openWindow("metrics");
                metrics.toggleMode();
                event.consume();
                break;
            case Keyboard.KEY_F5:
                GUIManager.getInstance().openWindow("itemList");
                event.consume();
                break;
        }
    }

    private void toggleViewingDistance() {
        Config.getInstance().setViewingDistanceById((Config.getInstance().getActiveViewingDistanceId() + 1) % 4);
    }
}
