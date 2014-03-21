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
import org.terasology.math.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class ConsoleScreen extends CoreScreenLayer {

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    private UICommandEntry commandLine;

    private InteractionListener screenListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT && commandLine != null) {
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
        commandLine.setTabCompletionEngine(new ConsoleTabCompletionEngine(console));
        commandLine.bindCommandHistory(new ReadOnlyBinding<List<String>>() {
            @Override
            public List<String> get() {
                return console.getPreviousCommands();
            }
        });
        commandLine.subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget widget) {
                console.execute(commandLine.getText(), localPlayer.getClientEntity());
                commandLine.setText("");
                scrollArea.moveToBottom();
            }
        });

        final UILabel history = find("messageHistory", UILabel.class);
        history.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                StringBuilder messageList = new StringBuilder();
                Iterator<Message> messageIterator = console.getMessages().iterator();
                while (messageIterator.hasNext()) {
                    Message message = messageIterator.next();
                    messageList.append(FontColor.getColored(message.getMessage(), message.getType().getColor()));
                    messageList.append(Message.NEW_LINE);
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
