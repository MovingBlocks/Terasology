// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.nui.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.List;

public class ListOfObjectComponent implements Component {
    public String shortName;
    public List<SubElement> elements = new ArrayList<>();

    @MappedContainer
    public static class SubElement {
        public String id;
        public String type;
    }
}
