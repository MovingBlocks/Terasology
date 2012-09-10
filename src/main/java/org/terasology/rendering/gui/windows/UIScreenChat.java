/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.terasology.input.binds.ConsoleButton;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.manager.CommandManager;
import org.terasology.logic.manager.MessageManager.MessageSubscription;
import org.terasology.logic.manager.MessageManager.Message;
import org.terasology.logic.manager.CommandManager.Command;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIText;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.gui.widgets.list.UIListItemText;

/**
 * The in-game chat.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIScreenChat extends UIWindow {
    
    private final CommandManager commandManager;
    private final String commandPrefix = "/";
    
    //history
    private final List<String> history = new ArrayList<String>();
    private final int historyMax = 30;
    private int historyPosition = 0;
    
    private final UIText inputBox;
    private final UIList messageList;
    
    private final MessageSubscription chatSubscription = new MessageSubscription() {
        @Override
        public void message(Message message) {
            boolean scroll = messageList.isScrolledToBottom();
            boolean scrollable = messageList.isScrollable();
            
            UIListItemText item = new UIListItemText(message.getMessage(), null);
            item.setPadding(new Vector4f(0f, 5f, 0f, 5f));
            item.setColor(Color.black);
            messageList.addItem(item);
            
            if (messageList.getItemCount() > historyMax) {
                messageList.removeItem(0);
            }

            if (scroll || messageList.isScrollable() != scrollable) {
                messageList.scrollToBottom();
            }
        }
    };
    
    public UIScreenChat() {
        commandManager = CommandManager.getInstance();
        MessageManager.getInstance().subscribe(chatSubscription);
        
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        setCloseBinds(new String[] {ConsoleButton.ID});
        setId("chat");
        setModal(true);
        maximize();

        addWindowListener(new WindowListener() {
            @Override
            public void open(UIDisplayElement element) {
                setFocus(inputBox);
            }
            
            @Override
            public void close(UIDisplayElement element) {
                
            }
        });
        
        inputBox = new UIText();
        inputBox.setSize(new Vector2f(900f, 28f));
        inputBox.setBackgroundColor(255, 255, 255, 0.8f);
        inputBox.setBorderSolid(1, 0, 0, 0, 1f);
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
    
                        MessageManager.getInstance().addMessage(message);
                        addHistory(message);
    
                        // check if message is a command
                        if (message.startsWith(commandPrefix)) {
                            // execute the command
                            message = message.substring(1);
                            commandManager.execute(message);
                        }
                    }
                    //message history previous
                    else if (event.getKey() == Keyboard.KEY_UP) {
                        moveHistory(+1);
                        inputBox.setText(getHistory());
                        inputBox.setCursorEnd();
                    }
                    //message history next
                    else if (event.getKey() == Keyboard.KEY_DOWN) {
                        moveHistory(-1);
                        inputBox.setText(getHistory());
                        inputBox.setCursorEnd();
                    }
                    //guess command
                    else if (event.getKey() == Keyboard.KEY_TAB) {
                        String message = inputBox.getText().trim();
                        if (message.startsWith(commandPrefix)) {
                            String commandName = message.substring(1);
                            List<Command> commands = commandManager.getCommandList();
                            List<Command> matches = new ArrayList<Command>();
                            
                            //check for matching commands
                            for (Command cmd : commands) {
                                if (cmd.getName().regionMatches(0, commandName, 0, commandName.length())) {
                                    matches.add(cmd);
                                }
                            }
                            
                            //one match found
                            if (matches.size() == 1) {
                                inputBox.setText(commandPrefix + matches.get(0).getName());
                                inputBox.setCursorEnd();
                            }
                            //multiple matches found
                            else if (matches.size() > 1) {
                                String commandMatches = "";
                                for (Command cmd : matches) {
                                    if (!commandMatches.isEmpty()) {
                                        commandMatches += " ";
                                    }
                                    
                                    commandMatches += cmd.getName();
                                }
                                MessageManager.getInstance().addMessage(commandMatches);
                            }
                        }
                    }
                }
            }
        });
        
        messageList = new UIList();
        messageList.setSize(new Vector2f(900f, 400f));
        messageList.setBackgroundColor(255, 255, 255, 0.8f);
        messageList.setBorderSolid(1, 0, 0, 0, 1f);
        messageList.setVerticalAlign(EVerticalAlign.BOTTOM);
        messageList.setPosition(new Vector2f(2, -32));
        messageList.setPadding(new Vector4f(0f, 5f, 0f, 5f));
        messageList.setDisabled(true);
        messageList.setVisible(true);
        
        addDisplayElement(inputBox);
        addDisplayElement(messageList);
        
        startMessage();
    }
    
    private void startMessage() {
        MessageManager.getInstance().addMessage("Welcome to the wonderfull world of Terasology!\n\nType '/help' to see a list with available commands.\nTo see a detailed command description try '/help \"<commandName>\"'.");
    }

    private void addHistory(String message) {
        history.add(0, message);
        historyPosition = -1;
        
        if (history.size() > historyMax) {
            history.remove(history.get(history.size() - 1));
        }
    }
    
    public String getHistory() {        
        return history.get(historyPosition);
    }
    
    private void moveHistory(int i) {
        historyPosition += i;
        
        if (historyPosition < 0) {
            historyPosition = 0;
        } else if (historyPosition > (history.size() - 1)) {
            historyPosition = history.size() - 1;
        }
    }
}
