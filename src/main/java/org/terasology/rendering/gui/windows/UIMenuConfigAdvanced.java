package org.terasology.rendering.gui.windows;

import java.util.Arrays;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.config.AdvancedConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIButton.ButtonType;
import org.terasology.rendering.gui.widgets.UIComboBox;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIListItem;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.world.chunks.perBlockStorage.PerBlockStorageManager;

/**
 * Exposes internal configurable details of the Terasology engine to the user.
 * 
 * @see org.terasology.config.AdvancedConfig
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class UIMenuConfigAdvanced extends UIWindow {

    private final UIImage title;
    private final UILabel subtitle;
    private final UILabel warning;
    private final UIButton backToConfigMenuButton;

    private final UIDisplayElement mainContainer, chunkConfig, monitoringConfig;
    
    private final AdvancedConfig config; 
    private final PerBlockStorageManager manager;
    
    protected UIComposite initFactoryCombo(String title, String id, String selectedFactory, SelectionListener listener) {
        final GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        final UIComposite comp = new UIComposite();
        comp.setLayout(layout);
        comp.setVisible(true);
        comp.setId(id);
        
        UILabel label = new UILabel();
        label.setColor(Color.black);
        label.setText(title);
        label.setVisible(true);
        label.setId(id+":label");
        
        final UIComboBox combo = new UIComboBox(new Vector2f(256f, 32f), new Vector2f(256f, 96f));
        combo.setVisible(true);
        combo.setId(id+":combo");
        String[] factories = manager.getArrayFactoryIds();
        Arrays.sort(factories);
        for (String factory : factories) {
            String name = factory.substring(factory.lastIndexOf('.')+1);
            UIListItem item = new UIListItem(name, factory);
            item.setTextColor(Color.black);
            item.setPadding(new Vector4f(2f, 2f, 2f, 2f));
            combo.addItem(item);
            if (factory.equals(selectedFactory))
                combo.select(item);
        }
        combo.addSelectionListener(listener);

        
        comp.addDisplayElement(label);
        comp.addDisplayElement(combo);
        
        return comp;
    }
    
    protected UIComposite initOnOffButton(final String label, String id, boolean state, ChangedListener listener) {
        final GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        final UIComposite comp = new UIComposite();
        comp.setLayout(layout);
        comp.setVisible(true);
        comp.setId(id);

        final UIButton button = new UIButton(new Vector2f(256f, 32f), ButtonType.TOGGLE);
        button.setVisible(true);
        button.setId(id+":button");
        button.setToggleState(state);
        button.getLabel().setText(label + ": " + (state ? "On" : "Off"));
        button.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
               UIButton b = (UIButton) element;
               b.getLabel().setText(label + ": " + (b.getToggleState() ? "On" : "Off"));
            }
        });
        button.addChangedListener(listener);
        
        comp.addDisplayElement(button);
        
        return comp;
    }
    
    protected UIComposite initMainContainer(Vector2f pos) {
        
        final GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        final UIComposite comp = new UIComposite();
        comp.setPosition(pos);
        comp.setHorizontalAlign(EHorizontalAlign.CENTER);
        comp.setLayout(layout);
        comp.setVisible(true);
        
        comp.addDisplayElement(chunkConfig);
        comp.addDisplayElement(monitoringConfig);
        
        return comp;
    }
    
    protected UIComposite initChunkConfig() {
        
        final GridLayout layout = new GridLayout(2);
        layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        final UIComposite comp = new UIComposite();
        comp.setHorizontalAlign(EHorizontalAlign.CENTER);
        comp.setLayout(layout);
        comp.setVisible(true);
        
        comp.addDisplayElement(initFactoryCombo("Default class for block data", "block", config.getBlocksFactoryName(), new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                config.setBlocksFactory((String)((UIComboBox)element).getSelection().getValue());
            }
        }));
        comp.addDisplayElement(initFactoryCombo("Default class for sunlight data", "sunlight", config.getSunlightFactoryName(), new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                config.setSunlightFactory((String)((UIComboBox)element).getSelection().getValue());
            }
        }));
        comp.addDisplayElement(initFactoryCombo("Default class for light data", "light", config.getLightFactoryName(), new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                config.setLightFactory((String)((UIComboBox)element).getSelection().getValue());
            }
        }));
        comp.addDisplayElement(initFactoryCombo("Default class for liquid data", "liquid", config.getExtraFactoryName(), new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                config.setExtraFactory((String)((UIComboBox)element).getSelection().getValue());
            }
        }));
        
        
        comp.addDisplayElement(initOnOffButton("Runtime Chunk Compression", "chunkDeflation", config.isChunkDeflationEnabled(), new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UIButton b = (UIButton) element;
                config.setChunkDeflationEnabled(b.getToggleState());
            }
        }));
        
        comp.addDisplayElement(initOnOffButton("Log Chunk Compression", "chunkDeflationLogging", config.isChunkDeflationLoggingEnabled(), new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UIButton b = (UIButton) element;
                config.setChunkDeflationLoggingEnabled(b.getToggleState());
            }
        }));
        
        
        comp.orderDisplayElementTop(comp.getElementById("liquid"));
        comp.orderDisplayElementTop(comp.getElementById("light"));
        comp.orderDisplayElementTop(comp.getElementById("sunlight"));
        comp.orderDisplayElementTop(comp.getElementById("block"));
        
        return comp;
    }
    
    protected UIComposite initMonitoringConfig() {
        
        final GridLayout layout = new GridLayout(2);
        layout.setCellPadding(new Vector4f(2f, 2f, 2f, 2f));
        
        final UIComposite comp = new UIComposite();
        comp.setHorizontalAlign(EHorizontalAlign.CENTER);
        comp.setLayout(layout);
        comp.setVisible(true);
        
        comp.addDisplayElement(initOnOffButton("Enable Advanced Monitoring", "advancedMonitoring", config.isAdvancedMonitoringEnabled(), new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UIButton b = (UIButton) element;
                config.setAdvancedMonitoringEnabled(b.getToggleState());
            }
        }));
        
        comp.addDisplayElement(initOnOffButton("Show Advanced Monitor @ Startup", "advancedMonitorVisible", config.isAdvancedMonitorVisibleAtStartup(), new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UIButton b = (UIButton) element;
                config.setAdvancedMonitorVisibleAtStartup(b.getToggleState());
            }
        }));
        
        return comp;
    }
    
    public UIMenuConfigAdvanced() {
        setId("config:advanced");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
        config = CoreRegistry.get(org.terasology.config.Config.class).getAdvanced();
        manager = CoreRegistry.get(PerBlockStorageManager.class);
        
        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 28f));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        subtitle = new UILabel("Advanced Settings");
        subtitle.setHorizontalAlign(EHorizontalAlign.CENTER);
        subtitle.setPosition(new Vector2f(0f, 128f));
        subtitle.setVisible(true);

        warning = new UILabel("Warning! Only change these settings if you know what you do!");
        warning.setColor(Color.red);
        warning.setHorizontalAlign(EHorizontalAlign.CENTER);
        warning.setPosition(new Vector2f(0f, 148f));
        warning.setVisible(true);

        chunkConfig = initChunkConfig();
        monitoringConfig = initMonitoringConfig();
        mainContainer = initMainContainer(new Vector2f(0f, 200f));

        backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f), UIButton.ButtonType.NORMAL);
        backToConfigMenuButton.getLabel().setText("Back");
        backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToConfigMenuButton.setPosition(new Vector2f(0f, 300f + 7 * 40f));
        backToConfigMenuButton.setVisible(true);
        backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                manager.loadAdvancedConfig(config);
                getGUIManager().openWindow("config");
            }
        });
        

        addDisplayElement(title);
        addDisplayElement(subtitle);
        addDisplayElement(warning);
        addDisplayElement(mainContainer);
        addDisplayElement(backToConfigMenuButton);
    }

}
