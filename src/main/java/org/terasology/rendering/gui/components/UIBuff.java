package org.terasology.rendering.gui.components;

import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.components.CuredComponent;
import org.terasology.components.SpeedBoostComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/*
*   For Status Effects that don't affect the hearts
*/
public class UIBuff extends UIDisplayContainer {
    private final UIGraphicsElement _speedbuff;
    private final UIGraphicsElement _curedbuff;
    protected EntityRef entity;
    protected EntityManager entityManager;


    public UIBuff() {
        setSize(new Vector2f(280f, 28f));

        // Create speed buff icon.
        _speedbuff = new UIGraphicsElement(AssetManager.loadTexture("engine:buffs"));
        _speedbuff.getTextureSize().set(new Vector2f(16f / 256f, 16f / 256f));
        _speedbuff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));
        _speedbuff.setSize(new Vector2f(18f, 18f));
        _speedbuff.setPosition(new Vector2f(18f, 18f));
        // Create speed buff icon.
        _curedbuff = new UIGraphicsElement(AssetManager.loadTexture("engine:buffs"));
        _curedbuff.getTextureSize().set(new Vector2f(16f / 256f, 16f / 256f));
        _curedbuff.getTextureOrigin().set(new Vector2f(16f / 256f, 0.0f / 256f));
        _curedbuff.setSize(new Vector2f(18f, 18f));
        _curedbuff.setPosition(new Vector2f(50f, 18f));

        addDisplayElement(_curedbuff);
        addDisplayElement(_speedbuff);
    }

    @Override
    public void update() {

        super.update();
        SpeedBoostComponent speed = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(SpeedBoostComponent.class);
        CharacterMovementComponent charmov = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CharacterMovementComponent.class);
        CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CuredComponent.class);
        entityManager = CoreRegistry.get(EntityManager.class);
        //Speed Boost
        for (EntityRef entity : entityManager.iteratorEntities(SpeedBoostComponent.class)) {
            if (speed.speedBoostDuration >= 1) {
                _speedbuff.setVisible(true);
                if (charmov.isRunning) {
                    _speedbuff.getTextureOrigin().set(new Vector2f(16f / 256f, 16f / 256f));
                } else _speedbuff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));

            } else _speedbuff.setVisible(false);

        }
        //Poison Immunity
        for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
            if (cured.cureDuration >= 1) {
                _curedbuff.setVisible(true);
                _curedbuff.getTextureOrigin().set(new Vector2f(16f / 256f, 0.0f / 256f));
            } else _curedbuff.setVisible(false);
        }
    }
}

