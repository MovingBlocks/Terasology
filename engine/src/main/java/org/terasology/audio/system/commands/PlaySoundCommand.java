/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.audio.system.commands;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;

import javax.vecmath.Vector3f;

/**
 * @author Immortius, Limeth
 */
@RegisterSystem
public class PlaySoundCommand extends Command {
    @In
    private LocalPlayer localPlayer;
    @In
    private AudioManager audioManager;

    public PlaySoundCommand() {
        super("playSound", false, "Plays a sound", "Plays a specified sound, or engine:dig if no sound is specified.");
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("sound", String.class, false),
                CommandParameter.single("xOffset", Float.class, false),
                CommandParameter.single("yOffset", Float.class, false),
                CommandParameter.single("zOffset", Float.class, false)
        };
    }

    public String execute(EntityRef sender, String nullableSound, Float xOffset, Float yOffset, Float zOffset) {
        Vector3f position = localPlayer.getPosition();
        String soundName = nullableSound != null ? nullableSound : "engine:dig";
        StaticSound sound = Assets.getSound(soundName);

        if(sound == null) {
            return "Sound '" + soundName + "' not found.";
        }

        position.x += xOffset != null ? xOffset : 0;
        position.y += yOffset != null ? yOffset : 0;
        position.z += zOffset != null ? zOffset : 0;
        audioManager.playSound(sound, position);

        return "Playing sound '" + soundName + "'.";
    }

    //TODO Add the suggest method
}
