/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.craft.components.actions;

import com.google.common.collect.Maps;
import org.terasology.components.actions.ActionTarget;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Small-Jeeper
 */
public class CraftingActionComponent implements Component {
    public ActionTarget relativeTo = ActionTarget.Self;

    private Map<String, ArrayList<EntityRef>> elements = Maps.newHashMap();

    public static String[] levels = {"bottom", "middle", "top"};

    public EntityRef possibleItem = EntityRef.NULL;
    public EntityRef lastSelected = EntityRef.NULL;
    public boolean isRefinement = false;

    public final static int MAX_SLOTS = 9;
    public final static int MAX_LEVEL = 3;

    private int currentItemSlot = 0;
    private int currentLevel = 0;

    public void increaseLevel() {
        currentLevel++;
        if (currentLevel >= MAX_LEVEL) {
            currentLevel = (MAX_LEVEL - 1);
        }
    }

    public void decreaseLevel() {
        currentLevel--;

        if (currentLevel < 0) {
            currentLevel = 0;
        }
    }

    public void addItem(int slot, EntityRef entity) {
        if (!elements.containsKey(levels[currentLevel])) {
            ArrayList<EntityRef> list = new ArrayList<EntityRef>();

            for (int i = 0; i < MAX_SLOTS; i++) {
                if (slot == i) {
                    list.add(entity);
                } else {
                    list.add(EntityRef.NULL);
                }
            }

            elements.put(levels[currentLevel], list);

            return;
        }

        elements.get(levels[currentLevel]).set(slot, entity);
    }

    public void deleteItem(int slot) {
        if (!elements.containsKey(levels[currentLevel])) {
            return;
        }

        ArrayList<EntityRef> list = elements.get(levels[currentLevel]);
        list.set(slot, EntityRef.NULL);


        for (String key : levels) {

            if (!elements.containsKey(key)) {
                continue;
            }

            boolean deleteLevel = true;
            for (EntityRef entity : elements.get(key)) {
                if (!entity.equals(EntityRef.NULL)) {
                    deleteLevel = false;
                    break;
                }
            }
            if (deleteLevel) {
                elements.remove(key);
            }
        }
    }

    public void deleteItem(int level, int slot) {
        if (!elements.containsKey(levels[level])) {
            return;
        }

        ArrayList<EntityRef> list = elements.get(levels[level]);
        list.set(slot, EntityRef.NULL);

        for (EntityRef entity : list) {
            if (!entity.equals(EntityRef.NULL)) {
                return;
            }
        }

        elements.remove(levels[level]);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public ArrayList<EntityRef> getLevelElements(int level) {
        if (!elements.containsKey(levels[level])) {
            return null;
        }

        return elements.get(levels[level]);
    }

    public ArrayList<EntityRef> getLevelElements(String level) {
        if (!elements.containsKey(level)) {
            return null;
        }

        return elements.get(level);
    }

    public ArrayList<EntityRef> getCurrentLevelElements() {
        if (!elements.containsKey(levels[currentLevel])) {
            addItem(0, EntityRef.NULL);
        }

        return elements.get(levels[currentLevel]);
    }

    public Map<String, ArrayList<EntityRef>> getAllElements() {
        return elements;
    }

}
