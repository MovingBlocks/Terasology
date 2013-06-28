package org.terasology.game.types;

import org.terasology.config.ModConfig;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author synopia
 */
public abstract class BaseGameType implements GameType {
    private GameTypeUri uri;

    protected BaseGameType(GameTypeUri uri) {
        this.uri = uri;
    }

    @Override
    public ModConfig defaultModConfig() {
        return null;
    }

    @Override
    public MapGeneratorUri defaultMapGenerator() {
        return null;
    }

    @Override
    public GameTypeUri uri() {
        return uri;
    }
}
