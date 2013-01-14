/*
 * Copyright 2012 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.hunger.commands;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.hunger.events.*;
import org.terasology.hunger.*;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.manager.MessageManager.EMessageScope;
import org.terasology.game.CoreRegistry;
import org.terasology.game.types.GameType;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.commands.*;


/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 * 
 */
public class HungerCommands  implements CommandProvider {

    @Command(shortDescription = "Restores your Contentment to max")
    public void hunger() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getEntity().getComponent(HungerComponent.class);
        hunger.currentContentment = hunger.maxContentment;
        localPlayer.getEntity().send(new FullContentmentEvent(localPlayer.getEntity(), hunger.maxContentment));
        localPlayer.getEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Restores your Contentment by an amount")
    public void hunger(@CommandParam(name = "amount") int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getEntity().getComponent(HungerComponent.class);
        hunger.currentContentment = amount;
        if (hunger.currentContentment >= hunger.maxContentment) {
        	hunger.currentContentment = hunger.maxContentment;
        	localPlayer.getEntity().send(new FullContentmentEvent(localPlayer.getEntity(), hunger.maxContentment));
        } else if (hunger.currentContentment <= 0) {
        	hunger.currentContentment = 0;
        	localPlayer.getEntity().send(new NoContentmentEvent(localPlayer.getEntity(), hunger.maxContentment));
        } else {
        	localPlayer.getEntity().send(new ContentmentChangedEvent(localPlayer.getEntity(), hunger.currentContentment, hunger.maxContentment));
        }
        localPlayer.getEntity().saveComponent(hunger);
    }
    
    @Command(shortDescription = "Set Max Contentment")
    public void setMaxContentment(@CommandParam(name = "max") int max) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getEntity().getComponent(HungerComponent.class);
        hunger.maxContentment = max;
        hunger.currentContentment = hunger.maxContentment;
    	localPlayer.getEntity().send(new FullContentmentEvent(localPlayer.getEntity(), hunger.maxContentment));
        localPlayer.getEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Set hunger deregen rate")
    public void setHungerRate(@CommandParam(name = "rate") float rate) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getEntity().getComponent(HungerComponent.class);
        hunger.deregenRate = rate;
        localPlayer.getEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Show your Hunger")
    public void showHunger() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getEntity().getComponent(HungerComponent.class);
        MessageManager.getInstance().addMessage("Your hunger:" + hunger.currentContentment + " max:" + hunger.maxContentment + " deregen:" + hunger.deregenRate + " partDeregen:" + hunger.partialDeregen, EMessageScope.PRIVATE);
    }
}