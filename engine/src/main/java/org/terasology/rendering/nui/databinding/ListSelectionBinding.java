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
package org.terasology.rendering.nui.databinding;

import org.terasology.rendering.nui.widgets.UIList;

/**
 * @author Immortius
 */
public class ListSelectionBinding<T> implements Binding<T> {

    UIList<T> list;

    public ListSelectionBinding(UIList<T> list) {
        this.list = list;
    }

    @Override
    public T get() {
        return list.getSelection();
    }

    @Override
    public void set(T value) {
        list.setSelection(value);
    }
}
