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
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;


@RegisterSystem(RegisterMode.ALWAYS)
public class SortOrder extends BaseComponentSystem {
    private static int current;
    private static ArrayList<Integer[]> layersFilled; //arg1 of the Integer[] is the layer depth, arg2 is the number of things on that layer
    private static int index;
    private static int uiIndex;
    private static boolean inSortOrder;
    private static ArrayList<CoreScreenLayer> enabledWidgets;
    private static boolean initialized = false;
    private static ArrayList<Integer> used;

    @In
    private BindsManager bindsManager;

    /**
     * Initializes sort order
     * @param event
     * @param player
     */
    @ReceiveEvent
    public void onPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        initialized = true;
        Map<Integer, BindableButton> keys = bindsManager.getKeyBinds();
        if (keys.containsKey(Keyboard.Key.SEMICOLON.getId())) {
            keys.get(Keyboard.Key.SEMICOLON.getId()).subscribe(new BindButtonSubscriber() {
                @Override
                public boolean onPress(float delta, EntityRef target) {
                    target.send(new FocusChangedEvent());
                    return false;
                }

                @Override
                public boolean onRepeat(float delta, EntityRef target) {
                    target.send(new FocusChangedEvent());
                    return false;
                }

                @Override
                public boolean onRelease(float delta, EntityRef target) {
                    return false;
                }
            });
        } else {
            BindButtonSubscriber bindButtonSubscriber = new BindButtonSubscriber() {
                @Override
                public boolean onPress(float delta, EntityRef target) {
                    target.send(new FocusChangedEvent());
                    return false;
                }

                @Override
                public boolean onRepeat(float delta, EntityRef target) {
                    target.send(new FocusChangedEvent());
                    return false;
                }

                @Override
                public boolean onRelease(float delta, EntityRef target) {
                    return false;
                }
            };
            keys.put(Keyboard.Key.SEMICOLON.getId(), new BindableButtonImpl(new SimpleUri("changeFocus"), "Change Focus", new BindButtonEvent()));
            keys.get(Keyboard.Key.SEMICOLON.getId()).subscribe(bindButtonSubscriber);
        }
        current = 0;
        index = 0;
        uiIndex = -1;
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
            Collections.sort(layersFilled, (a, b) -> Math.max(a[0], b[0]));
            if (increase) {
                index++;
            }
            int iterator;
            if (index < layersFilled.size()) {
                iterator = layersFilled.get(index)[0];
            } else {
                index = 0;
                iterator = layersFilled.get(index)[0];
            }
            boolean loopThroughDone = false;


            int tempIndex = index;
            int timesLooping = 0;

            ArrayList<CoreScreenLayer> widgetsCopy = new ArrayList<>(enabledWidgets);

            while (!loopThroughDone) {
                for (CoreScreenLayer widget : widgetsCopy) {
                    inSortOrder = true;
                    if (widget.getDepth() == iterator) {
                        String widgId = widget.getId();
                        widget.getManager().pushScreen(widgId);
                        widget.getManager().render();
                    }
                    inSortOrder = false;
                }
                if (tempIndex < layersFilled.size()) {
                    iterator = layersFilled.get(tempIndex)[0];
                    tempIndex++;
                } else {
                    tempIndex = 0;
                    iterator = layersFilled.get(tempIndex)[0];
                    tempIndex++;
                }
                if (timesLooping > layersFilled.size()) {
                    loopThroughDone = true;
                }
                timesLooping++;
            }
            for (CoreScreenLayer layer:enabledWidgets) {
                Deque<UIScreenLayer> screens = layer.getManager().getScreens();
                Deque<UIScreenLayer> toCreate = Queues.newArrayDeque();
                for (UIScreenLayer screen: screens) {
                    if (!toCreate.contains(screen)) {
                        toCreate.add(screen);
                    }
                }
                layer.getManager().setScreens(toCreate);
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
     * adds another occurrence of a certain depth to layers filled
     * @param layer the depth
     */
    public static void addAnother(int layer) {
        try {
            layersFilled.get(layer)[1]++;
        } catch (Exception e) {
            Integer[] toAdd = new Integer[2];
            toAdd[0] = layer;
            toAdd[1] = 1;
            layersFilled.add(toAdd);
        }
    }

    /**
     * removes an occurance of a certain depth to layers filled
     * @param layer the depth
     */
    public static void removeOne(int layer) {
        for (int i = 0; i < layersFilled.size(); i++) {
            if (layersFilled.get(i)[0] == layer) {
                layersFilled.get(i)[1]--;
                return;
            }
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
    public static int makeIndex() {
        uiIndex++;
        return uiIndex;
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
}
