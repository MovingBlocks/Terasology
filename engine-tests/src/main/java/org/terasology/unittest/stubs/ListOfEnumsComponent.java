// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import com.google.common.collect.Lists;
import org.terasology.engine.math.Side;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;

public class ListOfEnumsComponent implements Component<ListOfEnumsComponent> {
    public List<Side> elements = new ArrayList<>();

    @Override
    public void copy(ListOfEnumsComponent other) {
        this.elements = Lists.newArrayList(other.elements);
    }
}
