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
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.List;

/**
 * @author Immortius
 */
public class UICommandEntry extends UIText {

    private Binding<List<String>> commandHistory = new DefaultBinding<List<String>>(Lists.<String>newArrayList());
    private int index;
    private TabCompletionEngine tabCompletionEngine;

    @Override
    public void onKeyEvent(KeyEvent event) {
        if (event.isDown()) {
            switch (event.getKey().getId()) {
                case Keyboard.KeyId.UP:
                    if (index > 0) {
                        index--;
                        if (getCommandHistory().size() > index) {
                            setText(getCommandHistory().get(index));
                        }
                        setCursorPosition(getText().length());
                    }
                    event.consume();
                    break;
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
                    event.consume();
                    break;
                case Keyboard.KeyId.TAB:
                    if (tabCompletionEngine != null) {
                        setText(tabCompletionEngine.complete(getText()));
                        setCursorPosition(getText().length());
                        event.consume();
                    }
                    break;
                case Keyboard.KeyId.ENTER:
                    super.onKeyEvent(event);
                    setText("");
                    index = getCommandHistory().size();
                    break;
                default:
                    super.onKeyEvent(event);
            }
        }
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
