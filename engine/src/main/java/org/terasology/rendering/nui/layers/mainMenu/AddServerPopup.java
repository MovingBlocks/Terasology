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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.engine.TerasologyConstants;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

import com.google.common.primitives.Ints;

/**
 * @author Immortius
 */
public class AddServerPopup extends CoreScreenLayer {

    public static final AssetUri ASSET_URI = new AssetUri(AssetType.UI_ELEMENT, "engine:addServerPopup");
    
    @In
    private Config config;
    private UIText nameText;
    private UIText addressText;
    private UIText portText;
    private UIButton okButton;
    private UIButton cancelButton;
    private ServerInfo serverInfo;

    @Override
    public void initialise() {
        nameText = find("name", UIText.class);
        addressText = find("address", UIText.class);
        portText = find("port", UIText.class);
        okButton = find("ok", UIButton.class);
        cancelButton = find("cancel", UIButton.class);

        okButton.subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {

                String name = nameText.getText();
                String address = addressText.getText();
                Integer port = Ints.tryParse(portText.getText());
                
                if (serverInfo == null) {
                    // create new
                    serverInfo = new ServerInfo(name, address, port);
                    
                    config.getNetwork().add(serverInfo);
                } else {
                    // update existing
                    serverInfo.setName(name);
                    serverInfo.setAddress(address);
                    serverInfo.setPort(port);
                }

                getManager().popScreen();
            }
        });
        
        okButton.bindEnabled(new ReadOnlyBinding<Boolean>() {

            @Override
            public Boolean get() {
                return !nameText.getText().isEmpty()
                    && !addressText.getText().isEmpty()
                    && Ints.tryParse(portText.getText()) != null;
            }
        });

        cancelButton.subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
        
        // copy name to address on ENTER if address is empty
        nameText.subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget widget) {
                if (addressText.getText().isEmpty()) {
                    addressText.setText(nameText.getText());
                    addressText.setCursorPosition(addressText.getText().length());
                }
                
                getManager().setFocus(addressText);
            }
        });
        
        // simulate tabbing behavior
        // TODO: replace with NUI tabbing, once available
        addressText.subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget widget) {
                getManager().setFocus(portText);
            }
        });
        
    }

    @Override
    public void onOpened() {
        super.onOpened();

        this.serverInfo = null;
        nameText.setText("");
        addressText.setText("");

        portText.setText(Integer.toString(TerasologyConstants.DEFAULT_PORT));
        portText.setCursorPosition(portText.getText().length());

        getManager().setFocus(nameText);
    }
    
    @Override
    public void onClosed() {
        super.onClosed();
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
        
        nameText.setText(serverInfo.getName());
        nameText.setCursorPosition(nameText.getText().length());
        
        addressText.setText(serverInfo.getAddress());
        addressText.setCursorPosition(addressText.getText().length());

        portText.setText(Integer.toString(serverInfo.getPort()));
        portText.setCursorPosition(portText.getText().length());
    }
}
