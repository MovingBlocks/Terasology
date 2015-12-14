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
package org.terasology.rendering.nui.widgets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.Objects;

/**
 */
public class UITextEntry<T> extends UIText {
    private static final Logger logger = LoggerFactory.getLogger(UITextEntry.class);
    
    private Binding<T> value = new DefaultBinding<>();
    private Binding<String> stringValue = new DefaultBinding<>("");
    private Parser<T> parser;
    private Formatter<T> formatter = new ToStringFormatter<>();

    public UITextEntry() {
        bindText(stringValue);
    }

    public UITextEntry(String id) {
        super(id);
        bindText(stringValue);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isFocused()) {
            stringValue.set(formatter.toString(value.get()));
        }
        super.onDraw(canvas);
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
        try {
            T result = parser.parse(stringValue.get());
            if (result != null) {
                value.set(result);
            }
        } catch (IllegalArgumentException e) {
            // ignore
            logger.debug("Failed to parse text value", e);
        }
    }

    public void bindValue(Binding<T> binding) {
        value = binding;
        stringValue.set(formatter.toString(value.get()));
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T val) {
        value.set(val);
    }

    public void setParser(Parser<T> parser) {
        this.parser = parser;
    }

    public void setFormatter(Formatter<T> formatter) {
        this.formatter = formatter;
    }

    public interface Formatter<T> {

        String toString(T value);
    }

    public interface Parser<T> {

        T parse(String value);
    }

    public static class ToStringFormatter<T> implements Formatter<T> {

        @Override
        public String toString(T value) {
            return Objects.toString(value);
        }
    }
}
