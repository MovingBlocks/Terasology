
package org.terasology.engine;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.MethodCommand;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ComponentSystemManagerTest {

    private ComponentSystemManager systemUnderTest;
    private Console console;

    @Before
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

        assertThat(Iterables.size(systemUnderTest.iterateUpdateSubscribers()), is(1));
    }

    @Test
    public void testShutdownRemovesUpdateSubscribers() {
        UpdateSubscriberSystem system = mock(UpdateSubscriberSystem.class);

        systemUnderTest.register(system);
        systemUnderTest.shutdown();

        assertThat(Iterables.size(systemUnderTest.iterateUpdateSubscribers()), is(0));
    }

    @Test
    public void testRegisterRenderSystemAddsRenderSubscriber() {
        RenderSystem system = mock(RenderSystem.class);

        systemUnderTest.register(system);

        assertThat(Iterables.size(systemUnderTest.iterateRenderSubscribers()), is(1));
    }

    @Test
    public void testShutdownRemovesRenderSubscribers() {
        //see https://github.com/MovingBlocks/Terasology/issues/3087#issuecomment-326409756
        RenderSystem system = mock(RenderSystem.class);

        systemUnderTest.register(system);
        systemUnderTest.shutdown();

        assertThat(Iterables.size(systemUnderTest.iterateRenderSubscribers()), is(0));
    }

    @Test
    public void shouldRegisterCommand() {
        systemUnderTest.register(new SystemWithValidCommand());
        systemUnderTest.initialise();

        ArgumentCaptor<MethodCommand> methodCommandArgumentCaptor = ArgumentCaptor.forClass(MethodCommand.class);
        verify(console).registerCommand(methodCommandArgumentCaptor.capture());

        MethodCommand command = methodCommandArgumentCaptor.getValue();
        assertThat(command.getName().toString(), is("validCommandName"));
    }

    @Test
    public void shouldRegisterCommandWithoutSenderAnnotation() {
        //see https://github.com/MovingBlocks/Terasology/issues/2679
        systemUnderTest.register(new SystemWithCommandMissingSenderAnnotation());
        systemUnderTest.initialise();

        ArgumentCaptor<MethodCommand> methodCommandArgumentCaptor = ArgumentCaptor.forClass(MethodCommand.class);
        verify(console).registerCommand(methodCommandArgumentCaptor.capture());

        MethodCommand command = methodCommandArgumentCaptor.getValue();
        assertThat(command.getName().toString(), is("commandWithoutSenderAnnotation"));
    }

    @Test
    public void shouldLogErrorWhenRegisterCommandWithoutSenderAnnotation() {
        //see https://github.com/MovingBlocks/Terasology/issues/2679
        Appender<ILoggingEvent> appender = mockAppender();
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(appender);

        systemUnderTest.register(new SystemWithCommandMissingSenderAnnotation());
        systemUnderTest.initialise();

        ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());
        List<String> allErrorLogMessages = loggingEventArgumentCaptor.getAllValues().stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.ERROR))
                .map(LoggingEvent::getFormattedMessage)
                .collect(toList());
        String expectedMessage = "Command commandWithoutSenderAnnotation provided by " +
                "SystemWithCommandMissingSenderAnnotation contains a EntityRef without @Sender annotation, " +
                "may cause a NullPointerException";
        assertThat(allErrorLogMessages, hasItem(expectedMessage));
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
