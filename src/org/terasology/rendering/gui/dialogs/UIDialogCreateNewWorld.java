package org.terasology.rendering.gui.dialogs;

import org.newdawn.slick.Color;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.Config;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIInput;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;

public class UIDialogCreateNewWorld extends UIDisplayWindow{
    private UIButton _okButton;
    private UIButton _cancelButton;

    private UIText   _inputSeedLabel;
    private UIInput  _inputSeed;
    private UIText   _inputWorldTitleLabel;
    private UIInput  _inputWorldTitle;

    public UIDialogCreateNewWorld(String title, Vector2f size){
        super(title, size);

        _inputSeed = new UIInput(new Vector2f(256f, 30f));
        _inputSeed.setVisible(true);

        _inputWorldTitle = new UIInput(new Vector2f(256f, 30f));
        _inputWorldTitle.setVisible(true);

        _inputWorldTitleLabel = new UIText("Enter the world name:");
        _inputWorldTitleLabel.setColor(Color.darkGray);
        _inputWorldTitleLabel.getSize().y = 16f;
        _inputWorldTitleLabel.setVisible(true);

        _inputSeedLabel      =  new UIText("Enter the seed:");
        _inputSeedLabel.setColor(Color.darkGray);
        _inputSeedLabel.getSize().y = 16f;
        _inputSeedLabel.setVisible(true);

        _inputWorldTitleLabel.setPosition(new Vector2f(15f, 32f));
        _inputWorldTitle.setPosition(new Vector2f(_inputWorldTitleLabel.getPosition().x,
                                                 _inputWorldTitleLabel.getPosition().y + _inputWorldTitleLabel.getSize().y + 8f));
        _inputSeedLabel.setPosition(new Vector2f(_inputWorldTitle.getPosition().x,
                                                  _inputWorldTitle.getPosition().y + _inputWorldTitle.getSize().y + 16f));
        _inputSeed.setPosition(new Vector2f(_inputSeedLabel.getPosition().x,
                                            _inputSeedLabel.getPosition().y + _inputSeedLabel.getSize().y + 8f));

        _okButton = new UIButton(new Vector2f(128f, 32f));
        _okButton.getLabel().setText("Play");
        _okButton.setPosition(new Vector2f(size.x/2-_okButton.getSize().x - 16f, size.y-_okButton.getSize().y));
        _okButton.setVisible(true);

        _okButton.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                if(_inputSeed.getValue().length()>0){
                    Config.getInstance().setDefaultSeed(_inputSeed.getValue());
                }

                if(_inputWorldTitle.getValue().length()>0){
                    Config.getInstance().setWorldTitle(_inputWorldTitle.getValue());
                }else{
                    Config.getInstance().setWorldTitle("World1");
                }
                Terasology.getInstance().setGameState(Terasology.GAME_STATE.SINGLE_PLAYER);
            }
        });


        _cancelButton = new UIButton(new Vector2f(128f, 32f));
        _cancelButton.setPosition(new Vector2f(_okButton.getPosition().x + _okButton.getSize().x + 16f,_okButton.getPosition().y));
        _cancelButton.getLabel().setText("Cancel");
        _cancelButton.setVisible(true);

        _cancelButton.addClickListener(new IClickListener() {
            public void clicked(UIDisplayElement element) {
                setVisible(false);
            }
        });

        addDisplayElement(_okButton);
        addDisplayElement(_cancelButton);
        addDisplayElement(_inputSeed);
        addDisplayElement(_inputSeedLabel);
        addDisplayElement(_inputWorldTitleLabel);
        addDisplayElement(_inputWorldTitle);
    }
}
