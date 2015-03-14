/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.rendering.nui.layers.mainMenu;

import java.util.ArrayList;
import java.util.List;

import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class CombinedListBinding<T> extends ReadOnlyBinding<List<T>> {

    private final List<T> servers = new ArrayList<>();

    private final List<T> list1;
    private final List<T> list2;

    /**
     * @param locals
     */
    public CombinedListBinding(List<T> list1, List<T> list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    @Override
    public List<T> get() {
        servers.clear();
        servers.addAll(list1);
        servers.addAll(list2);
        return servers;
    }
}
