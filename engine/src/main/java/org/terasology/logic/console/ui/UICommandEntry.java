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
package org.terasology.logic.console.ui;

import com.google.common.collect.Lists;
import org.terasology.input.Keyboard;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.List;

/**
 */
public class UICommandEntry extends UIText {

    private Binding<List<String>> commandHistory = new DefaultBinding<>(Lists.<String>newArrayList());
    private int index;
    private TabCompletionEngine tabCompletionEngine;

    public UICommandEntry() {
        subscribe((int oldPosition, int newPosition) -> {
            if (tabCompletionEngine == null) {
                return;
            }

            tabCompletionEngine.reset();
        });
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            
            if (id != Keyboard.KeyId.TAB && tabCompletionEngine != null) {
                tabCompletionEngine.reset();
            }
            
            switch (id) {
                case Keyboard.KeyId.UP:
                    if (index > 0) {
                        index--;
                        if (getCommandHistory().size() > index) {
                            setText(getCommandHistory().get(index));
                        }
                        setCursorPosition(getText().length());
                    }
                    return true;
                case Keyboard.KeyId.DOWN:
                    if (index < getCommandHistory().size()) {
                        index++;
                        if (index == getCommandHistory().size()) {
                            setText("");
                        } else {
                            setText(getCommandHistory().get(index));
                            setCursorPosition(getText().length());
                        }
                    }
                    return true;
                case Keyboard.KeyId.TAB:
                    if (tabCompletionEngine != null) {
                        setText(tabCompletionEngine.complete(getText()));
                        setCursorPosition(getText().length(), true, false);
                        return true;
                    }
                    break;
                case Keyboard.KeyId.ENTER:
                    boolean consumed = super.onKeyEvent(event);
                    setText("");
                    index = getCommandHistory().size();
                    return consumed;
                default:
                    return super.onKeyEvent(event);
            }
        }
        return false;
    }

    public void bindCommandHistory(Binding<List<String>> binding) {
        commandHistory = binding;
        index = commandHistory.get().size();
    }

    public List<String> getCommandHistory() {
        return commandHistory.get();
    }

    public void setCommandHistory(List<String> val) {
        commandHistory.set(val);
    }

    public TabCompletionEngine getTabCompletionEngine() {
        return tabCompletionEngine;
    }

    public void setTabCompletionEngine(TabCompletionEngine tabCompletionEngine) {
        this.tabCompletionEngine = tabCompletionEngine;
    }
}
