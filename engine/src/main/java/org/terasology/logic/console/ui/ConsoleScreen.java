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

import org.terasology.input.MouseInput;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.Message;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.List;

/**
 */
public class ConsoleScreen extends CoreScreenLayer {

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    private UICommandEntry commandLine;

    private boolean welcomePrinted;

    private InteractionListener screenListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT && commandLine != null) {
                getManager().setFocus(commandLine);
            }
            return true;
        }
    };

    @Override
    public void initialise() {
        final ScrollableArea scrollArea = find("scrollArea", ScrollableArea.class);
        scrollArea.moveToBottom();

        commandLine = find("commandLine", UICommandEntry.class);
        getManager().setFocus(commandLine);
        commandLine.setTabCompletionEngine(new CyclingTabCompletionEngine(console, localPlayer));
        commandLine.bindCommandHistory(new ReadOnlyBinding<List<String>>() {
            @Override
            public List<String> get() {
                return console.getPreviousCommands();
            }
        });
        commandLine.subscribe(widget -> {
            console.execute(commandLine.getText(), localPlayer.getClientEntity());
            commandLine.setText("");
            scrollArea.moveToBottom();
        });

        final UIText history = find("messageHistory", UIText.class);
        history.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                StringBuilder messageList = new StringBuilder();
                for (Message message : console.getMessages()) {
                    messageList.append(FontColor.getColored(message.getMessage(), message.getType().getColor()));
                    messageList.append(Console.NEW_LINE);
                }
                return messageList.toString();
            }
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();
        getManager().setFocus(commandLine);

        if (!welcomePrinted) {
            console.addMessage("Welcome to the wonderful world of Terasology!" + Console.NEW_LINE +
                    Console.NEW_LINE +
                    "Type 'help' to see a list with available commands or 'help <commandName>' for command details." + Console.NEW_LINE +
                    "Text parameters do not need quotes, unless containing spaces. No commas between parameters." + Console.NEW_LINE +
                    "You can use auto-completion by typing a partial command then hitting [tab] - examples:" + Console.NEW_LINE + Console.NEW_LINE +
                    "gh + [tab] => 'ghost'" + Console.NEW_LINE +
                    "help gh + [tab] => 'help ghost' (can auto complete commands fed to help)" + Console.NEW_LINE +
                    "giv + [tab] => 'giveBlock giveItem givePermission' (use [tab] again to cycle between choices)" + Console.NEW_LINE +
                    "lS + [tab] => 'listShapes' (camel casing abbreviated commands)" + Console.NEW_LINE);
            welcomePrinted = true;
        }
    }

    @Override
    protected InteractionListener getScreenListener() {
        return screenListener;
    }

    @Override
    public boolean canBeFocus() {
        return false;
    }
}
