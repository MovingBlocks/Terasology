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
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.binds.general.*;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.binds.interaction.FrobButton;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.input.binds.movement.*;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.*;
import org.terasology.logic.behavior.nui.BTEditorButton;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.players.DecreaseViewDistanceButton;
import org.terasology.logic.players.IncreaseViewDistanceButton;
import org.terasology.rendering.nui.editor.binds.NUIEditorButton;
import org.terasology.rendering.nui.editor.binds.NUISkinEditorButton;

public class EventCopier {


    public static Event copyEvent(Event e) {
        if (e instanceof PlaySoundEvent) {
            return e;
        } else if (e instanceof BindButtonEvent) {
            BindButtonEvent originalEvent = (BindButtonEvent) e;
            BindButtonEvent newEvent = createNewBindButtonEvent(originalEvent);
            newEvent.prepare(originalEvent.getId(), originalEvent.getState(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (e instanceof KeyEvent) {
            KeyEvent originalEvent = (KeyEvent) e;
            KeyEvent newEvent = createNewKeyEvent(originalEvent);
            newEvent.setState(originalEvent.getState());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (e instanceof BindAxisEvent) {
            BindAxisEvent originalEvent = (BindAxisEvent) e;
            BindAxisEvent newEvent = createNewBindAxisEvent(originalEvent);
            newEvent.prepare(originalEvent.getId(), originalEvent.getValue(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (e instanceof MouseAxisEvent) {
            MouseAxisEvent originalEvent = (MouseAxisEvent) e;
            MouseAxisEvent newEvent = createNewMouseAxisEvent(originalEvent);
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (e instanceof CameraTargetChangedEvent) {
            CameraTargetChangedEvent originalEvent = (CameraTargetChangedEvent) e;
            return new CameraTargetChangedEvent(originalEvent.getOldTarget(), originalEvent.getNewTarget());
        } else if (e instanceof CharacterMoveInputEvent) {
            CharacterMoveInputEvent originalEvent = (CharacterMoveInputEvent) e;
            return  new CharacterMoveInputEvent(originalEvent.getSequenceNumber(), originalEvent.getPitch(),
                    originalEvent.getYaw(), originalEvent.getMovementDirection(), originalEvent.isRunning(),
                    originalEvent.isCrouching(), originalEvent.isJumpRequested(), originalEvent.getDeltaMs());
        } else if (e instanceof MouseButtonEvent) {
            MouseButtonEvent originalEvent = (MouseButtonEvent) e;
            MouseButtonEvent newEvent = new MouseButtonEvent(originalEvent.getButton(), originalEvent.getState(), originalEvent.getDelta());
            newEvent.setMousePosition(originalEvent.getMousePosition());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (e instanceof MouseWheelEvent) {
            MouseWheelEvent originalEvent = (MouseWheelEvent) e;
            MouseWheelEvent newEvent = new MouseWheelEvent(originalEvent.getMousePosition(), originalEvent.getWheelTurns(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else {
            return null;
        }
    }

    private static void inputEventSetup(InputEvent newEvent, InputEvent originalEvent) {
        newEvent.setTargetInfo(originalEvent.getTarget(),
                originalEvent.getTargetBlockPosition(),
                originalEvent.getHitPosition(),
                originalEvent.getHitNormal());
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
            System.out.println("ERROR!!! Event not Identified: " + originalEvent.toString());
        }
        return newEvent;
    }

    private static KeyEvent createNewKeyEvent(KeyEvent originalEvent) {
        KeyEvent newEvent = null;
        Class c = originalEvent.getClass();

        if (c.equals(KeyDownEvent.class)) {
            newEvent = KeyDownEvent.create(originalEvent.getKey(), originalEvent.getKeyCharacter(), originalEvent.getDelta());
        } else if (c.equals(KeyRepeatEvent.class)) {
            newEvent = KeyRepeatEvent.create(originalEvent.getKey(), originalEvent.getKeyCharacter(), originalEvent.getDelta());
        } else if (c.equals(KeyUpEvent.class)) {
            newEvent = KeyUpEvent.create(originalEvent.getKey(), originalEvent.getKeyCharacter(), originalEvent.getDelta());
        } else {
            //Use logger here
            System.out.println("ERROR!!! Event not Identified");
        }
        return newEvent;
    }

    private static BindAxisEvent createNewBindAxisEvent(BindAxisEvent originalEvent) {
        BindAxisEvent newEvent = null;
        Class c = originalEvent.getClass();

        if (c.equals(ForwardsMovementAxis.class)) {
            newEvent = new ForwardsMovementAxis();
        } else if (c.equals(ForwardsRealMovementAxis.class)) {
            newEvent = new ForwardsRealMovementAxis();
        } else if (c.equals(RotationPitchAxis.class)) {
            newEvent = new RotationPitchAxis();
        } else if (c.equals(RotationYawAxis.class)) {
            newEvent = new RotationYawAxis();
        } else if (c.equals(StrafeMovementAxis.class)) {
            newEvent = new StrafeMovementAxis();
        } else if (c.equals(StrafeRealMovementAxis.class)) {
            newEvent = new StrafeRealMovementAxis();
        } else if (c.equals(VerticalMovementAxis.class)) {
            newEvent = new VerticalMovementAxis();
        } else if (c.equals(VerticalRealMovementAxis.class)) {
            newEvent = new VerticalRealMovementAxis();
        } else {
            //Use logger here
            System.out.println("ERROR!!! Event not Identified");
        }
        return newEvent;
    }

    private static MouseAxisEvent createNewMouseAxisEvent(MouseAxisEvent originalEvent) {
        return MouseAxisEvent.create(originalEvent.getMouseAxis(), originalEvent.getValue(), originalEvent.getDelta());
    }

}
