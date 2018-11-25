package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import java.util.ArrayList;
import java.util.List;

@RegisterSystem
public class TabbingManager extends BaseComponentSystem {

    public static final int UNINITIALIZED_DEPTH = -9999;
    public static final Logger logger = LoggerFactory.getLogger(TabbingManager.class);

    private static int currentNum;
    private static int maxNum;
    private static int nextNum;
    private static ArrayList<Integer> usedNums;
    private static ArrayList<WidgetWithOrder> widgetsList;
    private static boolean initialized = false;

    public TabbingManager() {
        currentNum = 0;
        maxNum = 0;
        nextNum = 0;
        logger.info("constructing");
        usedNums = new ArrayList<>();
        widgetsList = new ArrayList<>();
    }

    public static void increaseCurrentNum() {
        boolean loopedOnce = false;
        currentNum++;

        logger.info("usedNums size: "+usedNums.size());
        while (!usedNums.contains(currentNum)) {
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
        logger.info("nextNum: "+nextNum);
        while (usedNums.contains(nextNum)) {
            nextNum++;
            maxNum++;
        }
        usedNums.add(nextNum);
        return nextNum;
    }
    public static void addToUsedNums(int toAdd, WidgetWithOrder widget) {
        if (!usedNums.contains(toAdd)) {
            usedNums.add(toAdd);
            if (toAdd>maxNum) {
                maxNum = toAdd;
            }
            widgetsList.add(widget);
        } else {
            logger.info("one of depth already exists. ignoring.");
        }
    }
    public static void addToWidgetsList(WidgetWithOrder widget) {
        widgetsList.add(widget);
    }
    public static int getCurrentNum() {
        return currentNum;
    }
    public static List<WidgetWithOrder> getWidgetsList() {
        return widgetsList;
    }
}
