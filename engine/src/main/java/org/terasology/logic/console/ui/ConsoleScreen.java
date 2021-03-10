// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

import org.codehaus.plexus.util.StringUtils;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.input.MouseInput;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.Message;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.InteractionListener;
import org.terasology.engine.registry.In;
import org.terasology.nui.FontColor;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.SwipeMenuAnimationSystem;
import org.terasology.engine.rendering.nui.animation.SwipeMenuAnimationSystem.Direction;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.layouts.ScrollableArea;
import org.terasology.nui.widgets.UIText;

import java.util.List;

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

        setAnimationSystem(new SwipeMenuAnimationSystem(0.2f, Direction.TOP_TO_BOTTOM));

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
            String text = commandLine.getText();
            if (StringUtils.isNotBlank(text)) {
                console.execute(text, localPlayer.getClientEntity());
            }
            scrollArea.moveToBottom();
        });

        final UIText history = find("messageHistory", UIText.class);
        history.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                StringBuilder messageList = new StringBuilder();
                for (Message message : console.getMessages()) {
                    messageList.append(FontColor.getColored(message.getMessage(), message.getType().getColor()));
                    if (message.hasNewLine()) {
                        messageList.append(Console.NEW_LINE);
                    }
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
                    "giv + [tab] => 'give givePermission' (use [tab] again to cycle between choices)" + Console.NEW_LINE +
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
