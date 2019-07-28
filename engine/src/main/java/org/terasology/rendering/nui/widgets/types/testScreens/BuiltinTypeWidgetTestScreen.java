/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.nui.widgets.types.testScreens;

import com.google.common.collect.ImmutableList;
import org.terasology.reflection.TypeInfo;

import java.util.List;

public class BuiltinTypeWidgetTestScreen extends TypeWidgetTestScreen {
    @Override
    protected void addWidgets() {
        newBinding(Boolean.class);

        newBinding(byte.class);
        newBinding(short.class);
        newBinding(int.class);
        newBinding(long.class);
        newBinding(float.class);
        newBinding(double.class);

        newBinding(String.class);

        newBinding(TestEnum.class);

        newBinding(new TypeInfo<List<String>>() {});
        newBinding(new TypeInfo<ImmutableList<Integer>>() {});
        newBinding(Boolean[].class);
    }

    private enum TestEnum {
        NO_DISPLAY(),
        DISPLAY_NAME("With Display Name"),
        TRANSLATED_DISPLAY_NAME("${engine:menu#warning}");


        private String displayName;

        TestEnum() {this(null);}

        TestEnum(String displayName) {this.displayName = displayName;}


        @Override
        public String toString() {
            return displayName != null ? displayName : super.toString();

        }
    }
}
