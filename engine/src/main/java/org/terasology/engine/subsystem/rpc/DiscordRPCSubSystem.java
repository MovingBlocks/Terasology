/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;

public class DiscordRPCSubSystem implements EngineSubsystem, IPCListener {

    private static DiscordRPCSubSystem instance;

    public static DiscordRPCSubSystem getInstance() {
        return instance;
    }

    public static void setState(String state) {
        if (instance == null) {
            return;
        }
        if (!getInstance().ready) {
            return;
        }
        RichPresence.Builder builder = new RichPresence.Builder();
        builder.setState(state);
        getInstance().ipcClient.sendRichPresence(builder.build());
    }

    private IPCClient ipcClient;
    private boolean ready;

    public DiscordRPCSubSystem() throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("More then one instance in the DiscordRPC");
        }
        ipcClient = new IPCClient(515274721080639504L);
        ipcClient.setListener(this);
        instance = this;
    }

    @Override
    public void onReady(IPCClient client) {
        this.ipcClient = client;
        if (!ready) ready = true;
        RichPresence.Builder builder = new RichPresence.Builder();
        builder.setState("In Lobby");
        client.sendRichPresence(builder.build());
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        if (ready) ready = false;
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        try {
            ipcClient.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void preShutdown() {
        ipcClient.close();
    }

    @Override
    public String getName() {
        return "DiscordRPC";
    }
}
