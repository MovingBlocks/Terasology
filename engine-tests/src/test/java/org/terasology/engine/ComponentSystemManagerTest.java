
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.MethodCommand;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComponentSystemManagerTest {

    private ComponentSystemManager systemUnderTest;
    private Console console;

    @BeforeEach
    public void setUp() {
        Context context = mock(Context.class);
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.getEventSystem()).thenReturn(mock(EventSystem.class));
        when(context.get(EntityManager.class)).thenReturn(entityManager);
        console = mock(Console.class);
        when(context.get(Console.class)).thenReturn(console);
        systemUnderTest = new ComponentSystemManager(context);
    }

    @Test
    public void testRegisterUpdateSubscriberAddsSubscriber() {
        UpdateSubscriberSystem system = mock(UpdateSubscriberSystem.class);

        systemUnderTest.register(system);

        assertEquals(Iterables.size(systemUnderTest.iterateUpdateSubscribers()), 1);
    }

    @Test
    public void testShutdownRemovesUpdateSubscribers() {
        UpdateSubscriberSystem system = mock(UpdateSubscriberSystem.class);

        systemUnderTest.register(system);
        systemUnderTest.shutdown();

        assertEquals(Iterables.size(systemUnderTest.iterateUpdateSubscribers()), 0);
    }

    @Test
    public void testRegisterRenderSystemAddsRenderSubscriber() {
        RenderSystem system = mock(RenderSystem.class);

        systemUnderTest.register(system);

        assertEquals(Iterables.size(systemUnderTest.iterateRenderSubscribers()), 1);
    }

    @Test
    public void testShutdownRemovesRenderSubscribers() {
        //see https://github.com/MovingBlocks/Terasology/issues/3087#issuecomment-326409756
        RenderSystem system = mock(RenderSystem.class);

        systemUnderTest.register(system);
        systemUnderTest.shutdown();

        assertEquals(Iterables.size(systemUnderTest.iterateRenderSubscribers()), 0);
    }

    @Test
    public void shouldRegisterCommand() {
        systemUnderTest.register(new SystemWithValidCommand());
        systemUnderTest.initialise();

        ArgumentCaptor<MethodCommand> methodCommandArgumentCaptor = ArgumentCaptor.forClass(MethodCommand.class);
        verify(console).registerCommand(methodCommandArgumentCaptor.capture());

        MethodCommand command = methodCommandArgumentCaptor.getValue();
        assertEquals(command.getName().toString(), "validCommandName");
    }

    @Test
    public void shouldRegisterCommandWithoutSenderAnnotation() {
        //see https://github.com/MovingBlocks/Terasology/issues/2679
        systemUnderTest.register(new SystemWithCommandMissingSenderAnnotation());
        systemUnderTest.initialise();

        ArgumentCaptor<MethodCommand> methodCommandArgumentCaptor = ArgumentCaptor.forClass(MethodCommand.class);
        verify(console).registerCommand(methodCommandArgumentCaptor.capture());

        MethodCommand command = methodCommandArgumentCaptor.getValue();
        assertEquals(command.getName().toString(), "commandWithoutSenderAnnotation");
    }

    @Test
    public void shouldLogErrorWhenRegisterCommandWithoutSenderAnnotation() {
        //see https://github.com/MovingBlocks/Terasology/issues/2679
        Appender<ILoggingEvent> appender = mockAppender();
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(appender);

        systemUnderTest.register(new SystemWithCommandMissingSenderAnnotation());
        systemUnderTest.initialise();

        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(appender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        List<String> allErrorLogMessages = loggingEventArgumentCaptor.getAllValues().stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.ERROR))
                .map(LoggingEvent::getFormattedMessage)
                .collect(toList());
        String expectedMessage = "Command commandWithoutSenderAnnotation provided by " +
                "SystemWithCommandMissingSenderAnnotation contains a EntityRef without @Sender annotation, " +
                "may cause a NullPointerException";
        assertTrue(allErrorLogMessages.contains(expectedMessage));
    }

    @SuppressWarnings("unchecked")
    private static Appender<ILoggingEvent> mockAppender() {
        return mock(Appender.class);
    }

    private static class SystemWithValidCommand extends BaseComponentSystem {
        @Command()
        public String validCommandName(@CommandParam(value = "parameter") String value, @Sender EntityRef sender) {
            return value;
        }
    }

    private static class SystemWithCommandMissingSenderAnnotation extends BaseComponentSystem {
        @Command()
        public String commandWithoutSenderAnnotation(@CommandParam(value = "parameter") String value, EntityRef sender) {
            return value;
        }
    }
}
