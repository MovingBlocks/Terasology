/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.rendering.nui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controls the tabbing for widgets.
 */
public class TabbingManager {

    public static final int UNINITIALIZED_DEPTH = -9999;
    private static CoreScreenLayer openScreen;

    private static boolean widgetIsOpen;

    public static WidgetWithOrder focusedWidget;
    public static boolean focusSetThrough;

    private static int currentNum;
    private static int maxNum;
    private static int nextNum;
    private static ArrayList<Integer> usedNums;
    private static ArrayList<WidgetWithOrder> widgetsList;
    private static boolean initialized = false;

    /**
     * Resets TabbingManager values.
     */
    public static void init() {
        widgetIsOpen = false;
        focusedWidget = null;
        focusSetThrough = false;
        currentNum = 0;
        maxNum = 0;
        nextNum = 0;
        usedNums = new ArrayList<>();
        widgetsList = new ArrayList<>();
        initialized = true;
    }

    /**
     * Changes (increases or decreases) currentNum.
     * @param increase if currentNumber should be increased.
     */
    public static void changeCurrentNum(boolean increase) {
        boolean loopedOnce = false;
        boolean adjusted = false;

        while ((!adjusted || !usedNums.contains(currentNum)) && usedNums.size()>0) {
            adjusted = true;

            if (increase) {
                currentNum++;
            } else {
                currentNum--;
            }
            if (currentNum > maxNum) {
                if (!loopedOnce) {
                    currentNum = 0;
                    loopedOnce = true;
                } else {
                    break;
                }
            } else if (currentNum < 0) {
                currentNum = Collections.max(usedNums);
                loopedOnce = true;
            }
        }
    }

    /**
     * Unfocuses the currently focused widget.
     */
    public static void unfocusWidget() {
        if (focusedWidget != null) {
            focusSetThrough = true;
            focusedWidget = null;
            openScreen.getManager().setFocus(null);
        }
    }

    /**
     * Gives an unused number.
     * @return a new number for order
     */
    public static int getNewNextNum() {
        nextNum++;
        maxNum++;
        while (usedNums.contains(nextNum)) {
            nextNum++;
            maxNum++;
        }
        return nextNum;
    }

    /**
     * Adds the order value to usedNums.
     * @param toAdd the number to add to usedNums.
     */
    public static void addToUsedNums(int toAdd) {
        if (!usedNums.contains(toAdd)) {
            usedNums.add(toAdd);
            if (toAdd > maxNum) {
                maxNum = toAdd;
            }
        }
    }

    /**
     * Adds a widget to usedNums.
     * @param widget the widget to add to usedNums.
     */
    public static void addToWidgetsList(WidgetWithOrder widget) {
        if (!widgetsList.contains(widget)) {
            widgetsList.add(widget);
        }
    }

    /**
     * Resets currentNum to zero.
     */
    public static void resetCurrentNum() {
        currentNum = 0;
    }
    public static int getCurrentNum() {
        return currentNum;
    }
    public static List<WidgetWithOrder> getWidgetsList() {
        return widgetsList;
    }
    public static boolean isInitialized() {
        return initialized;
    }
    public static void setInitialized(boolean setInit) {
        initialized = setInit;
    }
    public static boolean isWidgetOpen() {
        return widgetIsOpen;
    }
    public static CoreScreenLayer getOpenScreen() {
        return openScreen;
    }
    public static void setOpenScreen(CoreScreenLayer open) {
        openScreen = open;
    }
    public static void setWidgetIsOpen(boolean open) {
        widgetIsOpen = open;
    }
}
