/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.potions;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIImage;

import javax.vecmath.Vector2f;

/**
 * For Status Effects that don't affect the hearts
 * <p/>
 * TODO get rid of this.. HUD window
 */
public class UIBuff extends UIDisplayContainer {
    private final UIImage speedbuff;
    private final UIImage curedbuff;
    protected EntityRef entity;
    protected EntityManager entityManager;


    public UIBuff() {
        setSize(new Vector2f(280f, 28f));

        // Create speed buff icon.
        speedbuff = new UIImage(Assets.getTexture("engine:buffs"));
        speedbuff.getTextureSize().set(new Vector2f(16f / 256f, 16f / 256f));
        speedbuff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));
        speedbuff.setSize(new Vector2f(18f, 18f));
        speedbuff.setPosition(new Vector2f(18f, 18f));
        // Create speed buff icon.
        curedbuff = new UIImage(Assets.getTexture("engine:buffs"));
        curedbuff.getTextureSize().set(new Vector2f(16f / 256f, 16f / 256f));
        curedbuff.getTextureOrigin().set(new Vector2f(16f / 256f, 0.0f / 256f));
        curedbuff.setSize(new Vector2f(18f, 18f));
        curedbuff.setPosition(new Vector2f(50f, 18f));

        addDisplayElement(curedbuff);
        addDisplayElement(speedbuff);
    }

    @Override
    public void update() {
        //TODO do this with events instead.
        super.update();
        SpeedBoostComponent speed = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(SpeedBoostComponent.class);
        CharacterMovementComponent charmov = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterMovementComponent.class);
        CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CuredComponent.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        //Speed Boost
        for (EntityRef entity : entityManager.iteratorEntities(SpeedBoostComponent.class)) {
            if (speed.speedBoostDuration >= 1) {
                speedbuff.setVisible(true);
                speedbuff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));

            } else speedbuff.setVisible(false);

        }
        //Poison Immunity
        for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
            if (cured.cureDuration >= 1) {
                curedbuff.setVisible(true);
                curedbuff.getTextureOrigin().set(new Vector2f(16f / 256f, 0.0f / 256f));
            } else curedbuff.setVisible(false);
        }
    }
}

