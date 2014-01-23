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
package org.terasology.rendering.nui.mainMenu;

import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateLoading;
import org.terasology.registry.In;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.ListSelectionBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.ItemActivateEventListener;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;

/**
 * @author Immortius
 */
public class JoinGameScreen extends UIScreenLayer {

    @In
    private Config config;

    @In
    private NetworkSystem networkSystem;

    @In
    private GameEngine engine;

    private UIList<ServerInfo> serverList;

    @Override
    public void initialise() {
        serverList = find("serverList", UIList.class);
        if (serverList != null) {
            serverList.bindList(BindHelper.bindBeanListProperty("servers", config.getNetwork(), ServerInfo.class));
            serverList.setItemRenderer(new StringTextRenderer<ServerInfo>() {
                @Override
                public String getString(ServerInfo value) {
                    return value.getName();
                }
            });
            serverList.subscribe(new ItemActivateEventListener<ServerInfo>() {
                @Override
                public void onItemActivated(UIWidget widget, ServerInfo item) {
                    join(item.getAddress());
                }
            });

            UILabel name = find("name", UILabel.class);
            name.bindText(BindHelper.bindBoundBeanProperty("name", new ListSelectionBinding<ServerInfo>(serverList), ServerInfo.class, String.class));

            UILabel address = find("address", UILabel.class);
            address.bindText(BindHelper.bindBoundBeanProperty("address", new ListSelectionBinding<ServerInfo>(serverList), ServerInfo.class, String.class));

            WidgetUtil.trySubscribe(this, "add", new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget button) {
                    getManager().pushScreen("engine:addServerPopup");
                }
            });
            WidgetUtil.trySubscribe(this, "remove", new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget button) {
                    if (serverList.getSelection() != null) {
                        config.getNetwork().remove(serverList.getSelection());
                        serverList.setSelection(null);
                    }
                }
            });
            WidgetUtil.trySubscribe(this, "join", new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget button) {
                    config.save();
                    if (serverList.getSelection() != null) {
                        join(serverList.getSelection().getAddress());
                    }
                }
            });
        }
        WidgetUtil.trySubscribe(this, "joinDirect", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                config.save();
                getManager().pushScreen("engine:joinServerPopup");
            }
        });


        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                config.save();
                getManager().popScreen();
            }
        });
    }

    private void join(String address) {
        JoinStatus joinStatus = networkSystem.join(address, TerasologyConstants.DEFAULT_PORT);
        if (joinStatus.getStatus() != JoinStatus.Status.FAILED) {
            engine.changeState(new StateLoading(joinStatus));
        } else {
            getManager().pushScreen("engine:errorMessagePopup", ErrorMessagePopup.class)
                    .setError("Failed to Join", "Could not connect to server - " + joinStatus.getErrorMessage());
        }
    }
}
