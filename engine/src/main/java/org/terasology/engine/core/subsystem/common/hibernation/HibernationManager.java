/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.engine.core.subsystem.common.hibernation;

import org.terasology.gestalt.module.sandbox.API;

/**
 *
 */
@API
public class HibernationManager {
    private boolean hibernationAllowed = true;
    private boolean hibernating;

    public boolean isHibernationAllowed() {
        return hibernationAllowed;
    }

    public void setHibernationAllowed(boolean hibernationAllowed) {
        this.hibernationAllowed = hibernationAllowed;
    }

    public boolean isHibernating() {
        return hibernating;
    }

    void setHibernating(boolean hibernating) {
        this.hibernating = hibernating;
    }
}
