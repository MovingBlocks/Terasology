// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common.hibernation;

import org.terasology.context.annotation.API;


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
