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
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.Collections;
import java.util.List;

/**
 * The chat console widget
 */
public class ChatScreen extends CoreScreenLayer {

    private UIText commandLine;

    @In
    private Console console;
    
    @In
    private LocalPlayer localPlayer;

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

        commandLine = find("commandLine", UIText.class);
        getManager().setFocus(commandLine);

        commandLine.subscribe(widget -> {
            String text = commandLine.getText();

            if (!text.isEmpty()) {
                String command = "say";
                List<String> params = Collections.singletonList(text);

                // TODO: move command execution to separate class
                console.execute(new Name(command), params, localPlayer.getClientEntity());
                commandLine.setText("");
                scrollArea.moveToBottom();
            }
        });

        final UILabel history = find("messageHistory", UILabel.class);
        history.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                StringBuilder messageList = new StringBuilder();
                for (Message msg : console.getMessages(CoreMessageType.CHAT, CoreMessageType.NOTIFICATION)) {
                    messageList.append(msg.getMessage());
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
