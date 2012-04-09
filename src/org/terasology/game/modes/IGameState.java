/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

package org.terasology.game.modes;

import org.terasology.entitySystem.EntityManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;

/**
 * @author Anton Kireev <adeon.k87@gmail.com>
 * @version 0.1
 */
public interface IGameState {
    public void init();
    public void dispose();

    public void activate();
    public void deactivate();

    public void update(float delta);
    public void render();

    public void processKeyboardInput();
    public void processMouseInput();
    
    public EntityManager getEntityManager();

    // Ability to open an extra screen on top
    // TODO: This doesn't seem like the right place
    public void openScreen(UIDisplayElement screen);
    public void closeScreen();

}
