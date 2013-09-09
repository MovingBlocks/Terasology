/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.questing.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.questing.components.QuestJournalComponent;
import org.terasology.questing.gui.UIScreenQuest;

@RegisterSystem
public class QuestJournalAction implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(QuestJournalAction.class);

    @Override
    public void initialise() {
        CoreRegistry.get(GUIManager.class).registerWindow("journal", UIScreenQuest.class);
        logger.info("Questing journal loaded.");
    }

    @Override
    public void shutdown() {

    }

    /**
     * This is used for UI calls in the questing journal.
     */
    @ReceiveEvent(components = {QuestJournalComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        QuestJournalComponent questJournal = entity.getComponent(QuestJournalComponent.class);

        if (questJournal != null) {
            //logger.info("Journal used.");

            if (UIScreenQuest.qGoal != null && UIScreenQuest.qName != null) {
                if (QuestingCardFetchSystem.questName != null && QuestingCardFetchSystem.friendlyGoal != null) {
                    //Update the questing journal.
                    UIScreenQuest.qName.setText(QuestingCardFetchSystem.questName);
                    UIScreenQuest.qGoal.setText(QuestingCardFetchSystem.friendlyGoal);
                    //logger.info("Questing info updated.");
                } else {
                    UIScreenQuest.qName.setText("Find a quest card and use it!");
                    //logger.info("There is no active quest, called as an update.");
                }
            } else {
                if (QuestingCardFetchSystem.questName != null) {
                    UIScreenQuest.questName = QuestingCardFetchSystem.questName;
                    UIScreenQuest.questGoal = QuestingCardFetchSystem.friendlyGoal;
                    //logger.info("The quest infos were just set, for the first time.");
                } else {
                    UIScreenQuest.questName = "Find a quest card and use it!";
                    //logger.info("There is no active quest, and this was just set for the first time.");
                }
            }

            CoreRegistry.get(GUIManager.class).openWindow("journal");
        }
    }
}
