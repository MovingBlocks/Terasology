// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.interactions;

import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;

/**
 * Entities with this component will show an UI during interactions.
 *
 */
@API
public class InteractionScreenComponent implements Component {
    public String screen;

}
