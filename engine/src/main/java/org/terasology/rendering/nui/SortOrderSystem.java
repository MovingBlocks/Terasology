/*
 * Copyright 2018 MovingBlocks
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

import com.google.common.collect.Queues;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindButtonSubscriber;
import org.terasology.input.BindableButton;
import org.terasology.input.Keyboard;
import org.terasology.input.internal.BindableButtonImpl;
import org.terasology.logic.console.ui.ConsoleScreen;
import org.terasology.registry.In;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

/**
 * Keeps track of the order of screens, and allows the order of them to change.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SortOrderSystem extends BaseComponentSystem {
    public static final int DEFAULT_DEPTH = -99999;
    private static int current;
    private static ArrayList<Integer> layersFilled;
    private static int index;
    private static boolean inSortOrder;
    private static ArrayList<CoreScreenLayer> enabledWidgets;
    private static boolean initialized = false;
    private static ArrayList<Integer> used;
    private static boolean controlPressed;

    @In
    private BindsManager bindsManager;

    /**
     * Initializes sort order.
     */
     public void postBegin() {
        initialized = true;
        controlPressed = false;
        Map<Integer, BindableButton> keys = bindsManager.getKeyBinds();
         BindButtonSubscriber controlSubscriber = new BindButtonSubscriber() {
             @Override
             public boolean onPress(float delta, EntityRef target) {
                 if (!containsConsole()) {
                     controlPressed = true;
                 } else {
                     controlPressed = false;
                 }
                 return false;
             }

             @Override
             public boolean onRepeat(float delta, EntityRef target) {
                 return false;
             }

             @Override
             public boolean onRelease(float delta, EntityRef target) {
                 controlPressed = false;
                 return false;
             }
         };
         BindButtonSubscriber tabSubscriber = new BindButtonSubscriber() {
             @Override
             public boolean onPress(float delta, EntityRef target) {
                 if (controlPressed) {
                     target.send(new FocusChangedEvent());
                 }
                 return false;
             }

             @Override
             public boolean onRepeat(float delta, EntityRef target) {
                 if (controlPressed) {
                     target.send(new FocusChangedEvent());
                 }
                 return false;
             }

             @Override
             public boolean onRelease(float delta, EntityRef target) {
                 return false;
             }
         };

         if (keys.containsKey(Keyboard.Key.RIGHT_CTRL.getId())) {
             keys.get(Keyboard.Key.RIGHT_CTRL.getId()).subscribe(controlSubscriber);
         } else {
             keys.put(Keyboard.Key.RIGHT_CTRL.getId(), new BindableButtonImpl(new SimpleUri("ctrlMod"), "Control Modifier", new BindButtonEvent()));
             keys.get(Keyboard.Key.RIGHT_CTRL.getId()).subscribe(controlSubscriber);
         }
         if (keys.containsKey(Keyboard.Key.LEFT_CTRL.getId())) {
             keys.get(Keyboard.Key.LEFT_CTRL.getId()).subscribe(controlSubscriber);
         } else {
             keys.put(Keyboard.Key.LEFT_CTRL.getId(), new BindableButtonImpl(new SimpleUri("ctrlMod"), "Control Modifier", new BindButtonEvent()));
             keys.get(Keyboard.Key.LEFT_CTRL.getId()).subscribe(controlSubscriber);
         }
         if (keys.containsKey(Keyboard.Key.TAB.getId())) {
             keys.get(Keyboard.Key.TAB.getId()).subscribe(tabSubscriber);
         } else {
             keys.put(Keyboard.Key.TAB.getId(), new BindableButtonImpl(new SimpleUri("changeFocus"), "Change Focus", new BindButtonEvent()));
             keys.get(Keyboard.Key.TAB.getId()).subscribe(tabSubscriber);
         }

        current = 0;
        index = 0;
        layersFilled = new ArrayList<>();
        enabledWidgets = new ArrayList<>();
        used = new ArrayList<>();
        inSortOrder = false;
    }

    @ReceiveEvent
    public void changeFocus(FocusChangedEvent event, EntityRef ref) {
        rotateOrder(true);
    }

    /**
     * rotates through the elements
     * @param increase where or not to increment index
     */
    public static void rotateOrder(boolean increase) {
        if (layersFilled.size() > 0) {
            Collections.sort(layersFilled, (a, b) -> Math.max(a, b));
            if (increase) {
                index++;
            }
            int iterator;

            while(index >= layersFilled.size()) {
                index -= layersFilled.size();
            }

            iterator = layersFilled.get(index);

            boolean loopThroughDone = false;


            int tempIndex = index;
            int timesLooping = 0;

            ArrayList<CoreScreenLayer> widgetsCopy = new ArrayList<>(enabledWidgets);

            while (!loopThroughDone) {
                for (CoreScreenLayer widget : widgetsCopy) {
                    if (widget.getManager() != null) {
                        inSortOrder = true;
                        if (widget.getDepth() == iterator) {
                            String widgId = widget.getId();
                            widget.getManager().pushScreen(widgId);
                            widget.getManager().render();
                        }
                        inSortOrder = false;
                    }
                }
                if (tempIndex < layersFilled.size()) {
                    iterator = layersFilled.get(tempIndex);
                    tempIndex++;
                } else {
                    tempIndex = 0;
                    iterator = layersFilled.get(tempIndex);
                    tempIndex++;
                }
                if (timesLooping > layersFilled.size()) {
                    loopThroughDone = true;
                }
                timesLooping++;
            }
            for (CoreScreenLayer layer:enabledWidgets) {
                if (layer.getManager() != null) {
                    Deque<UIScreenLayer> screens = layer.getManager().getScreens();
                    Deque<UIScreenLayer> toCreate = Queues.newArrayDeque();
                    for (UIScreenLayer screen : screens) {
                        if (!toCreate.contains(screen)) {
                            toCreate.add(screen);
                        }
                    }
                    layer.getManager().setScreens(toCreate);
                }
            }

            enabledWidgets = widgetsCopy;
        }
    }

    /**
     * increments current (for depth)
     * @return the new value of current
     */
    public static int getCurrent() {
        current++;
        while (used.contains(current)) {
            current++;
        }
        return current;
    }

    /**
     * Adds a certain depth to layersFilled.
     * @param layer the depth
     */
    public static void addAnother(int layer) {
        if (!layersFilled.contains(layer)) {
            layersFilled.add(layer);
        }
        index = -1;
    }

    /**
     * removes an occurance of a certain depth to layers filled
     * @param layer the depth
     */
    public static void removeOne(int layer) {
        if (layersFilled.contains(layer)) {
            layersFilled.remove(layersFilled.indexOf(layer));
        }
    }

    public static void setEnabledWidgets(ArrayList<CoreScreenLayer> widgetList) {
        if (initialized) {
            enabledWidgets = widgetList;
        }
    }

    public static ArrayList<CoreScreenLayer> getEnabledWidgets() {
        return enabledWidgets;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isInSortOrder() {
        return inSortOrder;
    }

    public static ArrayList<Integer> getUsed() {
        return used;
    }

    public static void setUsed(ArrayList<Integer> other) {
        used = other;
    }

    public static boolean containsConsole() {
        if (enabledWidgets != null) {
            boolean containsConsole = false;
            for (CoreScreenLayer layer : enabledWidgets) {
                if (layer instanceof ConsoleScreen) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean getControlPressed() {
        return controlPressed;
    }
}
