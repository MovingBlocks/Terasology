
package org.terasology.engine;

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentSystemManagerTest {

    private ComponentSystemManager systemUnderTest;

    @Before
    public void setUp() {
        Context context = mock(Context.class);
        EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.getEventSystem()).thenReturn(mock(EventSystem.class));
        when(context.get(EntityManager.class)).thenReturn(entityManager);
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

}
