package org.terasology.questing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.questing.utils.ModIcons;

@RegisterComponentSystem
public class QuestingCardFetchSystem implements EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(QuestingCardFetchSystem.class);

    public static String questName = null;
    private static String goal = null;
    public static String friendlyGoal = null;
    private static String amount = null;
    private static Integer currentAmount = 1;

    @Override
    public void initialise() {
        ModIcons.loadIcons();
    }

    @Override
    public void shutdown() {}

    @ReceiveEvent(components = {InventoryComponent.class}, priority = EventPriority.PRIORITY_HIGH)
    public void onReceiveItem(ReceiveItemEvent event, EntityRef entity) {
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);
        String stackID = item.stackId;
        logger.info("Picked up item with id " + stackID);

        if(goal != null) {
            if(stackID.equals(goal)) {
                Integer amounts = Integer.parseInt(amount);

                if(!currentAmount.equals(amounts)) {
                    currentAmount += 1;
                    logger.info("You have gotten " + currentAmount + " blocks.");
                } else {
                    resetQuest();
                    logger.info("Quest finished! Quest goal is now {}", friendlyGoal);
                }
            }
        }
    }

    @ReceiveEvent(components = {QuestingCardFetchComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        QuestingCardFetchComponent questingCard = entity.getComponent(QuestingCardFetchComponent.class);

        questName = questingCard.questName;
        goal = questingCard.goal;
        friendlyGoal = questingCard.friendlyGoal;
        amount = questingCard.amount;

        logger.info("Quest is now active! The quest is {}", questName);
    }

    public static void resetQuest() {
        questName = null;
        goal = null;
        friendlyGoal = null;
        amount = null;
        currentAmount = 1;
    }
}
