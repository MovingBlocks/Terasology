package org.terasology.ligthandshadow.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.HealthComponent;
import org.terasology.config.ModConfig;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.types.BaseGameType;
import org.terasology.game.types.GameTypeUri;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author synopia
 */
public class LASGameType extends BaseGameType {
    private final static Logger logger = LoggerFactory.getLogger(LASGameType.class);

    public LASGameType() {
        super(new GameTypeUri("las:game"));
    }

    @Override
    public String name() {
        return "Light And Shadow";
    }

    @Override
    public void initialize() {
        logger.info("Welcome to Light And Shadow");
    }

    @Override
    public ModConfig defaultModConfig() {
        ModConfig config = new ModConfig();
        config.addMod("core");
        config.addMod("pathfinding");
        config.addMod("hunger");
        return config;
    }

    @Override
    public MapGeneratorUri defaultMapGenerator() {
        return new MapGeneratorUri("las:mapgen");
    }

    @Override
    public void onCreateInventoryHook(UIWindow parent) {
    }

    @Override
    public void onPlayerDamageHook(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        if (health.currentHealth <= 0) return;

        health.timeSinceLastDamage = 0;
        health.currentHealth -= damageAmount;
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent(instigator, health.maxHealth));
        } else {
            entity.send(new HealthChangedEvent(instigator, health.currentHealth, health.maxHealth));
        }
        entity.saveComponent(health);
    }
}
