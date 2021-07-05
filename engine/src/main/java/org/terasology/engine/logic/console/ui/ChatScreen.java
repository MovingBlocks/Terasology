// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

import org.codehaus.plexus.util.StringUtils;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.input.MouseInput;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.InteractionListener;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.layouts.ScrollableArea;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIText;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The chat console widget
 */
public class ChatScreen extends CoreScreenLayer {

    private UIText commandLine;

    @In
    private Console console;

    @In
    private LocalPlayer localPlayer;

    @In
    private NUIManager nuiManager;

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

            if (StringUtils.isNotBlank(text)) {
                String command = "say";
                List<String> params = Collections.singletonList(text);

                // TODO: move command execution to separate class
                console.execute(new Name(command), params, localPlayer.getClientEntity());
                commandLine.setText("");
                scrollArea.moveToBottom();
                NotificationOverlay overlay = nuiManager.addOverlay(NotificationOverlay.ASSET_URI, NotificationOverlay.class);
                overlay.setVisible(true);
                nuiManager.closeScreen(this);
            } else {
                commandLine.setText("");
                nuiManager.closeScreen(this);
            }
        });

        final UILabel history = find("messageHistory", UILabel.class);
        history.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Iterable<Message> messageIterable = console.getMessages(CoreMessageType.CHAT, CoreMessageType.NOTIFICATION);
                Stream<Message> messageStream = StreamSupport.stream(messageIterable.spliterator(), false);
                return messageStream.map(Message::getMessage).collect(Collectors.joining(Console.NEW_LINE));
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
