/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.engine.GameEngine;
import org.terasology.engine.GameThread;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.input.Keyboard;
import org.terasology.module.ModuleRegistry;
import org.terasology.naming.NameVersion;
import org.terasology.network.BroadcastServer;
import org.terasology.network.BroadcastClient;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkSystem;
import org.terasology.network.PingService;
import org.terasology.network.ServerInfoMessage;
import org.terasology.network.ServerInfoService;
import org.terasology.registry.In;
import org.terasology.nui.FontColor;
import org.terasology.nui.Color;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.nui.databinding.BindHelper;
import org.terasology.nui.databinding.IntToStringBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.layouts.CardLayout;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.time.WorldTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 */
public class JoinGameScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:joinGameScreen");

    private static final Logger logger = LoggerFactory.getLogger(JoinGameScreen.class);

    @In
    private Config config;

    @In
    private NetworkSystem networkSystem;

    @In
    private GameEngine engine;

    @In
    private ModuleManager moduleManager;

    @In
    private TranslationSystem translationSystem;

    @In
    private StorageServiceWorker storageServiceWorker;

    private final BroadcastServer broadcastServer = new BroadcastServer();

    private final BroadcastClient broadcastClient = new BroadcastClient();

    private Map<ServerInfo, Future<ServerInfoMessage>> extInfo = new HashMap<>();

    private ServerInfoService infoService;

    private ServerListDownloader downloader;

    private  UIList<ServerInfo> lanServerList;

    private UIList<ServerInfo> visibleList;

    private Predicate<ServerInfo> activeServersOnly = ServerInfo::isActive;

    private List<ServerInfo> listedServers = new ArrayList<>();

    private boolean updateComplete;

    @Override
    public void initialise() {

        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        downloader = new ServerListDownloader(config.getNetwork().getMasterServer());

        CardLayout cards = find("cards", CardLayout.class);

        UIList<ServerInfo> customServerList = find("customServerList", UIList.class);
        if (customServerList != null) {
            customServerList.setList(config.getNetwork().getServerInfos());
            configureServerList(customServerList);
        }

        UIList<ServerInfo> onlineServerList = find("onlineServerList", UIList.class);
        if (onlineServerList != null) {
            onlineServerList.setList(listedServers);
            configureServerList(onlineServerList);
        }

        lanServerList = find("lanServerList", UIList.class);
        if (lanServerList != null) {
            lanServerList.setList(BroadcastClient.lanServers);
            configureServerList(lanServerList);
        }

        ActivateEventListener activateCustom = e -> {
            cards.setDisplayedCard("customServerListScrollArea");
            find("customButton", UIButton.class).setFamily("highlight");
            find("onlineButton", UIButton.class).setFamily("default");
            find("lanButton", UIButton.class).setFamily("default");
            visibleList = customServerList;
            refresh();
        };

        WidgetUtil.trySubscribe(this, "customButton", activateCustom);

        ActivateEventListener activateOnline = e -> {
            cards.setDisplayedCard("onlineServerListScrollArea");
            find("customButton", UIButton.class).setFamily("default");
            find("onlineButton", UIButton.class).setFamily("highlight");
            find("lanButton", UIButton.class).setFamily("default");
            visibleList = onlineServerList;
            refresh();
        };
        WidgetUtil.trySubscribe(this, "onlineButton", activateOnline);

        ActivateEventListener activateLan = e -> {
            broadcastClient.startBroadcast();
            cards.setDisplayedCard("lanServerListScrollArea");
            find("customButton", UIButton.class).setFamily("default");
            find("onlineButton", UIButton.class).setFamily("default");
            find("lanButton", UIButton.class).setFamily("highlight");
            visibleList = lanServerList;
            RefreshLan refresh=new RefreshLan();
            refresh.refresh();
        };
        WidgetUtil.trySubscribe(this, "lanButton", activateLan);

        bindCustomButtons();
        bindInfoLabels();

        WidgetUtil.trySubscribe(this, "close", button -> {
            config.save();
            triggerBackAnimation();
        });

        activateOnline.onActivated(null);
    }

    @Override
    public void onOpened() {
        super.onOpened();

        infoService = new ServerInfoService();

        if (!config.getPlayer().hasEnteredUsername()) {
            getManager().pushScreen(EnterUsernamePopup.ASSET_URI, EnterUsernamePopup.class);
        }

        if (storageServiceWorker.hasConflictingIdentities()) {
            new IdentityConflictHelper(storageServiceWorker, getManager(), translationSystem).runSolver();
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!updateComplete) {
            if (downloader.isDone()) {
                updateComplete = true;
            }

            listedServers.clear();
            listedServers.addAll(Collections2.filter(downloader.getServers(), activeServersOnly));
        }
    }

    @Override
    public void onClosed() {
        infoService.close();

        super.onClosed();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void join(final String address, final int port) {
        Callable<JoinStatus> operation = () -> {
            JoinStatus joinStatus = networkSystem.join(address, port);
            return joinStatus;
        };

        final WaitPopup<JoinStatus> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage(translationSystem.translate("${engine:menu#join-game-online}"),
                translationSystem.translate("${engine:menu#connecting-to}")
                        + " '"
                        + address
                        + ":"
                        + port
                        + "' - "
                        + translationSystem.translate("${engine:menu#please-wait}"));
        popup.onSuccess(result -> {
            if (result.getStatus() != JoinStatus.Status.FAILED) {
                engine.changeState(new StateLoading(result));
            } else {
                MessagePopup screen = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                screen.setMessage(translationSystem.translate("${engine:menu#failed-to-join}"),
                        translationSystem.translate("${engine:menu#could-not-connect-to-server}") + " - " + result.getErrorMessage());
            }
        });
        popup.startOperation(operation, true);

    }

    private void configureServerList(final UIList<ServerInfo> serverList) {

        serverList.subscribe((widget, item) -> join(item.getAddress(), item.getPort()));

        serverList.subscribeSelection((widget, item) -> {
            extInfo.remove(item);
            if (item != null) {
                extInfo.put(item, infoService.requestInfo(item.getAddress(), item.getPort()));
                refreshPing();
            }
        });

        serverList.setItemRenderer(new StringTextRenderer<ServerInfo>() {
            @Override
            public String getString(ServerInfo value) {
                return value.getName();
            }
        });
    }

    private void bindInfoLabels() {

        final ReadOnlyBinding<ServerInfo> infoBinding = new ReadOnlyBinding<ServerInfo>() {

            @Override
            public ServerInfo get() {
                return visibleList.getSelection();
            }
        };

        UILabel name = find("name", UILabel.class);
        if (name != null) {
            name.bindText(BindHelper.bindBoundBeanProperty("name", infoBinding, ServerInfo.class, String.class));
        }

        UILabel owner = find("owner", UILabel.class);
        if (owner != null) {
            owner.bindText(BindHelper.bindBoundBeanProperty("owner", infoBinding, ServerInfo.class, String.class));
        }

        UILabel address = find("address", UILabel.class);
        if (address != null) {
            address.bindText(BindHelper.bindBoundBeanProperty("address", infoBinding, ServerInfo.class, String.class));
        }

        UILabel port = find("port", UILabel.class);
        if (port != null) {
            port.bindText(new IntToStringBinding(BindHelper.bindBoundBeanProperty("port", infoBinding, ServerInfo.class, int.class)));
        }

        UILabel onlinePlayers = find("onlinePlayers", UILabel.class);
        onlinePlayers.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Future<ServerInfoMessage> info = extInfo.get(visibleList.getSelection());
                if (info != null) {
                    if (info.isDone()) {
                        return getOnlinePlayersText(info);
                    } else {
                        return translationSystem.translate("${engine:menu#join-server-requested}");
                    }
                }
                return null;
            }
        });

        UILabel modules = find("modules", UILabel.class);
        modules.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Future<ServerInfoMessage> info = extInfo.get(visibleList.getSelection());
                if (info != null) {
                    if (info.isDone()) {
                        return getModulesText(info);
                    } else {
                        return translationSystem.translate("${engine:menu#join-server-requested}");
                    }
                }
                return null;
            }
        });

        UILabel worlds = find("worlds", UILabel.class);
        worlds.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                Future<ServerInfoMessage> info = extInfo.get(visibleList.getSelection());
                if (info != null) {
                    if (info.isDone()) {
                        return getWorldText(info);
                    } else {
                        return translationSystem.translate("${engine:menu#join-server-requested}");
                    }
                }
                return null;
            }
        });

        UIButton joinButton = find("join", UIButton.class);
        if (joinButton != null) {
            joinButton.bindEnabled(new ReadOnlyBinding<Boolean>() {

                @Override
                public Boolean get() {
                    return infoBinding.get() != null;
                }
            });
            joinButton.subscribe(button -> {
                config.save();
                ServerInfo item = infoBinding.get();
                if (item != null) {
                    join(item.getAddress(), item.getPort());
                }
            });
        }

        UIButton refreshButton = find("refresh", UIButton.class);
        if (refreshButton != null) {
            refreshButton.bindEnabled(new ReadOnlyBinding<Boolean>() {

                @Override
                public Boolean get() {
                    return visibleList.getSelection() != null;
                }
            });
            refreshButton.subscribe(button -> {
                refresh();
            });
        }

        UIButton broadCastServerButton = find("broadcast", UIButton.class);
        if (broadCastServerButton != null) {
            broadCastServerButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return true;
                }
            });
            broadCastServerButton.subscribe(button -> {
                broadCastServerButton.setText(translationSystem.translate("${engine:menu#start-server-broadcast}"));
                try {
                    broadcastServer.startBroadcast();
                } catch (Exception e) {

                }
            });
        }
    }

    private void bindCustomButtons() {

        UIList<?> customServerList = find("customServerList", UIList.class);
        ReadOnlyBinding<Boolean> localSelectedServerOnly = new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return customServerList.getSelection() != null;
            }
        };

        UIButton add = find("add", UIButton.class);
        if (add != null) {
            add.subscribe(button -> {
                AddServerPopup popup = getManager().pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
                // select the entry if added successfully
                popup.onSuccess(item -> {
                    config.getNetwork().addServerInfo(item);
                    visibleList.setSelection(item);
                });
            });
        }

        UIButton edit = find("edit", UIButton.class);
        if (edit != null) {
            edit.bindEnabled(localSelectedServerOnly);
            edit.subscribe(button -> {
                AddServerPopup popup = getManager().pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
                ServerInfo info = visibleList.getSelection();
                popup.setServerInfo(info);

                // editing invalidates the currently known info, so query it again
                popup.onSuccess(item -> extInfo.put(item, infoService.requestInfo(item.getAddress(), item.getPort())));
            });
        }

        UIButton removeButton = find("remove", UIButton.class);
        if (removeButton != null) {
            removeButton.bindEnabled(localSelectedServerOnly);
            removeButton.subscribe(button -> {
                ServerInfo info = visibleList.getSelection();
                if (info != null) {
                    config.getNetwork().removeServerInfo(info);
                    extInfo.remove(info);
                    visibleList.setSelection(null);
                }
            });
        }

        UILabel downloadLabel = find("download", UILabel.class);
        if (downloadLabel != null) {
            downloadLabel.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return translationSystem.translate(downloader.getStatus());
                }
            });
        }
    }

    private String getWorldText(Future<ServerInfoMessage> info) {
        try {
            List<String> codedWorldInfo = new ArrayList<>();
            ServerInfoMessage serverInfoMessage = info.get();

            if (serverInfoMessage == null) {
                return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
            }

            for (WorldInfo wi : serverInfoMessage.getWorldInfoList()) {
                float timeInDays = wi.getTime() / (float) WorldTime.DAY_LENGTH;
                codedWorldInfo.add(String.format("%s (%.2f days)", wi.getTitle(), timeInDays));
            }
            return Joiner.on('\n').join(codedWorldInfo);
        } catch (ExecutionException | InterruptedException e) {
            return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
        }
    }

    private String getOnlinePlayersText(Future<ServerInfoMessage> info) {
        try {
            List<String> codedWorldInfo = new ArrayList<>();
            ServerInfoMessage serverInfoMessage = info.get();

            if (serverInfoMessage == null) {
                return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
            }

            codedWorldInfo.add(String.format("%d", serverInfoMessage.getOnlinePlayersAmount()));
            return Joiner.on('\n').join(codedWorldInfo);
        } catch (ExecutionException | InterruptedException e) {
            return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
        }
    }

    private String getModulesText(Future<ServerInfoMessage> info) {
        try {
            ServerInfoMessage serverInfoMessage = info.get();

            if (serverInfoMessage == null) {
                return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
            }

            List<String> codedModInfo = new ArrayList<>();
            ModuleRegistry reg = moduleManager.getRegistry();
            for (NameVersion entry : serverInfoMessage.getModuleList()) {
                boolean isInstalled = reg.getModule(entry.getName(), entry.getVersion()) != null;
                Color color = isInstalled ? Color.GREEN : Color.RED;
                codedModInfo.add(FontColor.getColored(entry.toString(), color));
            }
            Collections.sort(codedModInfo, String.CASE_INSENSITIVE_ORDER);
            return Joiner.on('\n').join(codedModInfo);
        } catch (ExecutionException | InterruptedException e) {
            return FontColor.getColored(translationSystem.translate("${engine:menu#connection-failed}"), Color.RED);
        }
    }

    private void refreshPing() {
        String address = visibleList.getSelection().getAddress();
        int port = visibleList.getSelection().getPort();
        String name = visibleList.getSelection().getName();
        UILabel ping = find("ping", UILabel.class);
        ping.setText("Requested");

        Thread getPing = new Thread(() -> {
            PingService pingService = new PingService(address, port);
            // we're not on the game thread, so we cannot modify GUI elements directly
            try {
                long responseTime = pingService.call();
                if (visibleList.getSelection().getAddress().equals(address)) {
                    GameThread.asynch(() -> ping.setText(responseTime + " ms."));
                }
            } catch (IOException e) {
                String text = translationSystem.translate("${engine:menu#connection-failed}");
                // Check if selection name is same as earlier when response is received before updating ping field
                if (name.equals(visibleList.getSelection().getName())) {
                    GameThread.asynch(() -> ping.setText(FontColor.getColored(text, Color.RED)));
                }
            }
        });

        // TODO: once the common thread pool is in place this could be posted there and the
        // returned Future could be kept and cancelled as soon the selected menu entry changes
        getPing.start();
    }

    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            if (event.getKey() == Keyboard.Key.ESCAPE) {
                if (isEscapeToCloseAllowed()) {
                    triggerBackAnimation();
                    return true;
                }
            } else if (event.getKey() == Keyboard.Key.R) {
                refresh();
            }
        }
        return false;
    }

    public void refresh() {
        ServerInfo i = visibleList.getSelection();
        visibleList.setSelection(null);
        extInfo.clear();
        visibleList.setSelection(i);
    }

    private class RefreshLan extends TimerTask {
        @Override
        public void run() {
            lanServerList.setList(BroadcastClient.lanServers);
            logger.info(broadcastClient.getLanServers().toString());
            configureServerList(lanServerList);
            visibleList = lanServerList;
        }
        public void refresh() {
            Timer timer = new Timer(true);
            timer.schedule(new RefreshLan(), 0, 1500);
        }
    }

}
