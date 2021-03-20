// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.testScreens;

import com.google.common.collect.ImmutableList;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Set;

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

        newBinding(new TypeInfo<Set<String>>() {});
        newBinding(new TypeInfo<ImmutableList<Integer>>() {});
        newBinding(Boolean[].class);

        newBinding(new TypeInfo<Container>() {});
        newBinding(new TypeInfo<List<Base<Integer>>>() {});

        newBinding(WithFinalFields.class);

        // TODO: Test Queue
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

    public static class Base<T> {
        protected T a;

        public T getA() {
            return a;
        }

        public void setA(T a) {
            this.a = a;
        }

        @Override
        public String toString() {
            return "Base{" +
                       "a=" + a +
                       '}';
        }
    }

    public static class Sub<T> extends Base<T> {
        public T b;

        public Sub(T b) {
            this.b = b;
        }

        @Override
        public String toString() {
            return "Sub{" +
                       "a=" + a +
                       ", b=" + b +
                       '}';
        }
    }

    public static class Container {
        private Base<Boolean> base;

        public Container(Base<Boolean> base) {
            this.base = base;
        }

        @Override
        public String toString() {
            return "Container{" +
                       "base=" + base +
                       '}';
        }
    }

    public static class WithFinalFields {
        public final int finalInt;
        public final float finalFloat;
        public final String finalString;

        public WithFinalFields(int finalInt, float finalFloat, String finalString) {
            this.finalInt = finalInt;
            this.finalFloat = finalFloat;
            this.finalString = finalString;
        }

        @Override
        public String toString() {
            return "WithFinalFields{" +
                       "finalInt=" + finalInt +
                       ", finalFloat=" + finalFloat +
                       ", finalString='" + finalString + '\'' +
                       '}';
        }
    }
}
