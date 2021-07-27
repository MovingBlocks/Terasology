// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

import org.terasology.engine.entitySystem.Component;

/**
 * PingSubscriberComponent, only on the server system, will be added to a client entity when this client subscribe.
 * Server will only send ping information to the clients subscribed.
 * <p>
 * It can be used to stock the ping information of users in future.
 */
public class PingSubscriberComponent implements Component {
}
