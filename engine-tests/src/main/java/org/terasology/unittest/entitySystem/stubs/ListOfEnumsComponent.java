// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.entitySystem.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Side;

import java.util.ArrayList;
import java.util.List;

public class ListOfEnumsComponent implements Component {
    public List<Side> elements = new ArrayList<>();
}
