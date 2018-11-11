package org.terasology.rendering.nui;
//TODO: make other ordered widgets inherit from WidgetWithOrder
//TODO: figure out why bindsManager is null, then test
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindButtonSubscriber;
import org.terasology.input.BindableButton;
import org.terasology.input.Keyboard;
import org.terasology.input.internal.BindableButtonImpl;
import org.terasology.registry.In;

import java.util.ArrayList;
import java.util.Map;

@RegisterSystem
public class TabbingManager extends BaseComponentSystem {

    @In
    private BindsManager bindsManager;

    public static final Logger logger = LoggerFactory.getLogger(TabbingManager.class);

    public int currentNum;
    public int maxNum;
    public int nextNum;
    public static boolean initialized = false;
    public ArrayList<Integer> usedNums;
    public ArrayList<WidgetWithOrder> widgetsList;

    TabbingManager() {
        if (!initialized) {
            initialize();
        }
        currentNum = 0;
        maxNum = 0;
        nextNum = 0;

        usedNums = new ArrayList<>();
        usedNums.add(nextNum);
    }
    TabbingManager(ArrayList<Integer> alreadyUsed) {
        if (!initialized) {
            initialize();
        }
        usedNums = alreadyUsed;
    }

    private void initialize() {
        logger.info("bindsManager: " + bindsManager);
        if (bindsManager != null) {
            Map<Integer, BindableButton> keys = bindsManager.getKeyBinds();
            BindButtonSubscriber subscriber = new BindButtonSubscriber() {
                @Override
                public boolean onPress(float delta, EntityRef target) {
                    logger.info("pressed");
                    target.send(new ChangeActiveWidgetEvent());
                    return false;
                }

                @Override
                public boolean onRepeat(float delta, EntityRef target) {
                    logger.info("held");
                    target.send(new ChangeActiveWidgetEvent());
                    return false;
                }

                @Override
                public boolean onRelease(float delta, EntityRef target) {
                    return false;
                }
            };
            if (keys.containsKey(Keyboard.Key.BACKSLASH)) {
                keys.get(Keyboard.Key.BACKSLASH.getId()).subscribe(subscriber);
            } else {
                keys.put(Keyboard.Key.BACKSLASH.getId(), new BindableButtonImpl(new SimpleUri("changeActive"), "Change Focused Widget", new BindButtonEvent()));
                keys.get(Keyboard.Key.BACKSLASH.getId()).subscribe(subscriber);
            }
            initialized = true;
        }
    }

    @ReceiveEvent
    public void changeFocus(ChangeActiveWidgetEvent event, EntityRef ref) {
        logger.info("changing focus of widget");
        increaseCurrentNum();
        for(WidgetWithOrder widget:widgetsList) {
            logger.info("widget order: "+widget.getOrder());
            logger.info("currentNum: "+currentNum);
            if (widget.getOrder() == currentNum) {
                widget.onGainFocus();
            } else {
                widget.onLoseFocus();
            }
        }

    }
    public void increaseCurrentNum() {
        currentNum++;
        while (!usedNums.contains(currentNum)) {
            currentNum++;
            if (currentNum > maxNum) {
                currentNum = 0;
            }
        }
    }
    public int getNewNextNum() {
        nextNum++;
        maxNum++;
        while (usedNums.contains(nextNum)) {
            nextNum++;
            maxNum++;
        }
        usedNums.add(nextNum);
        return nextNum;
    }
    public void addToUsedNums(int toAdd, WidgetWithOrder widget) {
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
}
