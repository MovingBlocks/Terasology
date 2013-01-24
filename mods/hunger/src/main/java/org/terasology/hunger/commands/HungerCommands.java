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

import org.terasology.game.CoreRegistry;
import org.terasology.hunger.HungerComponent;
import org.terasology.hunger.events.ContentmentChangedEvent;
import org.terasology.hunger.events.FullContentmentEvent;
import org.terasology.hunger.events.NoContentmentEvent;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.players.LocalPlayer;


/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class HungerCommands implements CommandProvider {

    @Command(shortDescription = "Restores your Contentment to max")
    public void hunger() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getCharacterEntity().getComponent(HungerComponent.class);
        hunger.currentContentment = hunger.maxContentment;
        localPlayer.getCharacterEntity().send(new FullContentmentEvent(localPlayer.getCharacterEntity(), hunger.maxContentment));
        localPlayer.getCharacterEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Restores your Contentment by an amount")
    public void hunger(@CommandParam(name = "amount") int amount) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getCharacterEntity().getComponent(HungerComponent.class);
        hunger.currentContentment = amount;
        if (hunger.currentContentment >= hunger.maxContentment) {
            hunger.currentContentment = hunger.maxContentment;
            localPlayer.getCharacterEntity().send(new FullContentmentEvent(localPlayer.getCharacterEntity(), hunger.maxContentment));
        } else if (hunger.currentContentment <= 0) {
            hunger.currentContentment = 0;
            localPlayer.getCharacterEntity().send(new NoContentmentEvent(localPlayer.getCharacterEntity(), hunger.maxContentment));
        } else {
            localPlayer.getCharacterEntity().send(new ContentmentChangedEvent(localPlayer.getCharacterEntity(), hunger.currentContentment, hunger.maxContentment));
        }
        localPlayer.getCharacterEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Set Max Contentment")
    public void setMaxContentment(@CommandParam(name = "max") int max) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getCharacterEntity().getComponent(HungerComponent.class);
        hunger.maxContentment = max;
        hunger.currentContentment = hunger.maxContentment;
        localPlayer.getCharacterEntity().send(new FullContentmentEvent(localPlayer.getCharacterEntity(), hunger.maxContentment));
        localPlayer.getCharacterEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Set hunger deregen rate")
    public void setHungerRate(@CommandParam(name = "rate") float rate) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getCharacterEntity().getComponent(HungerComponent.class);
        hunger.deregenRate = rate;
        localPlayer.getCharacterEntity().saveComponent(hunger);
    }

    @Command(shortDescription = "Show your Hunger")
    public void showHunger() {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        HungerComponent hunger = localPlayer.getCharacterEntity().getComponent(HungerComponent.class);
        MessageManager.getInstance().addMessage("Your hunger:" + hunger.currentContentment + " max:" + hunger.maxContentment + " deregen:" + hunger.deregenRate + " partDeregen:" + hunger.partialDeregen);
    }
}