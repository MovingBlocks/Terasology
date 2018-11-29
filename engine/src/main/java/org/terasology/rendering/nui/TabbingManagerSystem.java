package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RegisterSystem
public class TabbingManagerSystem extends BaseComponentSystem {

    public static final int UNINITIALIZED_DEPTH = -9999;
    public static final Logger logger = LoggerFactory.getLogger(TabbingManagerSystem.class);
    public static CoreScreenLayer openScreen;

    public static boolean tooltipLocked = false;

    public static WidgetWithOrder focusedWidget;
    public static boolean focusSetThrough = false;

    private static int currentNum;
    private static int maxNum;
    private static int nextNum;
    private static ArrayList<Integer> usedNums;
    private static ArrayList<WidgetWithOrder> widgetsList;
    private static boolean initialized = false;

    public static void init() {
        focusedWidget = null;
        currentNum = 0;
        maxNum = 0;
        nextNum = 0;
        logger.info("constructing");
        usedNums = new ArrayList<>();
        widgetsList = new ArrayList<>();
        initialized = true;
    }

    public static void increaseCurrentNum() {
        boolean loopedOnce = false;
        currentNum++;

        //removes duplicates
        Set<Integer> set = new HashSet<>();
        set.addAll(usedNums);
        usedNums.clear();
        usedNums.addAll(set);
        logger.info("used nums: "+usedNums);
        logger.info("widget list: "+widgetsList.size());

        logger.info("currentNum: "+currentNum);
        while (!usedNums.contains(currentNum)) {
            logger.info("doesn't contain");
            currentNum++;
            if (currentNum > maxNum) {
                if (!loopedOnce) {
                    logger.info("looped once");
                    currentNum = 0;
                    loopedOnce = true;
                } else {
                    logger.debug("usedNums doesn't contain enough numbers.");
                    break;
                }
            }
        }
    }
    public static int getNewNextNum() {
        nextNum++;
        maxNum++;
        while (usedNums.contains(nextNum)) {
            nextNum++;
            maxNum++;
        }
        logger.info("nextNum: "+nextNum);
        return nextNum;
    }
    public static void addToUsedNums(int toAdd, WidgetWithOrder widget) {
        if (!usedNums.contains(toAdd)) {
            usedNums.add(toAdd);
            if (toAdd>maxNum) {
                maxNum = toAdd;
            }
        } else {
            logger.debug("one of depth already exists. ignoring.");
        }
    }
    public static void addToWidgetsList(WidgetWithOrder widget) {
        if (!widgetsList.contains(widget)) {
            widgetsList.add(widget);
        }
    }
    public static void resetCurrentNum() { currentNum = 0; }
    public static int getCurrentNum() {
        return currentNum;
    }
    public static List<WidgetWithOrder> getWidgetsList() {
        return widgetsList;
    }
    public static boolean isInitialized() { return initialized; }
    public static void setInitialized(boolean setInit) { initialized = setInit; }
}
