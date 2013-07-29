/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.logic.players;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.console.ConsoleMessageEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.windows.metricsScreen.UIScreenMetrics;
import org.terasology.rendering.world.ViewDistance;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Immortius
 */
@RegisterSystem
public class DebugControlSystem implements ComponentSystem {

    private UIScreenMetrics metrics;

    @In
    private GameEngine engine;

    @In
    private WorldProvider world;
    @In
    private WorldRenderer worldRenderer;
    @In
    private Config config;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyEvent(KeyEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled && event.isDown()) {
            switch (event.getKey()) {
                case Keyboard.KEY_UP:
                    world.getTime().setDays(world.getTime().getDays() + 0.005f);
                    event.consume();
                    break;
                case Keyboard.KEY_DOWN:
                    world.getTime().setDays(world.getTime().getDays() - 0.005f);
                    event.consume();
                    break;
                case Keyboard.KEY_RIGHT:
                    world.getTime().setDays(world.getTime().getDays() + 0.02f);
                    event.consume();
                    break;
                case Keyboard.KEY_LEFT:
                    world.getTime().setDays(world.getTime().getDays() - 0.02f);
                    event.consume();
                    break;
            }
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled) {
            switch (event.getKey()) {
                case Keyboard.KEY_R:
                    config.getRendering().getDebug().setWireframe(!config.getRendering().getDebug().isWireframe());
                    event.consume();
                    break;
                case Keyboard.KEY_K:
                    entity.send(new DoDamageEvent(9999, null));
                    break;
                case Keyboard.KEY_H:
                    for (UIDisplayElement element : CoreRegistry.get(GUIManager.class).getWindowById("hud").getDisplayElements()) {
                        element.setVisible(!element.isVisible());
                    }

                    event.consume();
                    break;
                case Keyboard.KEY_F6:
                    config.getRendering().getDebug().setEnabled(!config.getRendering().getDebug().isEnabled());
                    event.consume();
                    break;
                case Keyboard.KEY_F7:
                    config.getRendering().getDebug().cycleStage();
                    entity.send(new ConsoleMessageEvent("Set debug stage to: " + config.getRendering().getDebug().getStage()));
                    event.consume();
                    break;
                case Keyboard.KEY_F8:
                    config.getRendering().getDebug().setRenderChunkBoundingBoxes(!config.getRendering().getDebug().isRenderChunkBoundingBoxes());
                    event.consume();
                    break;
            }
        }

        switch (event.getKey()) {
            case Keyboard.KEY_F1:
                engine.setFocus(!engine.hasFocus());
                Mouse.setGrabbed(engine.hasFocus());
                event.consume();
                break;
            case Keyboard.KEY_F3:
                config.getSystem().setDebugEnabled(!config.getSystem().isDebugEnabled());
                event.consume();
                break;
            case Keyboard.KEY_F:
                toggleViewingDistance();
                event.consume();
                break;
            case Keyboard.KEY_F4:
                metrics = (UIScreenMetrics) CoreRegistry.get(GUIManager.class).openWindow("metrics");
                metrics.toggleMode();
                event.consume();
                break;

        }
    }

    private void toggleViewingDistance() {
        config.getRendering().setViewDistance(ViewDistance.forIndex((config.getRendering().getViewDistance().getIndex() + 1) % ViewDistance.values().length));
    }
}
