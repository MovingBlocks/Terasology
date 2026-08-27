// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.IntegerComponent;

import static com.google.common.truth.Truth.assertThat;

/**
 * Regression test for #1079: "EntityManager::copy works incorrectly on a remote client" - copying a
 * networked entity on a client and then modifying the copy's components was reported to also modify
 * the original, attributed at the time to the copy's {@link EntityRef} "getting directed to the last
 * registered thing with the given net id".
 * <p>
 * That mechanism no longer exists: {@code NetworkEntitySystem#onAddNetworkComponent}, which registers a
 * {@link NetworkComponent}-bearing entity into the client/server's netId lookup, is gated
 * {@code @NetFilterEvent(netFilter = RegisterMode.AUTHORITY)} - a client never runs it, including for a
 * locally-created copy that happens to carry a (copied) {@code NetworkComponent}. This test exercises
 * the actual copy path on a real client context to confirm component mutation is properly isolated.
 */
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class EntityCopyOnClientTest {

    @In
    private ModuleTestingHelper helper;

    @Test
    public void copyingANetworkedEntityOnAClientDoesNotMutateTheOriginal() throws Exception {
        Context clientContext = helper.createClient();
        EntityManager clientEntityManager = clientContext.get(EntityManager.class);

        // A NetworkComponent is what distinguishes the entity this issue was about from any other -
        // its presence is what used to trigger the redirection.
        NetworkComponent networkComponent = new NetworkComponent();
        networkComponent.setNetworkId(9001);
        EntityRef original = clientEntityManager.create(networkComponent, new IntegerComponent(42));

        EntityRef copy = original.copy();
        assertThat(copy.exists()).isTrue();
        assertThat(copy.getId()).isNotEqualTo(original.getId());

        IntegerComponent copyValue = copy.getComponent(IntegerComponent.class);
        copyValue.value = 99;
        copy.saveComponent(copyValue);

        assertThat(copy.getComponent(IntegerComponent.class).value).isEqualTo(99);
        assertThat(original.getComponent(IntegerComponent.class).value).isEqualTo(42);
    }
}
