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
package org.terasology.config.flexible.ui;

import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.rendering.nui.UIWidget;

import java.util.Optional;

public abstract class ConstraintWidgetFactory<T, C extends SettingConstraint<T>> {
    private Setting<T> setting;

    protected final T castToT(Object value) {
        return getSetting().getValueClass().cast(value);
    }

    protected final Setting<T> getSetting() {
        return setting;
    }

    @SuppressWarnings({"unchecked"})
    protected C getConstraint() {
        return (C) setting.getConstraint();
    }

    public Optional<UIWidget> buildWidgetFor(Setting<T> setting) {
        this.setting = setting;

        return buildWidget();
    }

    abstract protected Optional<UIWidget> buildWidget();
}
