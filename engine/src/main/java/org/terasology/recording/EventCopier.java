/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.recording;

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.binds.general.*;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.binds.interaction.FrobButton;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.binds.movement.*;
import org.terasology.logic.behavior.nui.BTEditorButton;
import org.terasology.logic.players.DecreaseViewDistanceButton;
import org.terasology.logic.players.IncreaseViewDistanceButton;
import org.terasology.rendering.nui.editor.binds.NUIEditorButton;
import org.terasology.rendering.nui.editor.binds.NUISkinEditorButton;

public class EventCopier {


    public static Event copyEvent (Event e) {
        if(e instanceof PlaySoundEvent) {
            return e;
        } else if ( e instanceof BindButtonEvent) {
            BindButtonEvent originalEvent = (BindButtonEvent) e;
            //printJumpEventData(originalEvent);
            BindButtonEvent newEvent = createNewBindButtonEvent(originalEvent);
            newEvent.prepare(originalEvent.getId(), originalEvent.getState(), originalEvent.getDelta());
            newEvent.setTargetInfo(originalEvent.getTarget(),
                    originalEvent.getTargetBlockPosition(),
                    originalEvent.getHitPosition(),
                    originalEvent.getHitNormal());
            return newEvent;
        } else {
            return null;
        }
    }

    // there must be a better way to do this
    private static BindButtonEvent createNewBindButtonEvent(BindButtonEvent originalEvent) {
        BindButtonEvent newEvent = null;
        Class c = originalEvent.getClass();

        if (c.equals(ChatButton.class)) {
            newEvent = new ChatButton();
        } else if (c.equals(ConsoleButton.class)) {
            newEvent = new ConsoleButton();
        } else if (c.equals(HideHUDButton.class)) {
            newEvent = new HideHUDButton();
        } else if (c.equals(OnlinePlayersButton.class)) {
            newEvent = new OnlinePlayersButton();
        } else if (c.equals(PauseButton.class)) {
            newEvent = new PauseButton();
        } else if (c.equals(ScreenshotButton.class)) {
            newEvent = new ScreenshotButton();
        } else if (c.equals(AttackButton.class)) {
            newEvent = new AttackButton();
        } else if (c.equals(FrobButton.class)) {
            newEvent = new FrobButton();
        } else if (c.equals(UseItemButton.class)) {
            newEvent = new UseItemButton();
        } else if (c.equals(AutoMoveButton.class)) {
            newEvent = new AutoMoveButton();
        } else if (c.equals(BackwardsButton.class)) {
            newEvent = new BackwardsButton();
        } else if (c.equals(CrouchButton.class)) {
            newEvent = new CrouchButton();
        } else if (c.equals(ForwardsButton.class)) {
            newEvent = new ForwardsButton();
        } else if (c.equals(JumpButton.class)) {
            newEvent = new JumpButton();
        } else if (c.equals(LeftStrafeButton.class)) {
            newEvent = new LeftStrafeButton();
        } else if (c.equals(RightStrafeButton.class)) {
            newEvent = new RightStrafeButton();
        } else if (c.equals(ToggleSpeedPermanentlyButton.class)) {
            newEvent = new ToggleSpeedPermanentlyButton();
        } else if (c.equals(ToggleSpeedTemporarilyButton.class)) {
            newEvent = new ToggleSpeedTemporarilyButton();
        } else if (c.equals(BTEditorButton.class)) {
            newEvent = new BTEditorButton();
        } else if (c.equals(DecreaseViewDistanceButton.class)) {
            newEvent = new DecreaseViewDistanceButton();
        } else if (c.equals(IncreaseViewDistanceButton.class)) {
            newEvent = new IncreaseViewDistanceButton();
        } else if (c.equals(NUIEditorButton.class)) {
            newEvent = new NUIEditorButton();
        } else if (c.equals(NUISkinEditorButton.class)) {
            newEvent = new NUISkinEditorButton();
        }  else {
            //Use logger here
            System.out.println("ERROR!!! Event not Identified");
        }
        return newEvent;
    }

    private static void printJumpEventData(JumpButton e) {
        System.out.println("Jump data:");
        System.out.println("Delta: " + e.getDelta());
        System.out.println("HitNormal: " + e.getHitNormal());
        System.out.println("HitPosition: " + e.getHitPosition());
        System.out.println("Id: " + e.getId());
        System.out.println("State: " + e.getState());
        System.out.println("Target: " + e.getTarget());
        System.out.println("TargetBlockPosition: " + e.getTargetBlockPosition());
    }
}
