// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("TteTest")
public class ConsoleTest extends TerasologyTestingEnvironment {

    private final String MESSAGE_TEXT = "Test message";

    @Test
    public void testClearCommand() {
        for (int i = 0; i < 10; i++) {
            getConsole().addMessage("Just a message");
        }

        getConsole().clear();

        Iterator<Message> it = getConsole().getMessages().iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testAddMessage() {
        getConsole().addMessage(MESSAGE_TEXT);

        checkMessage(getConsole().getMessages().iterator(), true);
    }

    @Test
    public void testAddConsoleMessage() {
        getConsole().addMessage(new Message(MESSAGE_TEXT));

        checkMessage(getConsole().getMessages().iterator(), true);
    }

    @Test
    public void testAddInlineMessage() {
        getConsole().addMessage(MESSAGE_TEXT, false);

        checkMessage(getConsole().getMessages().iterator(), false);
    }

    @Test
    public void testAddInlineMessage2() {
        getConsole().addMessage(new Message(MESSAGE_TEXT, false));

        checkMessage(getConsole().getMessages().iterator(), false);
    }

    private void checkMessage(Iterator<Message> it, boolean hasNewLine) {
        assertNotNull(it);
        assertTrue(it.hasNext());
        final Message message = it.next();
        assertEquals(MESSAGE_TEXT, message.getMessage());
        assertEquals(hasNewLine, message.hasNewLine());
        assertFalse(it.hasNext());
    }

    private Console getConsole() {
        return context.get(Console.class);
    }
}
