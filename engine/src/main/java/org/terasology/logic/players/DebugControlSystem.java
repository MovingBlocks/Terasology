/*
 * Copyright 2013 MovingBlocks
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

import org.lwjgl.input.Mouse;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.Keyboard;
import org.terasology.input.binds.general.HideHUDButton;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.debug.DebugProperties;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.metrics.DebugOverlay;
import org.terasology.world.WorldProvider;


@RegisterSystem(RegisterMode.CLIENT)
public class DebugControlSystem extends BaseComponentSystem {

    @In
    private WorldProvider world;
    
    @In
    private Config config;

    @In
    private NUIManager nuiManager;
    
    private DebugOverlay overlay;
    
    private boolean mouseGrabbed = true;

    @Override
    public void initialise() {
        overlay = nuiManager.addOverlay("engine:debugOverlay", DebugOverlay.class);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onHideHUD(HideHUDButton event, EntityRef entity) {
        if (event.isDown()) {
            // Make sure both are either visible or hidden
            final boolean hide = !(config.getRendering().getDebug().isHudHidden() && config.getRendering().getDebug().isFirstPersonElementsHidden());

            config.getRendering().getDebug().setFirstPersonElementsHidden(hide);
            config.getRendering().getDebug().setHudHidden(hide);

            event.consume();
        }
    }


    /**
     * Creates illusion of time flying by if correspondig key is held down.
     * Up / Down : Increases / Decreases time of day by 0.005 per keystroke.
     * Right / left : Increases / Decreases time of day by 0.02 per keystroke.
     * @param entity The player entity that triggered the time change.
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyEvent(KeyEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled && event.isDown()) {
            switch (event.getKey().getId()) {
                case Keyboard.KeyId.UP:
                    world.getTime().setDays(world.getTime().getDays() + 0.005f);
                    event.consume();
                    break;
                case Keyboard.KeyId.DOWN:
                    world.getTime().setDays(world.getTime().getDays() - 0.005f);
                    event.consume();
                    break;
                case Keyboard.KeyId.RIGHT:
                    world.getTime().setDays(world.getTime().getDays() + 0.02f);
                    event.consume();
                    break;
                case Keyboard.KeyId.LEFT:
                    world.getTime().setDays(world.getTime().getDays() - 0.02f);
                    event.consume();
                    break;
                default:
                    break;
            }
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        boolean debugEnabled = config.getSystem().isDebugEnabled();
        // Features for debug mode only
        if (debugEnabled) {
            switch (event.getKey().getId()) {
                case Keyboard.KeyId.H:
                    String DebugInfo = "engine:DebugInfo";
                    nuiManager.toggleScreen(DebugInfo);
                    event.consume();
                    break;
                case Keyboard.KeyId.F6:
                    config.getRendering().getDebug().setEnabled(!config.getRendering().getDebug().isEnabled());
                    event.consume();
                    break;
                case Keyboard.KeyId.F8:
                    config.getRendering().getDebug().setRenderChunkBoundingBoxes(!config.getRendering().getDebug().isRenderChunkBoundingBoxes());
                    event.consume();
                    break;
                case Keyboard.KeyId.F9:
                    config.getRendering().getDebug().setWireframe(!config.getRendering().getDebug().isWireframe());
                    event.consume();
                    break;
                default:
                    break;
            }
        }

        switch (event.getKey().getId()) {
            case Keyboard.KeyId.F11:
                mouseGrabbed = !mouseGrabbed;
                DebugProperties debugProperties = (DebugProperties) nuiManager.getHUD().getHUDElement("engine:DebugProperties");
                debugProperties.setVisible(!mouseGrabbed);
                Mouse.setGrabbed(mouseGrabbed);
                event.consume();
                break;
            case Keyboard.KeyId.F3:
                config.getSystem().setDebugEnabled(!config.getSystem().isDebugEnabled());
                event.consume();
                break;
            case Keyboard.KeyId.F4:
                overlay.toggleMetricsMode();
                event.consume();
                break;
            default:
                break;

        }
    }

    @ReceiveEvent(components = CharacterComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onMouseX(MouseAxisEvent event, EntityRef entity) {
        if (!mouseGrabbed) {
            event.consume();
        }
    }

}
