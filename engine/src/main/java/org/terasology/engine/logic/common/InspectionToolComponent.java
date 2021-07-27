// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.common;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;

/**
 * Component of the inspection tool which can be used to view the json data of
 * entities. This component can also be used to test external references.
 * 
 */
public class InspectionToolComponent implements Component {
    @Replicate
    public EntityRef inspectedEntity = EntityRef.NULL;
}
