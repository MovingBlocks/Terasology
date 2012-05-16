package org.terasology.rendering.gui.components;

import org.lwjgl.input.Keyboard;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.SpeedBoostComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
/*
 *   For Status Effects that don't affect the hearts
 */
public class UIBuff extends UIDisplayContainer {
    private final UIGraphicsElement _buff;
    protected EntityRef entity;
    protected EntityManager entityManager;


    public UIBuff() {
        setSize(new Vector2f(280f, 28f));

    // Create speed buff icon.
        _buff = new UIGraphicsElement(AssetManager.loadTexture("engine:buffs"));
        _buff.getTextureSize().set(new Vector2f(16f / 256f, 16f / 256f));
        _buff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));
        _buff.setSize(new Vector2f(18f, 18f));
        _buff.setPosition(new Vector2f(18f, 18f));

        addDisplayElement(_buff);
    }
    @Override
    public void update() {

        super.update();
        SpeedBoostComponent speed = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(SpeedBoostComponent.class);
        CharacterMovementComponent charmov = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CharacterMovementComponent.class);

        entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.iteratorEntities(SpeedBoostComponent.class)){
        if (speed.speedBoostDuration >=1){
            _buff.setVisible(true);
            if (charmov.isRunning){
                _buff.getTextureOrigin().set(new Vector2f(16f / 256f, 16f / 256f));

            }
            else _buff.getTextureOrigin().set(new Vector2f(0.0f / 256f, 16f / 256f));

        }
        else _buff.setVisible(false);

        }
    }
}

