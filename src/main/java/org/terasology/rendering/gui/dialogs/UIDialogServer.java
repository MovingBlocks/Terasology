package org.terasology.rendering.gui.dialogs;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.Color;
import org.terasology.config.ServerInfo;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIText;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIDialogServer extends UIDialog {
    
    private UIComposite containerButtons;
    private UIButton okButton;
    private UIButton cancelButton;

    private UIComposite containerForm;
    private UILabel labelName;
    private UIText inputName;
    private UILabel labelIp;
    private UIText inputIp;

    public UIDialogServer() {
        this("New Server", "");

    }

    public UIDialogServer(String name, String address) {
        super(new Vector2f(400f, 240f));
        setTitle("Edit Server");

        setup(name, address);
    }
    
    private void setup(String name, String address) {
        setModal(true);
        
        //form
        containerForm = new UIComposite();
        GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(5f, 5f, 5f, 10f));
        containerForm.setLayout(layout);
        containerForm.setSize("100%", "100%");
        containerForm.setPosition(new Vector2f(0f, 30f));
        containerForm.setVisible(true);
        
        labelName = new UILabel("Server Name:");
        labelName.setColor(Color.darkGray);
        labelName.setVisible(true);
        
        inputName = new UIText();
        inputName.setText(name);
        inputName.setSize(new Vector2f(380f, 30f));
        inputName.setVisible(true);
        
        labelIp = new UILabel("Address:");
        labelIp.setColor(Color.darkGray);
        labelIp.setVisible(true);
        
        inputIp = new UIText();
        inputIp.setText(address);
        inputIp.setSize(new Vector2f(380f, 30f));
        inputIp.setVisible(true);
        
        //buttons
        containerButtons = new UIComposite();
        layout = new GridLayout(2);
        layout.setCellPadding(new Vector4f(0f, 5f, 0f, 5f));
        containerButtons.setLayout(layout);
        containerButtons.setHorizontalAlign(EHorizontalAlign.CENTER);
        containerButtons.setVerticalAlign(EVerticalAlign.BOTTOM);
        containerButtons.setPosition(new Vector2f(0f, -20f));
        containerButtons.setVisible(true);
        
        okButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        okButton.getLabel().setText("Ok");
        okButton.setVisible(true);
        okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                ServerInfo serverInfo = new ServerInfo(inputName.getText(), inputIp.getText());
                closeDialog(EReturnCode.OK, serverInfo);
            }
        });
        
        cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        cancelButton.getLabel().setText("Cancel");
        cancelButton.setVisible(true);
        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                closeDialog(EReturnCode.CANCEL, null);
            }
        });
        
        containerForm.addDisplayElement(labelName);
        containerForm.addDisplayElement(inputName);
        containerForm.addDisplayElement(labelIp);
        containerForm.addDisplayElement(inputIp);
        
        containerButtons.addDisplayElement(okButton);
        containerButtons.addDisplayElement(cancelButton);
        
        addDisplayElement(containerForm);
        addDisplayElement(containerButtons);
    }
    
}
