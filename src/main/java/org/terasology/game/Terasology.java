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
package org.terasology.game;

import org.slf4j.LoggerFactory;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.logic.manager.PathManager;

/**
 * The heart and soul of Terasology.
 * <p/>
 * TODO: Create a function returns the number of generated worlds
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Kireev   Anton   <adeon.k87@gmail.com>
 */
public final class Terasology {
    private Terasology() {
    }

    public static void main(String[] args) {
        try {
            PathManager.getInstance().determineRootPath(true);
            TerasologyEngine engine = new TerasologyEngine();
            engine.init();
            engine.run(new StateMainMenu());
            engine.dispose();
        } catch (Throwable t) {
            LoggerFactory.getLogger(Terasology.class).error("Uncaught Exception", t);
        }
        System.exit(0);
    }
}
