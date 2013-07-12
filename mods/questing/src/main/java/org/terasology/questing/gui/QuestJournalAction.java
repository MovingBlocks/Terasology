package org.terasology.questing.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.GUIManager;
import org.terasology.questing.QuestingCardFetchSystem;

@RegisterComponentSystem
public class QuestJournalAction implements EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(QuestJournalAction.class);

    @Override
    public void initialise() {
        CoreRegistry.get(GUIManager.class).registerWindow("journal", UIScreenQuest.class);
        logger.info("Questing journal loaded.");
    }

    @Override
    public void shutdown() {}

    @ReceiveEvent(components = {QuestJournalComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        QuestJournalComponent questJournal = entity.getComponent(QuestJournalComponent.class);

        if(questJournal != null) {
            logger.info("Journal used.");

            if(UIScreenQuest.qGoal != null && UIScreenQuest.qName != null) {
                if(QuestingCardFetchSystem.questName != null && QuestingCardFetchSystem.friendlyGoal != null) {
                    UIScreenQuest.qName.setText(QuestingCardFetchSystem.questName);
                    UIScreenQuest.qGoal.setText(QuestingCardFetchSystem.friendlyGoal);
                    logger.info("Questing info updated.");
                } else {
                    UIScreenQuest.qName.setText("Find a quest card and use it!");
                    logger.info("There is no active quest, called as an update.");
                }
            } else {
                if(QuestingCardFetchSystem.questName != null) {
                    UIScreenQuest.questName = QuestingCardFetchSystem.questName;
                    UIScreenQuest.questGoal = QuestingCardFetchSystem.friendlyGoal;
                    logger.info("The quest infos were just set, for the first time.");
                } else {
                    UIScreenQuest.questName = "Find a quest card and use it!";
                    logger.info("There is no active quest, and this was just set for the first time.");
                }
            }

            CoreRegistry.get(GUIManager.class).openWindow("journal");
        }
    }
}
