// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

import com.google.common.collect.Lists;
import org.terasology.input.Keyboard;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.widgets.UIText;

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
