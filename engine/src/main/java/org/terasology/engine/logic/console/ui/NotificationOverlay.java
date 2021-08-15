// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.ui;

import com.google.common.collect.Iterables;
import org.codehaus.plexus.util.StringUtils;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.CoreMessageType;
import org.terasology.engine.logic.console.Message;
import org.terasology.nui.Canvas;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILabel;


/**
 * The miniaturized chat console widget.
 */
public class NotificationOverlay extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:notificationOverlay");

    /**
     * Extra display time per message char.
     */
    private static final float TIME_VISIBLE_PER_CHAR = 0.08f;

    private static final float TIME_VISIBLE_BASE = 5.0f;

    private static final float TIME_FADE = 0.3f;

    private static final int MAX_MESSAGES = 6;

    private static final int MAX_CHAR_PER_MSG = 250;

    private enum State {
        FADE_IN,
        VISIBLE,
        FADE_OUT,
        HIDDEN
    }

    private float time;

    private UILabel message;

    private State state = State.HIDDEN;

    @In
    private Console console;

    @Override
    public void initialise() {
        message = find("message", UILabel.class);
        message.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Iterable<Message> msgs = console.getMessages(CoreMessageType.CHAT, CoreMessageType.NOTIFICATION);
                StringBuilder messageHistory = new StringBuilder();
                int count = 1;
                int size = Iterables.size(msgs);

                for (Message msg : msgs) {
                    if (count > size - MAX_MESSAGES) {
                        messageHistory.append(StringUtils.abbreviate(msg.getMessage(), MAX_CHAR_PER_MSG));
                        if (count < size && msg.hasNewLine()) {
                            messageHistory.append(Console.NEW_LINE);
                        }
                    }
                    count++;
                }

                return messageHistory.toString();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (isVisible()) {        // depends on the "visible" binding
            refresh();
        } else {
            hideImmediately();
        }
    }

    private void refresh() {
        switch (state) {
            case VISIBLE:
                time = 0;
                break;

            case FADE_IN:
                break;

            case FADE_OUT:
                state = State.FADE_IN;
                time = TIME_FADE - time;
                break;

            case HIDDEN:
                time = 0;
                state = State.FADE_IN;
                break;
        }
    }

    private void hideImmediately() {
        state = State.HIDDEN;
        time = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        switch (state) {
            case FADE_IN:
                canvas.setAlpha(time / TIME_FADE);
                break;

            case FADE_OUT:
                canvas.setAlpha(1.0f - time / TIME_FADE);
                break;

            case HIDDEN:
                return;            // don't draw anything

            case VISIBLE:
                break;
        }

        super.onDraw(canvas);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        time += delta;

        switch (state) {
            case FADE_IN:
                if (time > TIME_FADE) {
                    time = 0;
                    state = State.VISIBLE;
                }
                break;

            case FADE_OUT:
                if (time > TIME_FADE) {
                    time = 0;
                    state = State.HIDDEN;
                }
                break;

            case HIDDEN:
                break;

            case VISIBLE:
                int textLen = message.getText().length();
                float maxTime = TIME_VISIBLE_BASE + textLen * TIME_VISIBLE_PER_CHAR;

                // longer text messages are shown for longer periods of time
                if (time > maxTime) {
                    time = 0;
                    state = State.FADE_OUT;
                }
                break;
        }
    }

    @Override
    public boolean canBeFocus() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isModal() {
        return false;
    }

}
