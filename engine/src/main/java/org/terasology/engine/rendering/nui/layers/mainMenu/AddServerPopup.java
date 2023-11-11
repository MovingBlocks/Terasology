// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import com.google.common.primitives.Ints;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.ServerInfo;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIText;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

import java.util.function.Consumer;

public class AddServerPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:addServerPopup!instance");

    private UIText nameText;
    private UIText ownerText;
    private UIText addressText;
    private UIText portText;
    private UIButton okButton;
    private UIButton cancelButton;
    private ServerInfo serverInfo;
    private UILabel tip;

    private Consumer<ServerInfo> successFunc;

    @Override
    public void initialise() {
        nameText = find("name", UIText.class);
        ownerText = find("owner", UIText.class);
        addressText = find("address", UIText.class);
        portText = find("port", UIText.class);
        okButton = find("ok", UIButton.class);
        cancelButton = find("cancel", UIButton.class);
        tip = find("tip", UILabel.class);

        okButton.subscribe(button -> {

            String name = nameText.getText();
            String owner = ownerText.getText();
            String address = addressText.getText();
            Integer portBoxed = Ints.tryParse(portText.getText().trim());
            int port = (portBoxed != null) ? portBoxed : TerasologyConstants.DEFAULT_PORT;

            if (serverInfo == null) {
                // create new
                serverInfo = new ServerInfo(name, address, port);
                serverInfo.setOwner(owner);
            } else {
                // update existing
                serverInfo.setName(name);
                serverInfo.setAddress(address);
                serverInfo.setPort(port);
                serverInfo.setOwner(owner);
            }

            if (successFunc != null) {
                successFunc.accept(serverInfo);
            }
            getManager().popScreen();
        });

        okButton.bindEnabled(new ReadOnlyBinding<Boolean>() {

            @Override
            public Boolean get() {
                return !nameText.getText().isEmpty()
                        && !addressText.getText().isEmpty()
                        && Ints.tryParse(portText.getText().trim()) != null;
            }
        });

        cancelButton.subscribe(button -> getManager().popScreen());

        // copy name to address on ENTER if address is empty
        nameText.subscribe(widget -> {
            if (addressText.getText().isEmpty()) {
                addressText.setText(nameText.getText());
                addressText.setCursorPosition(addressText.getText().length());
            }

            getManager().setFocus(addressText);
        });

        // simulate tabbing behavior
        // TODO: replace with NUI tabbing, once available
        addressText.subscribe(widget -> {
            getManager().setFocus(portText);
        });

    }

    @Override
    public void onOpened() {
        super.onOpened();

        serverInfo = null;
        successFunc = null;
        ownerText.setText(null);
        nameText.setText(null);
        addressText.setText(null);

        portText.setText(Integer.toString(TerasologyConstants.DEFAULT_PORT));
        portText.setCursorPosition(portText.getText().length());

        getManager().setFocus(nameText);
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;

        nameText.setText(serverInfo.getName());
        nameText.setCursorPosition(nameText.getText().length());

        ownerText.setText(serverInfo.getOwner());
        ownerText.setCursorPosition(ownerText.getText().length());

        addressText.setText(serverInfo.getAddress());
        addressText.setCursorPosition(addressText.getText().length());

        portText.setText(Integer.toString(serverInfo.getPort()));
        portText.setCursorPosition(portText.getText().length());
    }

    /**
     * @param success the method to call when editing is complete
     */
    public void onSuccess(Consumer<ServerInfo> success) {
        this.successFunc = success;
    }

    /**
     * And a listen to the cancel button.
     * @param listener The listener added on the cancel button
     */
    public void onCancel(ActivateEventListener listener) {
        cancelButton.subscribe(listener);
    }

    public void removeTip() {
        tip.setVisible(false);
    }
}
