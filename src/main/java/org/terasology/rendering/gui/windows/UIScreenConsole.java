/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.gui.windows;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.engine.CoreRegistry;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.console.CommandInfo;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleSubscriber;
import org.terasology.logic.console.Message;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.style.StyleShadow.EShadowDirection;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.utilities.collection.CircularBuffer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 * The in-game chat.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenConsole extends UIWindow implements ConsoleSubscriber {

    private static final int COMMAND_HISTORY_SIZE = 30;
    private static final int MESSAGE_HISTORY_SIZE = 30;

    private final Console console;
    private final LocalPlayer localPlayer;

    private final CircularBuffer<String> commandHistory = CircularBuffer.create(COMMAND_HISTORY_SIZE);

    private final UIText inputBox;
    private final UIList messageList;

    private int commandCursor = commandHistory.size();

    public UIScreenConsole() {
        console = CoreRegistry.get(Console.class);
        localPlayer = CoreRegistry.get(LocalPlayer.class);

        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});
        setCloseBinds(new String[]{"engine:console"});
        setId("chat");
        setModal(true);
        maximize();

        inputBox = new UIText();
        inputBox.setSize(new Vector2f(900f, 28f));
        inputBox.setBackgroundColor(new Color(255, 255, 255, 200));
        inputBox.setVerticalAlign(EVerticalAlign.BOTTOM);
        inputBox.setSelectionColor(Color.gray);
        inputBox.setPosition(new Vector2f(2, -2));
        inputBox.setVisible(true);
        inputBox.addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                if (event.isDown()) {
                    // submit message
                    if (event.getKey() == Keyboard.KEY_RETURN) {
                        String message = inputBox.getText().trim();
                        inputBox.deleteText();

                        commandHistory.add(message);
                        commandCursor = commandHistory.size();
                        console.execute(message, localPlayer.getClientEntity());
                    }
                    //message history previous
                    else if (event.getKey() == Keyboard.KEY_UP) {
                        if (commandCursor > 0) {
                            commandCursor--;
                            inputBox.setText(commandHistory.get(commandCursor));
                            inputBox.setCursorEnd();
                        }
                    }
                    //message history next
                    else if (event.getKey() == Keyboard.KEY_DOWN) {
                        if (commandCursor < commandHistory.size()) {
                            commandCursor++;
                            if (commandCursor == commandHistory.size()) {
                                inputBox.setText("");
                            } else {
                                inputBox.setText(commandHistory.get(commandCursor));
                            }
                            inputBox.setCursorEnd();
                        }
                    }
                    //guess command
                    else if (event.getKey() == Keyboard.KEY_TAB) {
                        String message = inputBox.getText().trim();

                        String commandName = message.substring(1);
                        List<CommandInfo> commands = console.getCommandList();
                        List<CommandInfo> matches = new ArrayList<CommandInfo>();

                        //check for matching commands
                        for (CommandInfo cmd : commands) {
                            if (cmd.getName().regionMatches(0, commandName, 0, commandName.length())) {
                                matches.add(cmd);
                            }
                        }

                        //one match found
                        if (matches.size() == 1) {
                            inputBox.setText(matches.get(0).getName());
                            inputBox.setCursorEnd();
                        }
                        //multiple matches found
                        else if (matches.size() > 1) {
                            //add list of available commands
                            String commandMatches = "";
                            for (CommandInfo cmd : matches) {
                                if (!commandMatches.isEmpty()) {
                                    commandMatches += " ";
                                }

                                commandMatches += cmd.getName();
                            }
                            console.addMessage(commandMatches);

                            //complete input

                        }
                    }
                }
            }
        });
        setFocus(inputBox);

        messageList = new UIList();
        messageList.setSize(new Vector2f(900f, 400f));
        messageList.setBackgroundColor(new Color(255, 255, 255, 200));
        messageList.setShadow(new Vector4f(0f, 3f, 3f, 0f), EShadowDirection.OUTSIDE, 1f);
        messageList.setBorderSolid(new Vector4f(1f, 1f, 1f, 1f), new Color(0, 0, 0));
        messageList.setVerticalAlign(EVerticalAlign.BOTTOM);
        messageList.setPosition(new Vector2f(2, -32));
        messageList.setPadding(new Vector4f(0f, 5f, 0f, 5f));
        messageList.setDisabled(true);
        messageList.setVisible(true);

        addDisplayElement(inputBox);
        addDisplayElement(messageList);

        for (Message message : console.getMessages()) {
            addMessage(message);
        }
        messageList.scrollToBottom();
        console.subscribe(this);
    }

    private void addMessage(Message message) {
        UIListItem item = new UIListItem(message.getMessage(), null);
        item.setPadding(new Vector4f(0f, 5f, 0f, 5f));
        item.setTextColor(new Color(message.getType().getColor()));
        messageList.addItem(item);

        if (messageList.getItemCount() > MESSAGE_HISTORY_SIZE) {
            messageList.removeItem(0);
        }
    }

    @Override
    public void onNewConsoleMessage(Message message) {
        boolean scroll = messageList.isScrolledToBottom();
        boolean scrollable = messageList.isScrollable();

        addMessage(message);

        if (scroll || messageList.isScrollable() != scrollable) {
            messageList.scrollToBottom();
        }
    }
}
