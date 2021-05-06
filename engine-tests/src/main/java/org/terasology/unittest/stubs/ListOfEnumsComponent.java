// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Side;

import java.util.ArrayList;
import java.util.List;

public class ListOfEnumsComponent implements Component {
    public List<Side> elements = new ArrayList<>();
}
