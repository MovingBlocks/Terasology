package org.terasology.questing;

import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.manager.MessageManager;

public class QuestingCommands implements CommandProvider {

    @Command(shortDescription = "Lists the active quest.")
    public void listActiveQuest() {
        if(QuestingCardFetchSystem.friendlyGoal != null) {
            MessageManager.getInstance().addMessage("The goal of this quest is " + QuestingCardFetchSystem.friendlyGoal, MessageManager.EMessageScope.PRIVATE);
        } else {
            MessageManager.getInstance().addMessage("No active quests! Get a card first.", MessageManager.EMessageScope.PRIVATE);
        }
    }

}
