package org.terasology.questing;

import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.manager.MessageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class QuestingCommands implements CommandProvider {

    @Command(shortDescription = "Lists all of the quests in the system.")
    public void listAllQuests() {
        MessageManager.getInstance().addMessage("All of the quests are: " + QuestingSystem.questList, MessageManager.EMessageScope.PRIVATE);
    }

    @Command(shortDescription = "Lists the current quest.")
    public void listActiveQuest() {
        MessageManager.getInstance().addMessage("The active quest is " + QuestingSystem.goals[QuestingSystem.currentQuest]);
    }

}
