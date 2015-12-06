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

import org.terasology.logic.console.Console;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * The miniaturized chat console widget
 *
 */
public class MiniChatOverlay extends CoreScreenLayer {

    /**
     * Extra display time per message char
     */
    private static final float TIME_VISIBLE_PER_CHAR = 0.08f;

    private static final float TIME_VISIBLE_BASE = 1.0f;

    private static final float TIME_FADE = 0.3f;

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
                String last = "";

                for (Message msg : msgs) {
                    last = msg.getMessage();
                }

                return last;
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
    public boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isModal() {
        return false;
    }

}
