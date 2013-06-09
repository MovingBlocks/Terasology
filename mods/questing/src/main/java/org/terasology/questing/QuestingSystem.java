package org.terasology.questing;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.BlockDroppedEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RegisterComponentSystem
public class QuestingSystem implements EventHandlerSystem {

    private static final Logger logger = LoggerFactory.getLogger(QuestingSystem.class);

    //The following 2 lines are necessary for some reason because when they are removed I get weird errors
    private EntityRef entity = EntityRef.NULL;
    private InventoryComponent inv = entity.getComponent(InventoryComponent.class);

    public static String questList; //One list to hold them all,
    private static String goalList; //A list for all of the goals

    public static String[] goals; //A list to hold all of the goals, in a non-friendly way, so that the engine can read them

    public static int currentQuest = 0; //An int to hold what quest the player is on

    @Override
    public void initialise() {
        Collection<Prefab> prefabs = CoreRegistry.get(PrefabManager.class).listPrefabs(QuestingComponent.class);
        logger.info("Grabbed all of the quest prefabs - got {}", prefabs);

        //This is to get all of the prefabs and assign their data to variables.
        //I know that this is overwriting each time, it is a crude hack.
        //TODO: Fix crude hack
        for(Prefab prefab : prefabs) {
            QuestingComponent questingComponent = prefab.getComponent(QuestingComponent.class);
            questList = questingComponent.quests.toString(); //And in the darkness bind them.
            goalList = questingComponent.goals.toString();
        }

        String blockType = goalList.substring(1, goalList.length() - 1);

        goals = blockType.split(", "); //Splits the goals into different parts

        logger.info("Quest list updated to {}", questList);
    }

    @Override
    public void shutdown() {}

    @ReceiveEvent(components = {InventoryComponent.class})
    public void onReceiveItem(ReceiveItemEvent event, EntityRef entity) {
        //logger.info("Receive Item event fired with receiveItem.");
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);

        String stackID = item.stackId;
        //logger.info("Item name is: " + stackID);

        //TODO: ADD A CHECK HERE TO SEE IF THAT EXISTS
        if(stackID.equals(goals[currentQuest])) {
            logger.info("Quest finished!"); //TODO: Set up a UI alert for finished quest
            currentQuest += 1;
        }
    }
}
