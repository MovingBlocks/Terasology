package org.terasology.rendering.gui.windows;

import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.TerasologyConstants;
import org.terasology.game.modes.StateLoading;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.gui.dialogs.UIDialogServer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.DialogListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog.EReturnCode;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

public class UIMenuJoinServer extends UIWindow {

    private final UIList list;

    private final UIComposite buttonContainer;
    private final UIButton addServerButton;
    private final UIButton editServerButton;
    private final UIButton deleteServerButton;
    private final UIButton joinServerButton;
    private final UIButton directjoinServerButton;
    private final UIButton refreshServerButton;
    private final UIButton backButton;

    public UIMenuJoinServer() {
        setId("joinserver");
        setBackgroundImage("engine:menubackground");
        setModal(true);
        maximize();

        list = new UIList();
        list.setSize(new Vector2f(512f, 256f));
        list.setHorizontalAlign(EHorizontalAlign.CENTER);
        list.setPosition(new Vector2f(0f, 230f));
        list.setPadding(new Vector4f(10f, 5f, 10f, 5f));
        list.setBackgroundImage("engine:gui_menu", new Vector2f(264f, 18f), new Vector2f(159f, 63f));
        list.setBorderImage("engine:gui_menu", new Vector2f(256f, 0f), new Vector2f(175f, 88f), new Vector4f(16f, 7f, 7f, 7f));
        list.addDoubleClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                tryJoinServer();
            }
        });
        list.setVisible(true);

        buttonContainer = new UIComposite();
        GridLayout layout = new GridLayout(3);
        layout.setCellPadding(new Vector4f(5f, 5f, 5f, 5f));
        buttonContainer.setLayout(layout);
        buttonContainer.setHorizontalAlign(EHorizontalAlign.CENTER);
        buttonContainer.setPosition(new Vector2f(0f, 500f));
        buttonContainer.setVisible(true);

        addServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        addServerButton.getLabel().setText("Add");
        addServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                UIDialogServer dialog = new UIDialogServer();
                dialog.addDialogListener(new DialogListener() {
                    @Override
                    public void close(UIDisplayElement dialog, EReturnCode returnCode, Object returnValue) {
                        if (returnCode == EReturnCode.OK && returnValue != null) {
                            add((ServerInfo) returnValue);
                            CoreRegistry.get(Config.class).getServer().add((ServerInfo) returnValue);
                        }
                    }
                });
                dialog.open();
            }
        });
        addServerButton.setVisible(true);

        editServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        editServerButton.getLabel().setText("Edit");
        editServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                final UIListItem item = list.getSelection();
                if (item != null) {
                    final ServerInfo serverInfo = (ServerInfo) item.getValue();
                    UIDialogServer dialog = new UIDialogServer(serverInfo.getName(), serverInfo.getAddress());
                    dialog.addDialogListener(new DialogListener() {
                        @Override
                        public void close(UIDisplayElement dialog, EReturnCode returnCode, Object returnValue) {
                            if (returnCode == EReturnCode.OK && returnValue != null) {
                                ServerInfo result = (ServerInfo) returnValue;
                                serverInfo.setName(result.getName());
                                serverInfo.setAddress(result.getAddress());
                                ((UILabel) item.getElementById("serverName")).setText(serverInfo.getName());
                                ((UILabel) item.getElementById("serverIp")).setText(serverInfo.getAddress());
                            }
                        }
                    });
                    dialog.open();
                }
            }
        });
        editServerButton.setVisible(true);

        deleteServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        deleteServerButton.getLabel().setText("Delete");
        deleteServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (list.getSelection() != null) {
                    ServerInfo info = (ServerInfo) list.getSelection().getValue();
                    CoreRegistry.get(Config.class).getServer().remove(info);
                    list.removeItem(list.getSelectionIndex());
                }
            }
        });
        deleteServerButton.setVisible(true);

        joinServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        joinServerButton.getLabel().setText("Join");
        joinServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                tryJoinServer();
            }
        });
        joinServerButton.setVisible(true);

        directjoinServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        directjoinServerButton.getLabel().setText("Direct Join");
        directjoinServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {

            }
        });
        directjoinServerButton.setVisible(true);

        refreshServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        refreshServerButton.getLabel().setText("Refresh");
        refreshServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadServerList();
            }
        });
        refreshServerButton.setVisible(true);

        backButton = new UIButton(new Vector2f(160f, 32f), UIButton.ButtonType.NORMAL);
        backButton.getLabel().setText("Back");
        backButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                saveServerList();
                getGUIManager().openWindow("main");
            }
        });
        backButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backButton.setPosition(new Vector2f(0f, 600f));
        backButton.setVisible(true);

        loadServerList();

        addDisplayElement(list);
        addDisplayElement(buttonContainer);
        addDisplayElement(backButton);
        buttonContainer.addDisplayElement(addServerButton);
        buttonContainer.addDisplayElement(deleteServerButton);
        buttonContainer.addDisplayElement(editServerButton);
        buttonContainer.addDisplayElement(joinServerButton);
        buttonContainer.addDisplayElement(directjoinServerButton);
        buttonContainer.addDisplayElement(refreshServerButton);
    }

    private void tryJoinServer() {
        saveServerList();
        if (list.getSelection() != null && list.getSelection().getValue() != null) {
            ServerInfo server = (ServerInfo) list.getSelection().getValue();
            NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
            if (networkSystem.join(server.getAddress(), TerasologyConstants.DEFAULT_PORT)) {
                CoreRegistry.get(GameEngine.class).changeState(new StateLoading(NetworkMode.CLIENT));
            } else {
                getGUIManager().showMessage("Failed to Join", "Could not connect to server (does it exist?)");
            }
        }
    }

    private void loadServerList() {
        list.removeAll();
        Config config = CoreRegistry.get(Config.class);
        for (ServerInfo serverInfo : config.getServer()) {
            add(serverInfo);
        }
    }

    private void saveServerList() {
        Config config = CoreRegistry.get(Config.class);
        config.save();
    }

    private void add(ServerInfo server) {
        UIListItem item = new UIListItem("", server);

        GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(5f, 5f, 5f, 10f));

        UIComposite composite = new UIComposite();
        composite.setLayout(layout);
        composite.setVisible(true);

        UILabel label = new UILabel(server.getName());
        label.setId("serverName");
        label.setVisible(true);
        composite.addDisplayElement(label);

        label = new UILabel(server.getAddress());
        label.setId("serverIp");
        label.setColor(Color.lightGray);
        label.setVisible(true);
        composite.addDisplayElement(label);

        UIImage connectionImage = new UIImage(Assets.getTexture("engine:icons"));
        connectionImage.setTextureOrigin(new Vector2f(0f, 56f)); //15f
        connectionImage.setTextureSize(new Vector2f(9f, 7f));
        connectionImage.setSize(new Vector2f(27f, 21f));
        connectionImage.setPosition(new Vector2f(-50f, 0f));
        connectionImage.setHorizontalAlign(EHorizontalAlign.RIGHT);
        connectionImage.setVerticalAlign(EVerticalAlign.CENTER);
        connectionImage.setVisible(true);
        connectionImage.setId("connectionImage");

        item.addDisplayElement(composite);
        item.addDisplayElement(connectionImage);

        item.setPadding(new Vector4f(10f, 10f, 10f, 10f));

        list.addItem(item);
    }

}
