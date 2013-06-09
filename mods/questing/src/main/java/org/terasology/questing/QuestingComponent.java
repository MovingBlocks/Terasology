package org.terasology.questing;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;

import java.util.ArrayList;
import java.util.List;

public class QuestingComponent implements Component {
    //Config options
    public List<String> quests = Lists.newArrayList(); //The list that has all of the quest names
    public List<String> friendlyGoals = Lists.newArrayList();               /* The friendly goal for the user to see */
    public List<String> goals = Lists.newArrayList(); //The list that has all of the goals stored
    public String onAchieve = null;          /* What happens when the user has gotten the item */

    public QuestingComponent() {}
}
