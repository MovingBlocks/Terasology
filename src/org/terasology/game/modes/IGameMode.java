package org.terasology.game.modes;

import org.terasology.rendering.world.WorldRenderer;


/**
 *
 * @author Anton Kireev
 */
public interface IGameMode {
   // public double _timeAccumulator = 0;
    public void updateTimeAccumulator(long currentTime, long startTime);
    public void init();
    public void update();
    public void render();
    public void processKeyboardInput();
    public void processMouseInput();
    
    //temporary
    public WorldRenderer getActiveWorldRenderer();
    public void updatePlayerInput();
}
