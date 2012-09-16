package org.terasology.rendering.gui.windows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.PathManager;
import org.terasology.rendering.gui.dialogs.UIDialogServer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.DialogListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class UIMenuMultiplayer extends UIWindow {

    private String serverFile = PathManager.getInstance().getWorldPath() + File.separator + "server.json";
    
    private final UIList list;

    private final UIComposite buttonContainer;
    private final UIButton addServerButton;
    private final UIButton editServerButton;
    private final UIButton deleteServerButton;
    private final UIButton joinServerButton;
    private final UIButton directjoinServerButton;
    private final UIButton refreshServerButton;  
    private final UIButton backButton;
    
    public class Server {
        @SerializedName("name")
        private String name = "";
        
        @SerializedName("ip")
        private String ip = "";
        
        public Server() {
            
        }
        
        public Server(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

    public UIMenuMultiplayer() {
        setId("multiplayer");
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
                
            }
        });
        list.setVisible(true);
        
        buttonContainer = new UIComposite();
        GridLayout layout = new GridLayout(3);
        layout.setPadding(new Vector4f(5f, 5f, 5f, 5f));
        buttonContainer.setLayout(layout);
        buttonContainer.setHorizontalAlign(EHorizontalAlign.CENTER);
        buttonContainer.setPosition(new Vector2f(0f, 500f));
        buttonContainer.setVisible(true);
        
        addServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        addServerButton.getLabel().setText("Add");
        addServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {                
                UIDialogServer dialog = new UIDialogServer(new Server());
                dialog.addDialogListener(new DialogListener() {
                    @Override
                    public void close(UIDisplayElement dialog, Object returnValue) {
                        if (returnValue != null) {
                            add((Server) returnValue);
                        }
                    }
                });
                dialog.open();
                
                saveServerList();
            }
        });
        addServerButton.setVisible(true);
        
        editServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        editServerButton.getLabel().setText("Edit");
        editServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                final UIListItem item = list.getSelection();
                if (item != null) {
                    UIDialogServer dialog = new UIDialogServer((Server) item.getValue());
                    dialog.addDialogListener(new DialogListener() {
                        @Override
                        public void close(UIDisplayElement dialog, Object returnValue) {
                            if (returnValue != null) {
                                Server server = (Server) returnValue;
                                item.setValue(server);
                                ((UILabel)item.getElementById("serverName")).setText(server.getName());
                                ((UILabel)item.getElementById("serverIp")).setText(server.getIp());
                            }
                        }
                    });
                    dialog.open();
                    
                    saveServerList();
                }
            }
        });
        editServerButton.setVisible(true);
        
        deleteServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        deleteServerButton.getLabel().setText("Delete");
        deleteServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                list.removeItem(list.getSelectionIndex());
                saveServerList();
            }
        });
        deleteServerButton.setVisible(true);
        
        joinServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        joinServerButton.getLabel().setText("Join");
        joinServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                
            }
        });
        joinServerButton.setVisible(true);
        
        directjoinServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        directjoinServerButton.getLabel().setText("Direct Join");
        directjoinServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                
            }
        });
        directjoinServerButton.setVisible(true);
        
        refreshServerButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        refreshServerButton.getLabel().setText("Refresh");
        refreshServerButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                loadServerList();
            }
        });
        refreshServerButton.setVisible(true);
        
        backButton = new UIButton(new Vector2f(160f, 32f), UIButton.eButtonType.NORMAL);
        backButton.getLabel().setText("Back");
        backButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().openWindow("main");
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
    
    private void add(Server server) {
        UIListItem item = new UIListItem("", server);
        
        GridLayout layout = new GridLayout(1);
        layout.setPadding(new Vector4f(5f, 5f, 5f, 10f));
        
        UIComposite composite = new UIComposite();
        composite.setSize("100%", "0px");
        composite.setLayout(layout);
        composite.setVisible(true);
        
        UILabel label = new UILabel(server.getName());
        label.setId("serverName");
        label.setVisible(true);
        composite.addDisplayElement(label);
        
        label = new UILabel(server.getIp());
        label.setId("serverIp");
        label.setColor(Color.lightGray);
        label.setVisible(true);
        composite.addDisplayElement(label);
        
        UIImage connectionImage = new UIImage(AssetManager.loadTexture("engine:icons"));
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
        
        list.addItem(item);
    }

    public void loadServerList() {
        try {
            FileReader reader = new FileReader(serverFile);
            
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray Jarray = parser.parse(reader).getAsJsonArray();
            
            Server server;
            for (JsonElement obj : Jarray)
            {
                server = gson.fromJson(obj , Server.class);
                //validate the JSON content
                if (!server.getName().isEmpty() && !server.getIp().isEmpty()) {
                    add(server);
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            
        }
        
    }
    
    private void saveServerList() {
        try {
            FileWriter writer = new FileWriter(serverFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            Server[] servers = new Server[list.getItemCount()];
            List<UIListItem> itemList = list.getItems();
            
            for (int i = 0; i < servers.length; i++) {
                servers[i] = (Server) itemList.get(i).getValue();
            }
            
            writer.write(gson.toJson(servers));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            
        }
    }
}
