package org.terasology.game.modes;

import org.terasology.rendering.world.WorldRenderer;


/**
 * @author Anton Kireev
 * @version 0.1
 */
public interface IGameMode {
    public void updateTimeAccumulator(long currentTime, long startTime);
    public void init();
    public void update();
    public void render();
    public void processKeyboardInput();
    public void processMouseInput();
    
    public WorldRenderer getActiveWorldRenderer();
    public void updatePlayerInput();
}
