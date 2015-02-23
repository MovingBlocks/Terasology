/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.config;

/**
 * Options that should <b>not</b> be stored in the config
 * file such as command line parameters.
 * @author Martin Steiger
 */
public class TransientConfig {

    private boolean writeSaveGamesEnabled = true;
    //The port that is used for hosting
    private int serverPort = -1;

    private Config config;

    public TransientConfig(Config config) {
        this.config = config;
    }

    /**
     * Enables/disables write access for the storage manager.
     * @return true if save games should be (periodically) stored on the file system
     */
    public boolean isWriteSaveGamesEnabled() {
        return writeSaveGamesEnabled;
    }

    /**
     * @param writeSaveGamesEnabled if save games should be (periodically) stored on the file system
     */
    public void setWriteSaveGamesEnabled(boolean writeSaveGamesEnabled) {
        this.writeSaveGamesEnabled = writeSaveGamesEnabled;
    }

    public int getServerPort() {
        if (serverPort == -1) {
            return config.getNetwork().getServerPort();
        } else {
            return serverPort;
        }
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
